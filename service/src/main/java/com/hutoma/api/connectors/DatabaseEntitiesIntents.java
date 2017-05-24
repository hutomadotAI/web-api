package com.hutoma.api.connectors;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.IntentVariable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 20/10/2016.
 */
public class DatabaseEntitiesIntents extends Database {

    @Inject
    public DatabaseEntitiesIntents(ILogger logger, Provider<DatabaseCall> callProvider,
                                   Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    public List<Entity> getEntities(final UUID devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getEntities", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            try {
                List<Entity> entities = new ArrayList<>();
                while (rs.next()) {
                    entities.add(new Entity(
                            rs.getString("name"),
                            rs.getBoolean("isSystem")));
                }
                return entities;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<UUID> getAisForEntity(final UUID devid, final String entityName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAIsForEntity", 2).add(devid).add(entityName);
            final ResultSet rs = call.executeQuery();
            try {
                final ArrayList<UUID> ais = new ArrayList<>();
                while (rs.next()) {
                    ais.add(UUID.fromString(rs.getString("aiid")));
                }
                return ais;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public ApiEntity getEntity(final UUID devid, final String entityName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            ResultSet rs;
            try {
                call.initialise("getEntityDetails", 2).add(devid).add(entityName);
                rs = call.executeQuery();
                boolean isSystem = false;
                if (rs.next()) {
                    isSystem = rs.getBoolean("isSystem");
                }
                final ArrayList<String> entityValues = new ArrayList<>();
                // only custom entities have values as ystem entities are handled externally
                if (!isSystem) {
                    call.initialise("getEntityValues", 2).add(devid).add(entityName);
                    rs = call.executeQuery();
                    while (rs.next()) {
                        entityValues.add(rs.getString("value"));
                    }
                }
                return new ApiEntity(entityName, devid, entityValues, isSystem);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<String> getIntents(final UUID devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntents", 2).add(devid).add(aiid);
            ResultSet rs = call.executeQuery();
            ArrayList<String> intents = new ArrayList<>();
            while (rs.next()) {
                intents.add(rs.getString("name"));
            }
            return intents;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    /***
     * Gets a fully populated intent object
     * including intent, usersays, variables and prompts
     * @param aiid the aiid that owns the intent
     * @param intentName
     * @return an intent
     * @throws DatabaseException if things go wrong
     */
    public ApiIntent getIntent(final UUID aiid, final String intentName) throws DatabaseException {

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            ResultSet rs = transaction.getDatabaseCall().initialise("getIntent", 2).add(aiid).add(intentName)
                    .executeQuery();
            if (!rs.next()) {
                // the intent was not found at all
                return null;
            }

            // build the intent
            ApiIntent intent = new ApiIntent(rs.getString("name"), rs.getString("topic_in"), rs.getString("topic_out"));

            // get the user triggers
            ResultSet saysRs = transaction.getDatabaseCall().initialise("getIntentUserSays", 2)
                    .add(aiid).add(intentName).executeQuery();
            while (saysRs.next()) {
                intent.addUserSays(saysRs.getString("says"));
            }

            // get the list of responses
            ResultSet intentResponseRs = transaction.getDatabaseCall().initialise("getIntentResponses", 2)
                    .add(aiid).add(intentName).executeQuery();
            while (intentResponseRs.next()) {
                intent.addResponse(intentResponseRs.getString("response"));
            }

            // get each intent variable
            ResultSet varRs = transaction.getDatabaseCall().initialise("getIntentVariables", 2)
                    .add(aiid).add(intentName).executeQuery();
            while (varRs.next()) {
                int varID = varRs.getInt("id");
                String uuidString = varRs.getString("dev_id");
                UUID devOwnerUUID = UUID.fromString(uuidString);
                IntentVariable variable = new IntentVariable(
                        varRs.getString("entity_name"),
                        devOwnerUUID,
                        varRs.getBoolean("required"),
                        varRs.getInt("n_prompts"),
                        varRs.getString("value"),
                        varRs.getBoolean("isPersistent"));

                // for each variable get all its prompts
                ResultSet promptRs = transaction.getDatabaseCall().initialise("getIntentVariablePrompts", 2)
                        .add(aiid).add(varID).executeQuery();
                while (promptRs.next()) {
                    variable.addPrompt(promptRs.getString("prompt"));
                }

                intent.addVariable(variable);
            }

            // nothing was written but this prevents an auto-rollback
            transaction.commit();
            return intent;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public void writeEntity(final UUID devid, final String entityOldName, final ApiEntity entity)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            // add or update the entity
            transaction.getDatabaseCall().initialise("addUpdateEntity", 3)
                    .add(devid).add(entityOldName).add(entity.getEntityName()).executeUpdate();

            // read the entity's values
            ResultSet valuesRs = transaction.getDatabaseCall().initialise("getEntityValues", 2)
                    .add(devid).add(entity.getEntityName()).executeQuery();

            // put them into a set
            HashSet<String> currentValues = new HashSet<>();
            while (valuesRs.next()) {
                currentValues.add(valuesRs.getString("value"));
            }

            if (entity.getEntityValueList() != null) {
                // for each new entity value, check if it was already there
                for (String entityValue : entity.getEntityValueList()) {
                    // if it was then remove it, otherwise it is new - add it
                    if (!currentValues.remove(entityValue)) {
                        transaction.getDatabaseCall().initialise("addEntityValue", 3)
                                .add(devid).add(entity.getEntityName()).add(entityValue).executeUpdate();
                    }
                }
            }

            // anything left over is an old obsolete value - delete it
            for (String obsoleteEntityValue : currentValues) {
                transaction.getDatabaseCall().initialise("deleteEntityValue", 3)
                        .add(devid).add(entity.getEntityName()).add(obsoleteEntityValue).executeUpdate();
            }

            // commit everything
            transaction.commit();

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean deleteEntity(UUID devid, String entityName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            int rowCount = call.initialise("deleteEntity", 2).add(devid).add(entityName).executeUpdate();
            return rowCount > 0;
        }
    }

    /***
     * Add a new complete intent to the database,
     * or update an intent previously called 'intentName' to the new intent.
     * If intentName is different to the one specified in the intent then this will rename the intent.
     * @param devid owner id
     * @param aiid owner aiid
     * @param intentName the old name if we are renaming the intent
     * @param intent the new data
     * @throws DatabaseException
     */
    public void writeIntent(final UUID devid, final UUID aiid, final String intentName, final ApiIntent intent)
            throws DatabaseException {

        // start the transaction
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            // add or update the intent
            transaction.getDatabaseCall().initialise("addUpdateIntent", 6)
                    .add(devid).add(aiid).add(intentName).add(intent.getIntentName())
                    .add(intent.getTopicIn()).add(intent.getTopicOut())
                    .executeUpdate();

            // synchronise user says (change as needed)
            updateIntentUserSays(devid, aiid, intent, transaction);
            // synchronise responses (change as needed)
            updateIntentResponses(devid, aiid, intent, transaction);
            // synchronise variables and their prompts
            updateIntentVariables(devid, aiid, intent, transaction);

            // commit everything
            transaction.commit();

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Deletes an intent and all dependent objects by DB cascade delete
     * @param devid
     * @param aiid
     * @param intentName
     * @return
     * @throws DatabaseException
     */
    public boolean deleteIntent(final UUID devid, final UUID aiid, final String intentName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            int rowCount = call.initialise("deleteIntent", 3).add(devid).add(aiid).add(intentName).executeUpdate();
            return rowCount > 0;
        }
    }

    /***
     * * Synchronise "user says" between database and new data
     * @param devid
     * @param aiid
     * @param intent
     * @param transaction
     * @throws DatabaseException
     * @throws SQLException
     */
    private void updateIntentUserSays(final UUID devid, final UUID aiid, final ApiIntent intent,
                                      final DatabaseTransaction transaction)
            throws DatabaseException, SQLException {

        // read current
        ResultSet readCurrentRs = transaction.getDatabaseCall().initialise("getIntentUserSays", 2)
                .add(aiid).add(intent.getIntentName()).executeQuery();

        HashSet<String> currentSet = new HashSet<>();
        // put them into a set
        while (readCurrentRs.next()) {
            currentSet.add(readCurrentRs.getString("says"));
        }

        if (intent.getUserSays() != null) {
            // for each new bit of data
            for (String newValue : intent.getUserSays()) {
                // mark it if it already existed
                if (!currentSet.remove(newValue)) {
                    // or add it if it didn't
                    transaction.getDatabaseCall().initialise("addIntentUserSays", 4)
                            .add(devid).add(aiid).add(intent.getIntentName()).add(newValue).executeUpdate();
                }
            }
        }

        // delete the old ones
        for (String obsoleteValue : currentSet) {
            transaction.getDatabaseCall().initialise("deleteIntentUserSays", 4)
                    .add(devid).add(aiid).add(intent.getIntentName()).add(obsoleteValue).executeUpdate();
        }
    }

    /***
     * Synchronise user responses between database and new data
     * @param devid
     * @param aiid
     * @param intent
     * @param transaction
     * @throws DatabaseException
     * @throws SQLException
     */
    private void updateIntentResponses(final UUID devid, final UUID aiid, final ApiIntent intent,
                                       final DatabaseTransaction transaction)
            throws DatabaseException, SQLException {


        // read current
        ResultSet readCurrentRs = transaction.getDatabaseCall().initialise("getIntentResponses", 2)
                .add(aiid).add(intent.getIntentName()).executeQuery();

        // put them into a set
        HashSet<String> currentSet = new HashSet<>();
        while (readCurrentRs.next()) {
            currentSet.add(readCurrentRs.getString("response"));
        }


        if (intent.getResponses() != null) {
            // for each new bit of data
            for (String newValue : intent.getResponses()) {
                // mark it if it already existed
                if (!currentSet.remove(newValue)) {
                    // or add it if it didn't
                    transaction.getDatabaseCall().initialise("addIntentResponse", 4)
                            .add(devid).add(aiid).add(intent.getIntentName()).add(newValue).executeUpdate();
                }
            }
        }

        // delete the old ones
        for (String obsoleteValue : currentSet) {
            transaction.getDatabaseCall().initialise("deleteIntentResponse", 4)
                    .add(devid).add(aiid).add(intent.getIntentName()).add(obsoleteValue).executeUpdate();
        }
    }

    /***
     * Synchronise new intent variables with old ones in the database
     * @param devid
     * @param aiid
     * @param intent
     * @param transaction
     * @throws DatabaseException
     * @throws SQLException
     */
    private void updateIntentVariables(final UUID devid, final UUID aiid, final ApiIntent intent,
                                       final DatabaseTransaction transaction)
            throws DatabaseException, SQLException {

        // read the existing intent variables from the database
        ResultSet readCurrentRs = transaction.getDatabaseCall().initialise("getIntentVariables", 2)
                .add(aiid).add(intent.getIntentName()).executeQuery();

        // put them into a set
        HashMap<String, IntentVariable> currentSet = new HashMap<>();
        while (readCurrentRs.next()) {
            String uuidString = readCurrentRs.getString("dev_id");
            UUID devOwnerUUID = UUID.fromString(uuidString);
            IntentVariable old = new IntentVariable(
                    readCurrentRs.getString("entity_name"),
                    devOwnerUUID,
                    readCurrentRs.getBoolean("required"), readCurrentRs.getInt("n_prompts"),
                    readCurrentRs.getString("value"),
                    readCurrentRs.getInt("id"),
                    readCurrentRs.getBoolean("isPersistent"));
            currentSet.put(old.getEntityName(), old);
        }

        if (intent.getVariables() != null) {
            // for every variable in the new data ...
            for (IntentVariable newValue : intent.getVariables()) {
                // mark it as done if there was already one using that entity
                currentSet.remove(newValue.getEntityName());
                // and create or update it
                intentVariableCreateOrUpdate(transaction, devid, aiid, intent, newValue);
            }
        }

        // anything left over needs to be deleted
        for (IntentVariable obsoleteVar : currentSet.values()) {
            intentVariableDeleteOld(transaction, devid, aiid, intent, obsoleteVar);
        }

    }

    /***
     * Update an intent variable with new data and potential changes to prompts
     * @param transaction
     * @param devid
     * @param aiid
     * @param intent
     * @param intentVariable
     * @throws DatabaseException
     * @throws SQLException
     */
    private void intentVariableCreateOrUpdate(final DatabaseTransaction transaction, final UUID devid,
                                              final UUID aiid, final ApiIntent intent,
                                              final IntentVariable intentVariable)
            throws DatabaseException, SQLException {

        // generate the call params
        ResultSet updateVarRs = transaction.getDatabaseCall().initialise("addUpdateIntentVariable", 7)
                .add(devid).add(aiid).add(intent.getIntentName())
                .add(intentVariable.getEntityName())
                .add(intentVariable.isRequired()).add(intentVariable.getNumPrompts()).add(intentVariable.getValue())
                .executeQuery();

        // we are expecting some results; if not then something has gone very wrong
        if (!updateVarRs.next()) {
            throw new DatabaseException("unable to create/update intent variable");
        }

        // 1 is a create and 2 is an update (0 means that we failed)
        if (updateVarRs.getInt("update") < 1) {
            throw new DatabaseEntityException(intentVariable.getEntityName());
        }

        // we need to take note of the ID to update prompts against
        int varId = updateVarRs.getInt("affected_id");

        // what prompts do we have now?
        ResultSet readCurrentRs = transaction.getDatabaseCall().initialise("getIntentVariablePrompts", 2)
                .add(aiid).add(varId).executeQuery();
        // put them into a set
        HashSet<String> currentSet = new HashSet<>();
        while (readCurrentRs.next()) {
            currentSet.add(readCurrentRs.getString("prompt"));
        }

        if (intentVariable.getPrompts() != null) {
            // add any prompts that didn't exist before
            for (String newValue : intentVariable.getPrompts()) {
                if (!currentSet.remove(newValue)) {
                    transaction.getDatabaseCall().initialise("addIntentVariablePrompt", 4)
                            .add(devid).add(aiid).add(varId).add(newValue).executeUpdate();
                }
            }
        }

        // delete the prompts that we no longer need
        for (String obsoleteValue : currentSet) {
            transaction.getDatabaseCall().initialise("deleteIntentVariablePrompt", 4)
                    .add(devid).add(aiid).add(varId).add(obsoleteValue).executeUpdate();
        }
    }

    /***
     * Delete an intent variable that is no longer referenced by an intent
     * @param transaction
     * @param devid
     * @param aiid
     * @param intent
     * @param variable
     * @throws DatabaseException
     */
    private void intentVariableDeleteOld(final DatabaseTransaction transaction, final UUID devid, final UUID aiid,
                                         ApiIntent intent, IntentVariable variable) throws DatabaseException {
        transaction.getDatabaseCall().initialise("deleteIntentVariable", 3)
                .add(devid).add(aiid).add(variable.getId())
                .executeUpdate();
    }

    /***
     * This means that we tried to reference a non-existent entity in an intent
     */
    public static class DatabaseEntityException extends DatabaseException {

        public DatabaseEntityException(String entity) {
            super(entity);
        }
    }
}
