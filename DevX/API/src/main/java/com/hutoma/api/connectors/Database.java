package com.hutoma.api.connectors;

import com.google.gson.internal.LinkedTreeMap;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiIntegration;
import com.hutoma.api.containers.sub.AiStore;
import com.hutoma.api.containers.sub.ChatRequestStatus;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.MeshVariable;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.apache.commons.lang.LocaleUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    protected static final String LOGFROM = "database";
    protected final Logger logger;
    protected final Provider<DatabaseCall> callProvider;
    protected Provider<DatabaseTransaction> transactionProvider;
    private String aiid;
    private String name;
    private String description;
    private String licenceType;
    private Float licenceFee;
    private Float rating;
    private int numberOfActivations;
    private boolean isBanned;
    private String iconPath;
    private String widgetColor;

    @Inject
    public Database(Logger logger, Provider<DatabaseCall> callProvider,
                    Provider<DatabaseTransaction> transactionProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
        this.transactionProvider = transactionProvider;
    }

    private static MemoryIntent loadMemoryIntent(final ResultSet rs, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try {
            List<LinkedTreeMap<String, Object>> list = jsonSerializer.deserializeList(rs.getString("variables"));
            List<MemoryVariable> variables = new ArrayList<>();
            for (LinkedTreeMap<String, Object> e : list) {
                @SuppressWarnings("unchecked")
                MemoryVariable memoryVariable = new MemoryVariable(
                        e.get("name").toString(),
                        e.containsKey("currentValue") ? e.get("currentValue").toString() : null,
                        (boolean) e.get("isMandatory"),
                        (List<String>) e.get("entityKeys"),
                        (List<String>) e.get("prompts"),
                        (int) Math.round((double) e.get("timesToPrompt")),
                        (int) Math.round((double) e.get("timesPrompted")));
                variables.add(memoryVariable);
            }
            return new MemoryIntent(rs.getString("name"),
                    UUID.fromString(rs.getString("aiid")),
                    UUID.fromString(rs.getString("chatId")),
                    variables,
                    rs.getBoolean("isFulfilled"));
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    private static TrainingStatus getTrainingStatusValue(final String value)
            throws DatabaseException {
        TrainingStatus trainingStatus = TrainingStatus.forValue(value);
        if (trainingStatus == null) {
            throw new DatabaseException("ai_status field does not contain an expected status");
        }
        return trainingStatus;
    }

    public boolean createDev(final String username, final String email, final String password,
                             final String passwordSalt, final String firstName, final String lastName,
                             final String devToken, final int planId, final String devId, final String clientToken)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addUser", 10).add(username).add(email).add(password).add(passwordSalt)
                    .add(firstName).add(lastName).add(devToken).add(planId).add(devId).add(clientToken);
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
                            final boolean isPrivate, final double deepLearningError, final int deepLearningStatus,
                            final int shallowLearningStatus, final TrainingStatus status, final String clientToken,
                            final String trainingFile, final Locale language, final String timezoneString,
                            final double confidence, final int personality, final int voice)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addAI_v1", 16)
                    .add(aiid)
                    .add(name)
                    .add(description)
                    .add(devid)
                    .add(isPrivate)
                    .add(deepLearningError)
                    .add(deepLearningStatus)
                    .add(shallowLearningStatus)
                    .add(status.value())
                    .add(clientToken)
                    .add(trainingFile)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateAI(final String devId, final UUID aiid, final String description, final boolean isPrivate,
                            final Locale language, final String timezoneString, final double confidence,
                            final int personality, final int voice)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAI_v1", 9)
                    .add(aiid)
                    .add(description)
                    .add(devId)
                    .add(isPrivate)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateAIStatus(final String devId, final UUID aiid, final TrainingStatus status)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAIstatus", 3)
                    .add(aiid)
                    .add(devId)
                    .add(status.value());
            return call.executeUpdate() > 0;
        }
    }

    public List<ApiAi> getAllAIs(final String devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAIs", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            final ArrayList<ApiAi> res = new ArrayList<>();
            try {
                while (rs.next()) {
                    res.add(getAiFromResultset(rs));
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
                    return getAiFromResultset(rs);
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

    public String getDevToken(final String devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevTokenFromDevID", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("dev_token");
                }
                return "";
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

    public ArrayList<AiStore> getBotStoreList() throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotsInStore", 0);
            final ResultSet rs = call.executeQuery();
            try {
                final ArrayList<AiStore> res = new ArrayList<>();
                while (rs.next()) {
                    res.add(new AiStore(rs.getString("dom_id"), rs.getString("name"), rs.getString("description"),
                            rs.getString("icon"), rs.getString("color"), rs.getBoolean("published")));
                }
                return res;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean isNeuralNetworkServerActive(final String devId, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAiActive", 2).add(aiid).add(devId);
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

    public ChatRequestStatus insertNeuralNetworkQuestion(final String devId, final UUID chatId, final UUID aiid,
                                                         final String question) throws DatabaseException {

        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("insertQuestion_v1", 4).add(devId).add(chatId).add(aiid).add(question);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    TrainingStatus trainingStatus = TrainingStatus.forValue(rs.getString("ai_status"));
                    return new ChatRequestStatus(rs.getLong("id"),
                            trainingStatus,
                            rs.getBoolean("failed_status"));
                }
                return new ChatRequestStatus();
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

    public RateLimitStatus checkRateLimit(final String devId, final String rateKey, final double burst,
                                          final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rate_limit_check", 4).add(devId).add(rateKey).add(burst).add(frequency);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(rs.getBoolean("rate_limit"), rs.getFloat("tokens"));
                }
                throw new DatabaseException(
                        new Exception("stored proc should have returned a row but it returned none"));
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

    public List<MemoryIntent> getMemoryIntentsForChat(final UUID aiid, final UUID chatId,
                                                      final JsonSerializer jsonSerializer)
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

    public boolean updateMemoryIntent(final MemoryIntent intent, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            String variables = jsonSerializer.serialize(intent.getVariables());
            call.initialise("updateMemoryIntent", 5)
                    .add(intent.getName())
                    .add(intent.getAiid())
                    .add(intent.getChatId())
                    .add(variables)
                    .add(intent.isFulfilled());
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

    /***
     * AI Mesh Calls
     */

    public List<MeshVariable> getMesh(final String devId, final String aiid)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getMesh", 2).add(devId).add(aiid);
            ResultSet rs = call.executeQuery();
            List<MeshVariable> mesh = new ArrayList<>();
            try {
                while (rs.next()) {
                    mesh.add(new MeshVariable(
                            rs.getString("aiid"),
                            rs.getString("aiid_mesh"),
                            rs.getString("ai_name"),
                            rs.getString("ai_description"),
                            rs.getString("licenceType"),
                            rs.getFloat("licenceFee"),
                            rs.getFloat("rating"),
                            rs.getInt("numberOfActivations"),
                            rs.getBoolean("isBanned"),
                            rs.getString("iconPath"),
                            rs.getString("color")
                    ));
                }
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
            return mesh;
        }
    }


    public boolean addMesh(final String devId, final String aiid, final String aiidMesh)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addMesh", 3).add(devId).add(aiid).add(aiidMesh);
            return call.executeUpdate() > 0;
        }
    }

    public boolean deleteSingleMesh(final String devId, final String aiid, final String aiidMesh)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteMesh", 3).add(devId).add(aiid).add(aiidMesh);
            return call.executeUpdate() > 0;
        }
    }

    public boolean deleteAllMesh(final String devId, final String aiid)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteAllMesh", 2).add(devId).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    private ApiAi getAiFromResultset(final ResultSet rs) throws SQLException, DatabaseException {
        String localeString = rs.getString("ai_language");
        String timezoneString = rs.getString("ai_timezone");
        return new ApiAi(rs.getString("aiid"),
                rs.getString("client_token"),
                rs.getString("ai_name"),
                rs.getString("ai_description"),
                new DateTime(rs.getDate("created_on")),
                rs.getBoolean("is_private"),
                rs.getDouble("deep_learning_error"),
                null /*training_debug_info*/,
                rs.getString("deep_learning_status"),
                getTrainingStatusValue(rs.getString("ai_status")),
                null /*training file*/,
                rs.getInt("ai_personality"),
                rs.getDouble("ai_confidence"),
                rs.getInt("ai_voice"),
                // Java, being funny, can't follow rfc5646 so we need to replace the separator
                localeString == null ? null : LocaleUtils.toLocale(localeString.replace("-", "_")),
                timezoneString);
    }

    /***
     * General exception for database errors
     */
    public static class DatabaseException extends Exception {
        public DatabaseException(final Throwable cause) {
            super(cause);
        }

        public DatabaseException(final String message) {
            super(message);
        }
    }

    /***
     * Happens when we violate a constraint, most commonly trying to create a duplicate in a unique field
     */
    public static class DatabaseIntegrityViolationException extends DatabaseException {
        public DatabaseIntegrityViolationException(Throwable cause) {
            super(cause);
        }
    }
}
