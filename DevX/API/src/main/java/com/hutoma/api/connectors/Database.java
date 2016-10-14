package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.sub.AiIntegration;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    private static final String LOGFROM = "database";
    private final Logger logger;
    private final Provider<DatabaseCall> callProvider;
    Provider<DatabaseTransaction> transactionProvider;

    @Inject
    public Database(Logger logger, Provider<DatabaseCall> callProvider, Provider<DatabaseTransaction> transactionProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
        this.transactionProvider = transactionProvider;
    }

    private static MemoryIntent loadMemoryIntent(final ResultSet rs, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try {
            List<MemoryVariable> variables = jsonSerializer.deserializeList(rs.getString("variables"));
            return new MemoryIntent(rs.getString("intentName"),
                    UUID.fromString(rs.getString("aiid")),
                    UUID.fromString(rs.getString("chatId")),
                    variables);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean createDev(final String username, final String email, final String password, final String passwordSalt, final String first_name, final String last_name, final String dev_token, final int plan_id, final String dev_id, final String client_token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addUser", 10).add(username).add(email).add(password).add(passwordSalt).add(first_name).add(last_name).add(dev_token).add(plan_id).add(dev_id).add(client_token);
            return call.executeUpdate() > 0;
        }
    }

    /***
     * Delete a developer from the database and remove the developer's AIs
     * @param devid
     * @return true if the user was found and deleted, false if no user was found
     * @throws DatabaseException
     */
    public boolean deleteDev(final String devid) throws DatabaseException {

        int updateCount = 0;
        // start a transaction
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // delete the user's AIs
            transaction.getDatabaseCall().initialise("deleteAllAIs", 1).add(devid).executeUpdate();
            // delete the user
            updateCount = transaction.getDatabaseCall().initialise("deleteUser", 1).add(devid).executeUpdate();
            // if all goes well, commit
            transaction.commit();
        }
        return updateCount > 0;
    }

    public boolean createAI(final UUID aiid, final String name, final String description, final String devid,
                            final boolean is_private, final double deep_learning_error, final int deep_learning_status,
                            final int shallow_learning_status, final TrainingStatus.trainingStatus status, final String client_token, final String trainingFile) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addAI", 11)
                    .add(aiid).add(name).add(description).add(devid).add(is_private)
                    .add(deep_learning_error).add(deep_learning_status).add(shallow_learning_status)
                    .add(status).add(client_token).add(trainingFile);
            return call.executeUpdate() > 0;
        }
    }

    public ArrayList<ApiAi> getAllAIs(final String devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAIs", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            final ArrayList<ApiAi> res = new ArrayList<>();
            try {
                while (rs.next()) {
                    final ApiAi ai = new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                            new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                            null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
                    res.add(ai);
                }
                return res;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public ApiAi getAI(final String devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAI_v1", 2).add(devid).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                            new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                            null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public String getAiTrainingFile(final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAiTrainingFile", 1).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("ai_trainingfile");
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deleteAi(final String devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteAI_v1", 2).add(devid).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public ArrayList<AiDomain> getAiDomainList() throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDomains", 0);
            final ResultSet rs = call.executeQuery();
            try {
                final ArrayList<AiDomain> res = new ArrayList<>();
                while (rs.next()) {
                    res.add(new AiDomain(rs.getString("dom_id"), rs.getString("name"), rs.getString("description"),
                            rs.getString("icon"), rs.getString("color"), rs.getBoolean("available")));
                }
                return res;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean isNeuralNetworkServerActive(final String dev_id, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAiActive", 2).add(aiid).add(dev_id);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return (1 == rs.getInt("NNActive"));
                }
                return false;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public long insertNeuralNetworkQuestion(final String dev_id, final UUID chatId, final UUID aiid, final String q) throws DatabaseException {

        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("insertQuestion", 4).add(dev_id).add(chatId).add(aiid).add(q);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return -1;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public String getAnswer(final long qid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAnswer", 1).add(qid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("answer");
                }
                return "";
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateAiTrainingFile(final UUID aiUUID, final String trainingData) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateTrainingData", 2).add(aiUUID).add(trainingData);
            return call.executeUpdate() > 0;
        }
    }

    public RateLimitStatus checkRateLimit(final String dev_id, final String rateKey, final double burst, final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rate_limit_check", 4).add(dev_id).add(rateKey).add(burst).add(frequency);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(rs.getBoolean("rate_limit"), rs.getFloat("tokens"));
                }
                throw new DatabaseException(new Exception("stored proc should have returned a row but it returned none"));
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    /***
     * Temporarily fix build break
     * @return
     */
    public List<AiIntegration> getAiIntegrationList() throws DatabaseException {
        throw new DatabaseException(new Exception("getAiIntegrationList unimplemented"));
    }

    public List<String> getEntities(final String devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getEntities", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            try {
                final ArrayList<String> entities = new ArrayList<>();
                while (rs.next()) {
                    entities.add(rs.getString("name"));
                }
                return entities;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public MemoryIntent getMemoryIntent(final String intentName, final UUID aiid, UUID chatId,
                                        final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getMemoryIntent", 3).add(intentName).add(aiid).add(chatId);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return loadMemoryIntent(rs, jsonSerializer);
                }
                return null;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<MemoryIntent> getMemoryIntentsForChat(final UUID aiid, final UUID chatId, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getMemoryIntentsForChat", 2).add(aiid).add(chatId);
            ResultSet rs = call.executeQuery();
            List<MemoryIntent> intents = new ArrayList<>();
            try {
                while (rs.next()) {
                    intents.add(loadMemoryIntent(rs, jsonSerializer));
                }
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
            return intents;
        }
    }

    public ApiEntity getEntity(final String devid, final String entityName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getEntityValues", 2).add(devid).add(entityName);
            final ResultSet rs = call.executeQuery();
            try {
                final ArrayList<String> entityValues = new ArrayList<>();
                while (rs.next()) {
                    entityValues.add(rs.getString("value"));
                }
                return new ApiEntity(entityName, entityValues);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<String> getIntents(String devid, UUID aiid) throws DatabaseException {
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
     * @param devid owner dev
     * @param aiid the aiid that owns the intent
     * @param intentName
     * @return an intent
     * @throws DatabaseException if things go wrong
     */
    public ApiIntent getIntent(String devid, UUID aiid, String intentName) throws DatabaseException {

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            ResultSet rs = transaction.getDatabaseCall().initialise("getIntent", 3).add(devid).add(aiid).add(intentName).executeQuery();
            if (!rs.next()) {
                // the intent was not found at all
                return null;
            }

            // build the intent
            ApiIntent intent = new ApiIntent(rs.getString("name"), rs.getString("topic_in"), rs.getString("topic_out"));

            // get the user triggers
            ResultSet saysRs = transaction.getDatabaseCall().initialise("getIntentUserSays", 3)
                    .add(devid).add(aiid).add(intentName).executeQuery();
            while (saysRs.next()) {
                intent.addUserSays(saysRs.getString("says"));
            }

            // get the list of responses
            ResultSet intentResponseRs = transaction.getDatabaseCall().initialise("getIntentResponses", 3)
                    .add(devid).add(aiid).add(intentName).executeQuery();
            while (intentResponseRs.next()) {
                intent.addResponse(intentResponseRs.getString("response"));
            }

            // get each intent variable
            ResultSet varRs = transaction.getDatabaseCall().initialise("getIntentVariables", 3)
                    .add(devid).add(aiid).add(intentName).executeQuery();
            while (varRs.next()) {
                int varID = varRs.getInt("id");
                IntentVariable variable = new IntentVariable(
                        varRs.getString("entity_name"), varRs.getBoolean("required"), varRs.getInt("n_prompts"), varRs.getString("value"));

                // for each variable get all its prompts
                ResultSet promptRs = transaction.getDatabaseCall().initialise("getIntentVariablePrompts", 3)
                        .add(devid).add(aiid).add(varID).executeQuery();
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

    public boolean updateMemoryIntent(final MemoryIntent intent, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            String variables = jsonSerializer.serialize(intent.getVariables());
            call.initialise("updateMemoryIntent", 4)
                    .add(intent.getName())
                    .add(intent.getAiid())
                    .add(intent.getChatId())
                    .add(variables);
            return call.executeUpdate() > 0;
        }
    }

    public boolean deleteAllMemoryIntents(final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteAllMemoryIntents", 1).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public boolean setRnnStatus(String devid, UUID aiid, int status) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            int rowsChanged = call.initialise("setRnnStatus", 3).add(status).add(devid).add(aiid).executeUpdate();
            return rowsChanged > 0;
        }
    }

    public static class DatabaseException extends Exception {

        public DatabaseException(final Throwable cause) {
            super(cause);
        }
    }
}
