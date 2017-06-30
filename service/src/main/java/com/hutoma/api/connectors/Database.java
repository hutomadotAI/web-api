package com.hutoma.api.connectors;

import com.google.gson.internal.LinkedTreeMap;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.*;

import org.apache.commons.lang.LocaleUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

    /***
     * Truncate strings if they are too long for the database field
     * @param field source data
     * @param maxLength
     * @return
     */
    protected static String limitSize(String field, int maxLength) {
        return ((field == null) || (field.length() <= maxLength)) ? field : field.substring(0, maxLength);
    }

    private static MemoryIntent loadMemoryIntent(final ResultSet rs, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try {
            List<LinkedTreeMap<String, Object>> list =
                    jsonSerializer.deserializeList(rs.getString("variables"));
            List<MemoryVariable> variables = new ArrayList<>();
            for (LinkedTreeMap<String, Object> e : list) {
                @SuppressWarnings("unchecked")
                MemoryVariable memoryVariable = new MemoryVariable(
                        e.get("entity").toString(),
                        e.containsKey("value") ? e.get("value").toString() : null,
                        (boolean) e.get("mandatory"),
                        (List<String>) e.get("entity_keys"),
                        (List<String>) e.get("prompts"),
                        (int) Math.round((double) e.get("max_prompts")),
                        (int) Math.round((double) e.get("times_prompted")),
                        (boolean) e.get("system_entity"),
                        (boolean) e.get("persistent"));
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

    public boolean createDev(final String username, final String email, final String password,
                             final String passwordSalt, final String firstName, final String lastName,
                             final String devToken, final int planId, final String devId, final String clientToken)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addUser", 10)
                    .add(username)
                    .add(email)
                    .add(password)
                    .add(passwordSalt)
                    .add(firstName)
                    .add(lastName)
                    .add(devToken)
                    .add(planId)
                    .add(devId)
                    .add(clientToken);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Gets the user information.
     * @param username the username
     * @return the user information
     * @throws DatabaseException
     */
    public UserInfo getUser(final String username) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getUserDetails", 1).add(username);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new UserInfo(
                            rs.getString("first_name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            new DateTime(rs.getTimestamp("created")),
                            rs.getBoolean("valid"),
                            rs.getBoolean("internal"),
                            rs.getString("password"),
                            rs.getString("password_salt"),
                            rs.getString("dev_id"),
                            rs.getString("attempt"),
                            rs.getInt("id"),
                            rs.getString("dev_token"));
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateUserPassword(final int userId, final String password, final String passwordSalt)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateUserPassword", 3)
                    .add(userId).add(password).add(passwordSalt);
            return call.executeUpdate() > 0;
        }
    }

    public boolean isPasswordResetTokenValid(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("isPasswordResetTokenValid", 1).add(token);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next();
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public int getUserIdForResetToken(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getUserIdForResetToken", 1).add(token);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt("uid");
                }
                return -1;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deletePasswordResetToken(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deletePasswordResetToken", 1).add(token);
            return call.executeUpdate() > 0;
        }
    }

    public boolean insertPasswordResetToken(final int userId, final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("insertResetToken", 2).add(token).add(userId);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Gets whether the user already exists or not.
     * @param username   the username to check
     * @param checkEmail whether to check the email field as well or not
     * @return whether the user already exists or not
     * @throws DatabaseException
     */
    public boolean userExists(final String username, final boolean checkEmail) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("userExists", 2).add(username).add(checkEmail);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next();
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    /**
     * Updates the user login attempts.
     * @param devId    the developer id
     * @param attempts the number of attempts, or a 'magic code' (this comes from the PHP code)
     * @return whether the update succeeded or not
     * @throws DatabaseException
     */
    public boolean updateUserLoginAttempts(final String devId, final String attempts) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateUserLoginAttempts", 2).add(devId).add(attempts);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Redeems an invite code for user registration.
     * @param code     the invite code.
     * @param username the registering user.
     * @return true if successful, otherwise false.
     * @throws DatabaseException database exception.
     */
    public boolean redeemInviteCode(final String code, final String username) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("redeemInviteCode", 2).add(code).add(username);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Determines whether a specified invite code is valid.
     * @param code the invite code.
     * @return true if the code is valid, otherwise false.
     * @throws DatabaseException database exception.
     * @throws SQLException      sql exception.
     */
    public boolean inviteCodeValid(final String code) throws DatabaseException, SQLException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("existsInviteCode", 1).add(code);

            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }

        return false;
    }

    /**
     * Gets the developer plan for the given developer Id.
     * @param devId the developer id
     * @return the plan, or null if there is no developer Id or not plan associated to it
     * @throws DatabaseException database exception
     */
    public DevPlan getDevPlan(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevPlan", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new DevPlan(rs.getInt("maxai"), rs.getInt("monthlycalls"),
                            rs.getLong("maxmem"), rs.getInt("maxtraining"));
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
    public boolean deleteDev(final UUID devid) throws DatabaseException {

        int updateCount = 0;
        // start a transaction
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // delete the user's AIs and status for those AIs
            transaction.getDatabaseCall().initialise("deleteAllAIs", 1)
                    .add(devid).executeUpdate();
            // delete the user
            updateCount = transaction.getDatabaseCall().initialise("deleteUser", 1)
                    .add(devid).executeUpdate();
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
    public DeveloperInfo getDeveloperInfo(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDeveloperInfo", 1).add(devId);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                final String devIdString = rs.getString("dev_id");
                final UUID devIdDb = UUID.fromString(devIdString);
                return new DeveloperInfo(
                        devIdDb,
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
     * @param clientToken a guest token that the dev can give out to users
     * @param language UI parameter
     * @param timezoneString UI parameter
     * @param confidence UI parameter
     * @param personality UI parameter
     * @param voice UI parameter
     * @return
     * @throws DatabaseException
     */
    public UUID createAI(final UUID aiid, final String name, final String description, final UUID devid,
                         final boolean isPrivate,
                         final String clientToken,
                         final Locale language, final String timezoneString,
                         final double confidence, final int personality, final int voice,
                         final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addAi", 11)
                    .add(aiid)
                    .add(name)
                    .add(description)
                    .add(devid)
                    .add(isPrivate)
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

    public boolean updateAI(final UUID devId, final UUID aiid, final String description, final boolean isPrivate,
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

    /***
     * Combines the statuses of one AI and returns them as a BackendStatus object
     * @param devId
     * @param aiid
     * @return
     * @throws DatabaseException
     */
    public BackendStatus getAIStatusReadOnly(final UUID devId, final UUID aiid)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            return getBackendStatus(devId, aiid, call);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean updateAIStatus(BackendServerType serverType, UUID aiid, TrainingStatus trainingStatus,
                                  String endpoint, double trainingProgress, double trainingError)
            throws DatabaseException {

        // open a transaction since this is a read/modify/write operation and we need consistency
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            transaction.getDatabaseCall().initialise("updateAiStatus", 6)
                    .add(serverType.value())
                    .add(aiid)
                    .add(trainingStatus)
                    .add(endpoint)
                    .add(trainingProgress)
                    .add(trainingError)
                    .executeUpdate();

            // if all goes well, commit
            transaction.commit();
        }

        // flag success
        return true;

    }

    public boolean updateAIStatus(final AiStatus status)
            throws DatabaseException {
        return updateAIStatus(status.getAiEngine(), status.getAiid(), status.getTrainingStatus(),
                status.getServerIdentifier(), status.getTrainingProgress(), status.getTrainingError());
    }

    /***
     * Get data for all the dev's AIs
     * @param devid
     * @return
     * @throws DatabaseException
     */
    public List<ApiAi> getAllAIs(final UUID devid) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // load a hashmap with all the dev's statuses
            HashMap<UUID, BackendStatus> backendStatuses = getAllAIsStatusHashMap(
                    devid, transaction.getDatabaseCall());

            // load all the dev's AIs
            ResultSet rs = transaction.getDatabaseCall()
                    .initialise("getAIs", 1)
                    .add(devid)
                    .executeQuery();
            final ArrayList<ApiAi> res = new ArrayList<>();
            try {
                while (rs.next()) {
                    // combine status data with AI
                    UUID aiid = UUID.fromString(rs.getString("aiid"));
                    ApiAi bot = getAiFromResultset(rs, backendStatuses.get(aiid));
                    int publishingStateOnDb = rs.getInt("publishing_state");
                    // Note that if null we actually match as it will return 0 => NOT_PUBLISHED, but to make sure
                    // we always get it right regardless of the default value
                    AiBot.PublishingState state;
                    try {
                        state = rs.wasNull()
                                ? AiBot.PublishingState.NOT_PUBLISHED
                                : AiBot.PublishingState.from(publishingStateOnDb);
                    } catch (IllegalArgumentException ex) {
                        // Default to NOT_PUBLISHED if for some reason we can't understand what is in the db
                        state = AiBot.PublishingState.NOT_PUBLISHED;
                    }
                    bot.setPublishingState(state);
                    res.add(bot);
                }
                return res;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Load data for an AI and populate its backend server statuses
     * @param devid
     * @param aiid
     * @return an ai, or null if it was not found
     * @throws DatabaseException
     */
    public ApiAi getAI(final UUID devid, final UUID aiid)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // load the statuses first
            BackendStatus backendStatus = getBackendStatus(devid, aiid, transaction.getDatabaseCall());
            // then load the AI
            ResultSet rs = transaction.getDatabaseCall().initialise("getAi", 2)
                    .add(devid)
                    .add(aiid)
                    .executeQuery();
            if (rs.next()) {
                return getAiFromResultset(rs, backendStatus);
            }
            return null;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean checkAIBelongsToDevId(final UUID devId, final UUID aiid) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // then load the AI
            ResultSet rs = transaction.getDatabaseCall().initialise("getAiSimple", 2)
                    .add(devId.toString())
                    .add(aiid)
                    .executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
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

    public String getDevToken(final UUID devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevTokenFromDevID", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("dev_token");
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deleteAi(final UUID devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteAi", 2).add(devid).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public List<AiBot> getBotsLinkedToAi(final UUID devId, final UUID aiid) throws DatabaseException {
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

    public List<AiMinP> getAisLinkedToAi(final UUID devId, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAisLinkedToAi", 2)
                    .add(devId)
                    .add(aiid);
            try {
                final ResultSet rs = call.executeQuery();
                List<AiMinP> ais = new ArrayList<>();
                while (rs.next()) {
                    ais.add(new AiMinP(
                            UUID.fromString(rs.getString("linked_ai_devId")),
                            UUID.fromString(rs.getString("linked_ai")),
                            rs.getDouble("minP")
                    ));
                }
                return ais;
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

    public boolean linkBotToAi(final UUID devId, final UUID aiid, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("linkBotToAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public boolean unlinkBotFromAi(final UUID devId, final UUID aiid, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("unlinkBotFromAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public AiBot getPublishedBotForAI(final UUID devId, final UUID aiid) throws DatabaseException {
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
                    .add(bot.getPublishingState().value());
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

    public boolean updateBotPublishingState(final int botId, final AiBot.PublishingState state)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateBotPublishingState", 2)
                    .add(botId)
                    .add(state.value());
            return call.executeUpdate() > 0;
        }
    }

    public List<AiBot> getPurchasedBots(final UUID devId) throws DatabaseException {
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

    public boolean purchaseBot(final UUID devId, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("purchaseBot", 2).add(devId).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public String getBotIconPath(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotIcon", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getString("botIcon");
            }
            return null;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean saveBotIconPath(final UUID devId, final int botId, final String filename)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("saveBotIcon", 3).add(devId).add(botId).add(filename);
            return call.executeUpdate() > 0;
        }
    }

    public boolean hasBotBeenPurchased(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("hasBotBeenPurchased", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) != 0;
            }
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
        return false;
    }

    public RateLimitStatus checkRateLimit(final UUID devId, final String rateKey, final double burst,
                                          final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rateLimitCheck", 4)
                    .add(devId).add(rateKey).add(burst).add(frequency);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(
                            rs.getBoolean("rate_limit"),
                            rs.getFloat("tokens"),
                            rs.getBoolean("valid"));
                }
                throw new DatabaseException(
                        new Exception("stored proc should have returned a row but it returned none"));
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public MemoryIntent getMemoryIntent(final String intentName, final UUID aiid, UUID chatId,
                                        final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getMemoryIntent", 3)
                    .add(intentName).add(aiid).add(chatId);
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

    public ChatState getChatState(final UUID devId, final UUID aiid, final UUID chatId,
                                  final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getChatState", 2).add(devId).add(chatId);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                String entitiesJson = rs.getString("entity_values");
                HashMap<String, String> entities;
                if (entitiesJson == null) {
                    entities = new HashMap<>();
                } else {
                    entities = (HashMap<String, String>) jsonSerializer.deserialize(entitiesJson, HashMap.class);
                }

                String lockedAiid = rs.getString("locked_aiid");
                double confidenceThreshold = rs.getDouble("confidence_threshold");
                if (rs.wasNull()) {
                    confidenceThreshold = this.getAI(devId, aiid).getConfidence();
                }
                return new ChatState(
                        new DateTime(rs.getTimestamp("timestamp")),
                        rs.getString("topic"),
                        rs.getString("history"),
                        lockedAiid != null ? UUID.fromString(lockedAiid) : null,
                        entities,
                        confidenceThreshold
                );
            }
            ChatState chatState = ChatState.getEmpty();
            chatState.setConfidenceThreshold(this.getAI(devId, aiid).getConfidence());
            return chatState;
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public boolean saveChatState(final UUID devId, final UUID chatId, final ChatState chatState,
                                 final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            String lockedAiid = (chatState.getLockedAiid() != null) ? chatState.getLockedAiid().toString() : null;
            call.initialise("setChatState", 8)
                    .add(devId)
                    .add(chatId)
                    .add(chatState.getTimestamp())
                    .add(limitSize(chatState.getTopic(), 250))
                    .add(limitSize(chatState.getHistory(), 1024))
                    .add(lockedAiid)
                    .add(chatState.getEntityValues().isEmpty()
                            ? null : jsonSerializer.serialize(chatState.getEntityValues()))
                    .add(chatState.getConfidenceThreshold());
            return call.executeUpdate() > 0;
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

    public boolean deleteMemoryIntent(final MemoryIntent intent) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteMemoryIntent", 3)
                    .add(intent.getName())
                    .add(intent.getAiid())
                    .add(intent.getChatId());
            return call.executeUpdate() > 0;
        }
    }

    public List<Integration> getAiIntegrationList() throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntegrations", 0);
            ResultSet rs = call.executeQuery();
            List<Integration> list = new ArrayList<>();
            try {
                while (rs.next()) {
                    list.add(
                            new Integration(
                                    rs.getInt("int_id"),
                                    rs.getString("name"),
                                    rs.getString("description"),
                                    rs.getString("icon"),
                                    rs.getBoolean("available")));

                }
                return list;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean createWebHook(final UUID aiid, final String intentName, final String endpoint, final boolean enabled)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addWebhook", 4)
                    .add(aiid)
                    .add(intentName)
                    .add(endpoint)
                    .add(enabled);
            return call.executeUpdate() > 0;
        }
    }

    public WebHook getWebHook(final UUID aiid, final String intentName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getWebhook", 2).add(aiid).add(intentName);
            ResultSet rs = call.executeQuery();

            try {
                if (rs.next()) {
                    return new WebHook(UUID.fromString(rs.getString("aiid")),
                            rs.getString("intent_name"),
                            rs.getString("endpoint"),
                            rs.getBoolean("enabled"));
                }
                return null;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateWebHook(final UUID aiid, final String intentName, final String endpoint, final boolean enabled)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateWebhook", 4)
                    .add(aiid).add(intentName).add(endpoint).add(enabled);
            return call.executeUpdate() > 0;
        }
    }

    public boolean deleteWebHook(final UUID aiid, final String intentName) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteWebhook", 2).add(aiid).add(intentName);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateIntegration(final UUID aiid, final UUID devid, final IntegrationType integration,
                                     final String integratedResource, final String integratedUserid,
                                     final String data, final String status,
                                     boolean active) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAiIntegration", 8)
                    .add(aiid)
                    .add(devid)
                    .add(integration.value())
                    .add(integratedResource)
                    .add(integratedUserid)
                    .add(data)
                    .add(status)
                    .add(active);
            return call.executeUpdate() > 0;
        }
    }

    public IntegrationRecord getIntegration(final UUID aiid, final UUID devid, final IntegrationType integration)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAiIntegration", 3)
                    .add(aiid)
                    .add(devid)
                    .add(integration.value());
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return new IntegrationRecord(
                        rs.getString("integrated_resource"),
                        rs.getString("integrated_userid"),
                        rs.getString("data"),
                        rs.getString("status"),
                        rs.getBoolean("active"));
            }
            return null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public IntegrationRecord getIntegrationResource(final IntegrationType integration,
                                                    final String integratedResource)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntegratedResource", 2)
                    .add(integration.value())
                    .add(integratedResource);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return new IntegrationRecord(
                        UUID.fromString(rs.getString("aiid")),
                        UUID.fromString(rs.getString("devid")),
                        rs.getString("integrated_userid"),
                        rs.getString("data"),
                        rs.getString("status"),
                        rs.getBoolean("active"));
            }
            return null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public void updateIntegrationStatus(final UUID aiid, final IntegrationType integration,
                                        final String status, boolean setChatTimeNow)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateIntegrationStatus", 4)
                    .add(aiid)
                    .add(integration.value())
                    .add(status)
                    .add(setChatTimeNow);
            call.executeUpdate();
        }
    }

    public void deleteIntegration(final UUID aiid, final UUID devid,
                                  final IntegrationType integrationType) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteIntegration", 3)
                    .add(aiid)
                    .add(devid)
                    .add(integrationType.value());
            call.executeUpdate();
        }
    }

    public boolean isIntegratedUserAlreadyRegistered(final IntegrationType integration, final String userID, final UUID devid)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("checkIntegrationUser", 3)
                    .add(integration.value())
                    .add(userID)
                    .add(devid);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt("use_count") > 0;
            }
            return false;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    private List<AiBot> getBotListFromResultset(final ResultSet rs) throws SQLException {
        final ArrayList<AiBot> bots = new ArrayList<>();
        while (rs.next()) {
            bots.add(this.getAiBotFromResultset(rs));
        }
        return bots;
    }

    private ApiAi getAiFromResultset(final ResultSet rs, BackendStatus backendStatus)
            throws SQLException, DatabaseException {
        String localeString = rs.getString("ui_ai_language");
        String timezoneString = rs.getString("ui_ai_timezone");
        // create one summary training status from the block of data
        return new ApiAi(
                rs.getString("aiid"),
                rs.getString("client_token"),
                rs.getString("ai_name"),
                rs.getString("ai_description"),
                new DateTime(rs.getTimestamp("created_on")),
                rs.getBoolean("is_private"),
                (backendStatus == null) ? new BackendStatus() : backendStatus,
                rs.getBoolean("has_training_file"),
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

    protected AiBot getAiBotFromResultset(final ResultSet rs) throws SQLException {
        return new AiBot(
                UUID.fromString(rs.getString("dev_id")),
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
                AiBot.PublishingState.from(rs.getInt("publishing_state")),
                rs.getString("botIcon")
        );
    }

    /***
     * Interpret a row from the ai_status table
     * @param rs resultset
     * @return a pair of (type, update)
     * @throws DatabaseException
     */
    protected Pair<BackendServerType, BackendEngineStatus> getBackendEngineStatus(ResultSet rs)
            throws DatabaseException {

        try {
            BackendServerType serverType = BackendServerType.forValue(rs.getString("server_type"));
            TrainingStatus trainingStatus = TrainingStatus.forValue(rs.getString("training_status"));
            double trainingProgress = rs.getDouble("training_progress");
            double trainingError = rs.getDouble("training_error");
            if (serverType == null) {
                throw new DatabaseException("bad servertype");
            }
            if (trainingStatus == null) {
                throw new DatabaseException("bad trainingstatus");
            }
            UUID aiid = UUID.fromString(rs.getString("aiid"));

            // queue status
            QueueAction action = QueueAction.forValue(rs.getString("queue_action"));
            String serverIdentifier = rs.getString("server_endpoint");
            java.sql.Timestamp updateTimeObject = rs.getTimestamp("update_time");
            DateTime updateTime = (updateTimeObject == null) ? null : new DateTime(updateTimeObject);

            BackendEngineStatus status = new BackendEngineStatus(aiid, trainingStatus, trainingError,
                    trainingProgress, action, serverIdentifier, updateTime);
            return new Pair<>(serverType, status);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Calls the database to get the status of all back-end servers for the AI
     * @param devId dev
     * @param aiid ai
     * @param call a database call in transaction
     * @return BackendStatus
     * @throws DatabaseException
     * @throws SQLException
     */
    protected BackendStatus getBackendStatus(final UUID devId, final UUID aiid, final DatabaseCall call)
            throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAiStatus", 2)
                .add(aiid).add(devId).executeQuery();

        BackendStatus status = new BackendStatus();
        while (rs.next()) {
            Pair<BackendServerType, BackendEngineStatus> update = getBackendEngineStatus(rs);
            status.setEngineStatus(update.getA(), update.getB());
        }

        return status;
    }

    /***
     * Reads the whole status table for a dev and returns a hashmap of BackendStatuses
     * @param devid the dev
     * @param call a database call in transaction
     * @return hashmap
     * @throws DatabaseException
     */
    protected HashMap<UUID, BackendStatus> getAllAIsStatusHashMap(
            final UUID devid,
            final DatabaseCall call) throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAIsStatus", 1)
                .add(devid)
                .executeQuery();

        HashMap<UUID, BackendStatus> statuses = new HashMap<>();
        while (rs.next()) {
            UUID aiid = UUID.fromString(rs.getString("aiid"));
            Pair<BackendServerType, BackendEngineStatus> serverStatus =
                    getBackendEngineStatus(rs);
            statuses.computeIfAbsent(aiid, x -> new BackendStatus())
                    .setEngineStatus(serverStatus.getA(), serverStatus.getB());
        }
        return statuses;
    }
}
