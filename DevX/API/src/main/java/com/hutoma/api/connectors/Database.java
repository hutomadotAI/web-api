package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.*;

import org.apache.commons.lang.LocaleUtils;
import org.joda.time.DateTime;

import java.io.InputStream;
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
    protected final ILogger logger;
    protected final Provider<DatabaseCall> callProvider;
    protected Provider<DatabaseTransaction> transactionProvider;

    @Inject
    public Database(ILogger logger, Provider<DatabaseCall> callProvider,
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

    /***
     * Try to deserialize json to create a valid BackendStatus
     * or return a blank BackendStatus if the json field was null or empty
     * @param statusJson json string
     * @param jsonSerializer serialiser
     * @return valid BackendStatus object
     * @throws DatabaseException if there was data it did not parse correctly
     */
    private static BackendStatus getBackendStatus(final String statusJson, JsonSerializer jsonSerializer)
            throws DatabaseException {
        BackendStatus backendStatus = null;
        // try to deserialize
        if (statusJson != null && !statusJson.isEmpty()) {
            try {
                backendStatus = (BackendStatus) jsonSerializer.deserialize(statusJson, BackendStatus.class);
            } catch (JsonParseException jpe) {
                throw new DatabaseException("Error parsing JSON in BackendStatus field");
            }
        }
        // if the field was empty then use an empty structure
        return (backendStatus == null) ? new BackendStatus() : backendStatus;
    }

    /***
     * Reports "summary status" for both back-end servers by taking the one that is furthest behind.
     * @param backendStatus
     * @return
     */
    private static TrainingStatus getSummaryTrainingStatus(final BackendStatus backendStatus) {
        TrainingStatus wnetStatus = backendStatus.getEngineStatus("wnet").getTrainingStatus();
        TrainingStatus rnnStatus = backendStatus.getEngineStatus("rnn").getTrainingStatus();

        if ((wnetStatus == TrainingStatus.AI_UNDEFINED) || (rnnStatus == TrainingStatus.AI_UNDEFINED)) {
            return TrainingStatus.AI_UNDEFINED;
        }
        if ((wnetStatus == TrainingStatus.AI_ERROR) || (rnnStatus == TrainingStatus.AI_ERROR)) {
            return TrainingStatus.AI_ERROR;
        }
        if ((wnetStatus == TrainingStatus.AI_READY_TO_TRAIN) || (rnnStatus == TrainingStatus.AI_READY_TO_TRAIN)) {
            return TrainingStatus.AI_READY_TO_TRAIN;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING_QUEUED) || (rnnStatus == TrainingStatus.AI_TRAINING_QUEUED)) {
            return TrainingStatus.AI_TRAINING_QUEUED;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING_STOPPED) || (rnnStatus == TrainingStatus.AI_TRAINING_STOPPED)) {
            return TrainingStatus.AI_TRAINING_STOPPED;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING) || (rnnStatus == TrainingStatus.AI_TRAINING)) {
            return TrainingStatus.AI_TRAINING;
        }
        return TrainingStatus.AI_TRAINING_COMPLETE;
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

    /**
     * Gets the developer plan for the given developer Id.
     * @param devId the developer id
     * @return the plan, or null if there is no developer Id or not plan associated to it
     * @throws DatabaseException database exception
     */
    public DevPlan getDevPlan(final String devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevPlan", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new DevPlan(rs.getInt("maxai"), rs.getInt("monthlycalls"), rs.getLong("maxmem"),
                            rs.getInt("maxtraining"));
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
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

    /**
     * Gets the developer info.
     * @param devId the developer id
     * @return the developer info
     * @throws DatabaseException
     */
    public DeveloperInfo getDeveloperInfo(final String devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDeveloperInfo", 1).add(devId);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return new DeveloperInfo(
                        rs.getString("dev_id"),
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("post_code"),
                        rs.getString("city"),
                        rs.getString("country"),
                        rs.getString("website")
                );
            }
            return null;
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    /**
     * Sets the developer info.
     * @param info the developer info
     * @return whether it performed the update or not
     * @throws DatabaseException
     */
    public boolean setDeveloperInfo(final DeveloperInfo info) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("setDeveloperInfo", 9)
                    .add(info.getDevId())
                    .add(info.getName())
                    .add(info.getCompany())
                    .add(info.getEmail())
                    .add(info.getAddress())
                    .add(info.getPostCode())
                    .add(info.getCity())
                    .add(info.getCountry())
                    .add(info.getWebsite());
            return call.executeUpdate() > 0;
        }
    }

    /***
     * Create a new AI but only if the devid doesn't already have an AI with that ai_name
     * @param aiid the id for the new AI
     * @param name ai_name
     * @param description ai_description
     * @param devid the owner dev
     * @param isPrivate private ai
     * @param backendStatus status of this ai in back end servers
     * @param clientToken a guest token that the dev can give out to users
     * @param language UI parameter
     * @param timezoneString UI parameter
     * @param confidence UI parameter
     * @param personality UI parameter
     * @param voice UI parameter
     * @return
     * @throws DatabaseException
     */
    public UUID createAI(final UUID aiid, final String name, final String description, final String devid,
                         final boolean isPrivate,
                         final BackendStatus backendStatus,
                         final String clientToken,
                         final Locale language, final String timezoneString,
                         final double confidence, final int personality, final int voice,
                         final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addAi", 12)
                    .add(aiid)
                    .add(name)
                    .add(description)
                    .add(devid)
                    .add(isPrivate)
                    .add(jsonSerializer.serialize(backendStatus))
                    .add(clientToken)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice);
            ResultSet result = call.executeQuery();
            if (result.next()) {
                String namedAiid = result.getString("aiid");
                return UUID.fromString((namedAiid == null) ? "" : namedAiid);
            } else {
                throw new DatabaseException("stored procedure returned nothing");
            }
        } catch (IllegalArgumentException | SQLException ex) {
            // null or empty UUID means that there was a db fail of some sort
            throw new DatabaseException(ex);
        }
    }

    public boolean updateAI(final String devId, final UUID aiid, final String description, final boolean isPrivate,
                            final Locale language, final String timezoneString, final double confidence,
                            final int personality, final int voice)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAi", 9)
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

    public boolean updateAIStatus(final AiStatus status, final JsonSerializer jsonSerializer)
            throws DatabaseException {

        // open a transaction since this is a read/modify/write operation and we need consistency
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            // read the status json for all the servers
            ResultSet rs = transaction.getDatabaseCall().initialise("getAiStatus", 2)
                    .add(status.getAiid()).add(status.getDevId()).executeQuery();

            // if there is nothing then end here (not found)
            if (!rs.next()) {
                return false;
            }

            // retrieve the status
            BackendStatus backendStatus = getBackendStatus(rs.getString("backend_status"), jsonSerializer);

            // modify the bit of the structure that relates to the server we are updating
            backendStatus.setEngineStatus(status);

            // write the json block back to the AI table
            transaction.getDatabaseCall().initialise("updateAiStatus", 3)
                    .add(status.getAiid())
                    .add(status.getDevId())
                    .add(jsonSerializer.serialize(backendStatus))
                    .executeUpdate();

            // if all goes well, commit
            transaction.commit();

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        // flag success
        return true;
    }

    public List<ApiAi> getAllAIs(final String devid, final JsonSerializer jsonSerializer) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAIs", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            final ArrayList<ApiAi> res = new ArrayList<>();
            try {
                while (rs.next()) {
                    res.add(getAiFromResultset(rs, jsonSerializer));
                }
                return res;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public ApiAi getAI(final String devid, final UUID aiid, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAi", 2).add(devid).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return getAiFromResultset(rs, jsonSerializer);
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
            call.initialise("deleteAi", 2).add(devid).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public List<AiBot> getBotsLinkedToAi(final String devId, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotsLinkedToAi", 2).add(devId).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                return getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<AiBot> getPublishedBots() throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPublishedBots", 0);
            final ResultSet rs = call.executeQuery();
            try {
                return getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean linkBotToAi(final String devId, final UUID aiid, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("linkBotToAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public boolean unlinkBotFromAi(final String devId, final UUID aiid, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("unlinkBotFromAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public AiBot getPublishedBotForAI(final String devId, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPublishedBotForAi", 2).add(devId).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next() ? getAiBotFromResultset(rs) : null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateAiTrainingFile(final UUID aiUUID, final String trainingData) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAiTrainingFile", 2).add(aiUUID).add(trainingData);
            return call.executeUpdate() > 0;
        }
    }

    public AiBot getBotDetails(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotDetails", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            return rs.next() ? getAiBotFromResultset(rs) : null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public int publishBot(final AiBot bot) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("publishBot", 17)
                    .add(bot.getDevId())
                    .add(bot.getAiid())
                    .add(bot.getName())
                    .add(bot.getDescription())
                    .add(bot.getLongDescription())
                    .add(bot.getAlertMessage())
                    .add(bot.getBadge())
                    .add(bot.getPrice())
                    .add(bot.getSample())
                    .add(bot.getLastUpdate())
                    .add(bot.getCategory())
                    .add(bot.getLicenseType())
                    .add(bot.getPrivacyPolicy())
                    .add(bot.getClassification())
                    .add(bot.getVersion())
                    .add(bot.getVideoLink())
                    .add(true);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DatabaseException("Stored procedure publishBot did not return any value!");
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public List<AiBot> getPurchasedBots(final String devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPurchasedBots", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                return getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean purchaseBot(final String devId, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("purchaseBot", 2).add(devId).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public InputStream getBotIcon(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotIcon", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getBinaryStream(1);
            }
            return null;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean saveBotIcon(final String devId, final int botId, final InputStream inputStream)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("saveBotIcon", 3).add(devId).add(botId).add(inputStream);
            return call.executeUpdate() > 0;
        }
    }

    public RateLimitStatus checkRateLimit(final String devId, final String rateKey, final double burst,
                                          final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rateLimitCheck", 4).add(devId).add(rateKey).add(burst).add(frequency);
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

    private List<AiBot> getBotListFromResultset(final ResultSet rs) throws SQLException {
        final ArrayList<AiBot> bots = new ArrayList<>();
        while (rs.next()) {
            bots.add(this.getAiBotFromResultset(rs));
        }
        return bots;
    }

    private AiBot getAiBotFromResultset(final ResultSet rs) throws SQLException {
        return new AiBot(
                rs.getString("dev_id"),
                UUID.fromString(rs.getString("aiid")),
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("long_description"),
                rs.getString("alert_message"),
                rs.getString("badge"),
                rs.getBigDecimal("price"),
                rs.getString("sample"),
                rs.getString("category"),
                rs.getString("license_type"),
                new DateTime(rs.getTimestamp("last_update")),
                rs.getString("privacy_policy"),
                rs.getString("classification"),
                rs.getString("version"),
                rs.getString("video_link"),
                rs.getBoolean("is_published")
        );
    }

    private ApiAi getAiFromResultset(final ResultSet rs, JsonSerializer jsonSerializer)
            throws SQLException, DatabaseException {
        String localeString = rs.getString("ui_ai_language");
        String timezoneString = rs.getString("ui_ai_timezone");
        // deserialize the backend-status block of JSON
        BackendStatus backendStatus = getBackendStatus(rs.getString("backend_status"), jsonSerializer);
        // create one summary trainign status from the block of data
        TrainingStatus summaryTrainingStatus = getSummaryTrainingStatus(backendStatus);
        return new ApiAi(
                rs.getString("aiid"),
                rs.getString("client_token"),
                rs.getString("ai_name"),
                rs.getString("ai_description"),
                new DateTime(rs.getDate("created_on")),
                rs.getBoolean("is_private"),
                backendStatus,
                summaryTrainingStatus,
                rs.getInt("ui_ai_personality"),
                rs.getDouble("ui_ai_confidence"),
                rs.getInt("ui_ai_voice"),
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
