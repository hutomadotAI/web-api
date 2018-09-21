package com.hutoma.api.connectors.db;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.sub.Entity;
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

    @Inject
    public DatabaseEntitiesIntents(final ILogger logger,
                                   final Provider<DatabaseCall> callProvider,
                                   final Provider<DatabaseTransaction> transactionProvider,
                                   final JsonSerializer serializer,
                                   final FeatureToggler featureToggler) {
        super(logger, callProvider, transactionProvider, featureToggler);
        this.serializer = serializer;
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
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            try {
                ApiEntity result = null;
                ResultSet rs = transaction.getDatabaseCall().initialise("getEntityDetails", 2)
                        .add(devid).add(entityName).executeQuery();
                if (rs.next()) {
                    boolean isSystem = rs.getBoolean("isSystem");
                    final ArrayList<String> entityValues = new ArrayList<>();
                    // only custom entities have values as system entities are handled externally
                    if (!isSystem) {
                        ResultSet valuesRs = transaction.getDatabaseCall().initialise("getEntityValues", 2)
                                .add(devid).add(entityName).executeQuery();
                        while (valuesRs.next()) {
                            entityValues.add(valuesRs.getString("value"));
                        }
                    }
                    result = new ApiEntity(entityName, devid, entityValues, isSystem);
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

            return (ApiIntent) this.serializer.deserialize(rs.getString("intent_json"), ApiIntent.class);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    /**
     * Writes (or updates) and entity
     * @param devid         the developer id
     * @param entityOldName the entity's old name
     * @param entity        the entity's new name
     * @throws DatabaseException if something goes wrong
     */
    public void writeEntity(final UUID devid, final String entityOldName, final ApiEntity entity)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            this.writeEntity(devid, entityOldName, entity, transaction);
            transaction.commit();
        }
    }

    /**
     * Writes (or updates) and entity
     * @param devid         the developer id
     * @param entityOldName the entity's old name
     * @param entity        the entity's new name
     * @param transaction   the transaction it should be enrolled in
     * @throws DatabaseException if something goes wrong
     */
    public void writeEntity(final UUID devid, final String entityOldName, final ApiEntity entity,
                            final DatabaseTransaction transaction)
            throws DatabaseException {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction");
        }
        try {
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

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public OptionalInt getEntityIdForDev(UUID devid, String entityName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            ResultSet rs;
            try {
                call.initialise("getEntityIdForDev", 2).add(devid).add(entityName);
                rs = call.executeQuery();
                OptionalInt entityId = OptionalInt.empty();
                if (rs.next()) {
                    entityId = OptionalInt.of(rs.getInt("id"));
                }
                return entityId;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deleteEntity(UUID devid, int entityId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            int rowCount = call.initialise("deleteEntity", 2).add(devid).add(entityId).executeUpdate();
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
}
