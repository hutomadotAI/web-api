package com.hutoma.api.connectors.db;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.EntityValueType;
import com.hutoma.api.logging.ILogger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by David MG on 20/10/2016.
 */
public class DatabaseEntitiesIntents extends DatabaseAI {

    private final JsonSerializer serializer;
    private final FeatureToggler featureToggler;
    private final String emptyAiid = "";

    @Inject
    public DatabaseEntitiesIntents(final ILogger logger,
                                   final Provider<DatabaseCall> callProvider,
                                   final Provider<DatabaseTransaction> transactionProvider,
                                   final JsonSerializer serializer,
                                   final FeatureToggler featureToggler) {
        super(logger, callProvider, transactionProvider);
        this.serializer = serializer;
        this.featureToggler = featureToggler;
    }

    public List<Entity> getEntities(final UUID devid, final UUID aiid) throws DatabaseException {
        String aiidString = aiid.toString();

        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getEntities", 2).add(devid).add(aiidString);
            final ResultSet rs = call.executeQuery();
            try {
                List<Entity> entities = new ArrayList<>();
                while (rs.next()) {
                    Entity entity = new Entity(
                            rs.getString("name"),
                            rs.getBoolean("isSystem"),
                            EntityValueType.fromString(rs.getString("value_type")));
                    entities.add(entity);
                }
                return entities;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<ApiIntent> getAllIntents(final UUID devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAllIntentsForDev", 1).add(devid);
            ResultSet rs = call.executeQuery();
            List<ApiIntent> intents = new ArrayList<>();
            while (rs.next()) {
                ApiIntent intent = getIntentFromRecordset(rs);
                if (intent != null) {
                    intents.add(intent);
                }
            }
            return intents;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public ApiEntity getEntity(final UUID devid, final String entityName, final UUID aiid) throws DatabaseException {
        String aiidString = aiid.toString();

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            try {
                ApiEntity result = null;
                ResultSet rs = transaction.getDatabaseCall().initialise("getEntityDetails", 3)
                        .add(devid).add(entityName).add(aiidString).executeQuery();
                if (rs.next()) {
                    boolean isSystem = rs.getBoolean("isSystem");
                    EntityValueType valueType = EntityValueType.fromString(rs.getString("value_type"));
                    final ArrayList<String> entityValues = new ArrayList<>();
                    // only custom entities have values as system entities are handled externally
                    if (!isSystem) {
                        ResultSet valuesRs = transaction.getDatabaseCall().initialise("getEntityValues", 3)
                                .add(devid).add(entityName).add(aiidString).executeQuery();
                        while (valuesRs.next()) {
                            entityValues.add(valuesRs.getString("value"));
                        }
                    }
                    result = new ApiEntity(entityName, devid, entityValues, isSystem, valueType);
                }
                transaction.commit();
                return result;
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

    public ApiIntentList getIntentsDetails(final UUID devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntents", 2).add(devid).add(aiid);
            ResultSet rs = call.executeQuery();
            ArrayList<String> intentNames = new ArrayList<>();
            ArrayList<ApiIntent> intents = new ArrayList<>();

            while (rs.next()) {
                String intentName = rs.getString("name");
                intentNames.add(intentName);
                intents.add(this.getIntent(aiid, intentName));
            }
            return new ApiIntentList(aiid, intentNames, intents);
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public int getEntityValuesCountForDevExcludingEntity(final UUID devId, final String entityName, final UUID aiid)
            throws DatabaseException {
        String aiidString = aiid.toString();

        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getEntityValuesCountForDevExcludingEntity", 3)
                    .add(devId)
                    .add(entityName)
                    .add(aiidString);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt("COUNT");
            }
            return 0;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    /***
     * Gets a fully populated intent object
     * including intent, usersays, variables and prompts
     * @param aiid the aiid that owns the intent
     * @param intentName the intent name
     * @return an intent
     * @throws DatabaseException if things go wrong
     */
    public ApiIntent getIntent(final UUID aiid, final String intentName) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            ApiIntent intent = getIntent(aiid, intentName, transaction);
            transaction.commit();
            return intent;
        }
    }

    /***
     * Gets a fully populated intent object
     * including intent, usersays, variables and prompts
     * @param aiid the aiid that owns the intent
     * @param intentName the intent name
     * @param transaction the transaction it should be enrolled in
     * @return an intent
     * @throws DatabaseException if things go wrong
     */
    public ApiIntent getIntent(final UUID aiid, final String intentName, final DatabaseTransaction transaction)
            throws DatabaseException {

        if (transaction == null) {
            throw new IllegalArgumentException("transaction");
        }

        try {
            ResultSet rs = transaction.getDatabaseCall().initialise("getIntent", 2)
                    .add(aiid)
                    .add(intentName)
                    .executeQuery();
            if (!rs.next()) {
                // the intent was not found at all
                return null;
            }

            return getIntentFromRecordset(rs);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    /**
     * Writes (or updates) and entity
     *
     * @param devid         the developer id
     * @param entityOldName the entity's old name
     * @param entity        the entity's new name
     * @throws DatabaseException if something goes wrong
     */
    public void writeEntity(final UUID devid, final String entityOldName, final ApiEntity entity, final UUID aiid)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            this.writeEntity(devid, entityOldName, entity, transaction, aiid);
            transaction.commit();
        }
    }

    /**
     * Writes (or updates) and entity
     *
     * @param devid         the developer id
     * @param entityOldName the entity's old name
     * @param entity        the entity's new name
     * @param transaction   the transaction it should be enrolled in
     * @throws DatabaseException if something goes wrong
     */
    public void writeEntity(final UUID devid, final String entityOldName, final ApiEntity entity,
                            final DatabaseTransaction transaction, final UUID aiid)
            throws DatabaseException {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction");
        }

        String aiidString = aiid.toString();
        try {
            // add or update the entity
            transaction.getDatabaseCall().initialise("addUpdateEntity", 5)
                    .add(devid)
                    .add(entityOldName)
                    .add(entity.getEntityName())
                    .add(entity.getEntityValueType().name())
                    .add(aiidString)
                    .executeUpdate();

            // read the entity's values
            ResultSet valuesRs = transaction.getDatabaseCall().initialise("getEntityValues", 3)
                    .add(devid).add(entity.getEntityName()).add(aiidString).executeQuery();

            // put them into a set
            HashSet<String> currentValues = new HashSet<>();
            while (valuesRs.next()) {
                currentValues.add(valuesRs.getString("value"));
            }

            // Delete all the old entity values.
            for (String obsoleteEntityValue : currentValues) {
                transaction.getDatabaseCall().initialise("deleteEntityValue", 4)
                        .add(devid)
                        .add(entity.getEntityName())
                        .add(obsoleteEntityValue)
                        .add(aiidString)
                        .executeUpdate();
            }

            // Add all the new entity values.
            if (entity.getEntityValueList() != null) {
                for (String entityValue : entity.getEntityValueList()) {
                    transaction.getDatabaseCall().initialise("addEntityValue", 4)
                            .add(devid)
                            .add(entity.getEntityName())
                            .add(entityValue)
                            .add(aiidString)
                            .executeUpdate();
                }
            }


        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Deletes a specific entity for a user.
     * @param devid The devid the entity belongs to.
     * @param aiid The aiid the entity belongs to.
     * @param name The name of the entity.
     * @return The number of entities deleted.
     * @throws DatabaseException if something goes wrong
     */
    public boolean deleteEntityByName(final UUID devid, final UUID aiid, final String name,
                                      DatabaseTransaction transaction) throws DatabaseException {
        String aiidString = aiid.toString();
        boolean createTrans = transaction == null;
        if (createTrans) {
            transaction = this.transactionProvider.get();
        }

        int rowCount = transaction.getDatabaseCall()
                .initialise("deleteEntityByName", 3)
                .add(devid)
                .add(aiidString)
                .add(name)
                .executeUpdate();

        if (createTrans) {
            transaction.commit();
        }
        return rowCount > 0;
    }

    /***
     * Add a new complete intent to the database,
     * or update an intent previously called 'intentName' to the new intent.
     * If intentName is different to the one specified in the intent then this will rename the intent.
     * @param devid owner id
     * @param aiid owner aiid
     * @param intentName the old name if we are renaming the intent
     * @param intent the new data
     * @param transaction the transaction to be enrolled in
     * @throws DatabaseException if something goes wrong
     */
    public int writeIntent(final UUID devid, final UUID aiid, final String intentName, final ApiIntent intent,
                           final DatabaseTransaction transaction)
            throws DatabaseException {

        if (transaction == null) {
            throw new IllegalArgumentException("transaction");
        }

        intent.setLastUpdated(DateTime.now(DateTimeZone.UTC));
        if (intent.getResponses() == null) {
            intent.setResponses(new ArrayList<>());
        }
        if (intent.getUserSays() == null) {
            intent.setUserSays(new ArrayList<>());
        }
        if (intent.getContextIn() == null) {
            intent.setContextIn(new HashMap<>());
        }
        if (intent.getContextOut() == null) {
            intent.setContextOut(new HashMap<>());
        }
        if (intent.getConditionsIn() == null) {
            intent.setConditionsIn(new ArrayList<>());
        }
        if (intent.getIntentOutConditionals() == null) {
            intent.setIntentOutConditionals(new ArrayList<>());
        }

        // add or update the intent
        int rowcount = transaction.getDatabaseCall().initialise("addUpdateIntent", 5)
                .add(devid)
                .add(aiid)
                .add(intentName)
                .add(intent.getIntentName())
                .add(this.serializer.serialize(intent))
                .executeUpdate();

        if (rowcount != 1 && rowcount != 2) { // insert=1, update=2
            throw new DatabaseException("Failed to add/update intent");
        }

        return rowcount;
    }

    /***
     * Deletes an intent and all dependent objects by DB cascade delete
     * @param devid
     * @param aiid
     * @param intentName
     * @return whether the intent was successfully deleted or not
     * @throws DatabaseException
     */
    public boolean deleteIntent(final UUID devid, final UUID aiid, final String intentName) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            boolean retValue = deleteIntent(devid, aiid, intentName, transaction);
            transaction.commit();
            return retValue;
        }
    }

    /***
     * Deletes an intent and all dependent objects by DB cascade delete
     * @param devid
     * @param aiid
     * @param intentName
     * @return whether the intent was successfully deleted or not
     * @throws DatabaseException
     */
    public boolean deleteIntent(final UUID devid, final UUID aiid, final String intentName,
                                final DatabaseTransaction transaction)
            throws DatabaseException {
        int rowCount = transaction.getDatabaseCall().initialise("deleteIntent", 3)
                .add(devid)
                .add(aiid)
                .add(intentName)
                .executeUpdate();
        return rowCount > 0;
    }

    public void resetIntentsStateForAi(final UUID devId, final UUID aiid) throws DatabaseException {

        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("resetChatStatesForAi", 2).add(devId).add(aiid);
            call.executeUpdate();
        }
    }

    /***
     * This means that we tried to reference a non-existent entity in an intent
     */
    public static class DatabaseEntityException extends DatabaseException {

        DatabaseEntityException(String entity) {
            super(entity);
        }
    }

    private ApiIntent getIntentFromRecordset(final ResultSet rs) throws SQLException {
        ApiIntent intent = (ApiIntent) this.serializer.deserialize(
                rs.getString("intent_json"), ApiIntent.class);
        intent.setAiid(UUID.fromString(rs.getString("aiid")));
        return intent;
    }
}
