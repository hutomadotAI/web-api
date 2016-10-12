package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiMemoryToken;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.sub.AiIntegration;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    private static final String LOGFROM = "database";
    private final Logger logger;
    private final Provider<DatabaseCall> callProvider;

    @Inject
    public Database(final Logger logger, final Provider<DatabaseCall> callProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
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

    public boolean deleteDev(final String devid) throws DatabaseException {

        //TODO: make this a single stored procedure
        // first delete all the user's AIs
        try (DatabaseCall deleleAICall = this.callProvider.get()) {
            deleleAICall.initialise("deleteAllAIs", 1).add(devid);
            deleleAICall.executeUpdate();

            // then delete the user
            try (DatabaseCall deleteUserCall = this.callProvider.get()) {
                deleteUserCall.initialise("deleteUser", 1).add(devid);
                return deleteUserCall.executeUpdate() > 0;
            }
        }
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

    public List<ApiMemoryToken> getAllUserVariables(final String dev_id, final String aiid, final String uid) throws DatabaseException {
        try {
            return null; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ApiMemoryToken getUserVariable(final String dev_id, final String aiid, final String uid, final String variable) throws DatabaseException {
        try {
            return null; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean setUserVariable(final String dev_id, final String aiid, final String uid, final int expires_seconds, final int n_prompt,
                                   final String variable_type, final String variable_name, final String variable_value) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
        }

    }

    public boolean removeVariable(final String dev_id, final String aiid, final String uid, final String variable) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllUserVariables(final String dev_id, final String aiid, final String uid) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllAiVariables(final String dev_id, final String aiid) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (final Exception e) {
            throw new DatabaseException(e);
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

    public ApiIntent getIntent(String devid, UUID aiid, String intentName) throws DatabaseException {

        // cascading tries for DB call is temporary
        // and will be replaced by transactions very soon.
        // transactions will have a single cleanup for multiple db calls.
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntent", 3).add(devid).add(aiid).add(intentName);
            ResultSet rs = call.executeQuery();
            if (!rs.next()) {
                // not found
                return null;
            }
            // create the intent
            ApiIntent intent = new ApiIntent(rs.getString("name"), rs.getString("topic_in"), rs.getString("topic_out"));

            // get the user triggers
            try (DatabaseCall saysCall = this.callProvider.get()) {
                saysCall.initialise("getIntentUserSays", 3).add(devid).add(aiid).add(intentName);
                ResultSet saysRs = saysCall.executeQuery();
                while (saysRs.next()) {
                    intent.addUserSays(saysRs.getString("says"));
                }
            }

            // get the list of responses
            try (DatabaseCall intentResponseCall = this.callProvider.get()) {
                intentResponseCall.initialise("getIntentResponses", 3).add(devid).add(aiid).add(intentName);
                ResultSet intentResponseRs = intentResponseCall.executeQuery();
                while (intentResponseRs.next()) {
                    intent.addResponse(intentResponseRs.getString("response"));
                }
            }

            // get each intent variable
            try (DatabaseCall varCall = this.callProvider.get()) {
                varCall.initialise("getIntentVariables", 3).add(devid).add(aiid).add(intentName);
                ResultSet varRs = varCall.executeQuery();
                while (varRs.next()) {
                    int varID = varRs.getInt("id");
                    IntentVariable variable = new IntentVariable(
                            varRs.getString("entity_name"), varRs.getBoolean("required"), varRs.getInt("n_prompts"), varRs.getString("value"));

                    // for each variable get all its prompts
                    try (DatabaseCall promptCall = this.callProvider.get()) {
                        promptCall.initialise("getIntentVariablePrompts", 3).add(devid).add(aiid).add(varID);
                        ResultSet promptRs = promptCall.executeQuery();
                        while (promptRs.next()) {
                            variable.addPrompt(promptRs.getString("prompt"));
                        }
                    }
                    intent.addVariable(variable);
                }
            }
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

    public static class DatabaseException extends Exception {
        public DatabaseException(final Throwable cause) {
            super(cause);
        }
    }
}
