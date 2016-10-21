package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.sub.AiIntegration;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.apache.commons.lang.LocaleUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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

    private static TrainingStatus getTrainingStatusValue(final String value)
            throws DatabaseException {
        TrainingStatus trainingStatus = TrainingStatus.forValue(value);
        if (trainingStatus == null) {
            throw new DatabaseException("ai_status field does not contain an expected status");
        }
        return trainingStatus;
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
                            final int shallow_learning_status, final TrainingStatus status, final String client_token,
                            final String trainingFile, final Locale language, final String timezoneString,
                            final double confidence, final int personality, final int voice)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addAI_v1", 16)
                    .add(aiid)
                    .add(name)
                    .add(description)
                    .add(devid)
                    .add(is_private)
                    .add(deep_learning_error)
                    .add(deep_learning_status)
                    .add(shallow_learning_status)
                    .add(status.value())
                    .add(client_token)
                    .add(trainingFile)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateAI(final String devId, final UUID aiid, final String description, final boolean is_private,
                            final Locale language, final String timezoneString, final double confidence,
                            final int personality, final int voice)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAI_v1", 9)
                    .add(aiid)
                    .add(description)
                    .add(devId)
                    .add(is_private)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice);
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
                timezoneString == null ? null : TimeZone.getTimeZone(timezoneString));
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
