package com.hutoma.api.connectors.db;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.containers.AiBotConfig;
import com.hutoma.api.containers.AiBotConfigDefinition;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiWithConfig;
import com.hutoma.api.containers.ApiLinkedBotData;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.AiMinP;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseAI extends Database  {


    @Inject
    public DatabaseAI(final ILogger logger, final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
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
                         final JsonSerializer jsonSerializer,
                         final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
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
                            final int personality, final int voice, final List<String> defaultChatResponses,
                            final JsonSerializer serializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateAi", 10)
                    .add(aiid)
                    .add(description)
                    .add(devId)
                    .add(isPrivate)
                    .add(language == null ? null : language.toLanguageTag())
                    .add(timezoneString)
                    .add(confidence)
                    .add(personality)
                    .add(voice)
                    .add(serializer.serialize(defaultChatResponses));
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
            return DatabaseBackends.getBackendStatus(devId, aiid, call);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean updateAIStatus(final AiStatus status)
            throws DatabaseException {
        return updateAIStatus(status.getAiEngine(), status.getAiid(), status.getTrainingStatus(),
                status.getServerIdentifier(), status.getTrainingProgress(), status.getTrainingError());
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

    /***
     * Get data for all the dev's AIs
     * @param devid
     * @return
     * @throws DatabaseException
     */
    public List<ApiAi> getAllAIs(final UUID devid, final JsonSerializer serializer) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // load a hashmap with all the dev's statuses
            HashMap<UUID, BackendStatus> backendStatuses = DatabaseBackends.getAllAIsStatusHashMap(
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
                    ApiAi bot = getAiFromResultset(rs, backendStatuses.get(aiid), serializer);
                    List<AiBot> linkedBots = this.getBotsLinkedToAi(devid, aiid);
                    if (!linkedBots.isEmpty()) {
                        bot.setLinkedBots(linkedBots.stream().map(AiBot::getBotId).collect(Collectors.toList()));
                    }
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

    public ApiAi getAI(final UUID devid, final UUID aiid, final JsonSerializer serializer,
                              final DatabaseTransaction transaction) throws DatabaseException {
        try (DatabaseTransaction dbTrans = transaction == null ? this.transactionProvider.get() : transaction) {

            // load the statuses first
            BackendStatus backendStatus = DatabaseBackends.getBackendStatus(devid, aiid, dbTrans.getDatabaseCall());
            // then load the AI
            ResultSet rs = dbTrans.getDatabaseCall().initialise("getAi", 2)
                    .add(devid)
                    .add(aiid)
                    .executeQuery();

            ApiAi apiAi = null;
            if (rs.next()) {
                apiAi = getAiFromResultset(rs, backendStatus, serializer);
            }
            if (apiAi != null) {
                rs = dbTrans.getDatabaseCall().initialise("getAiBotConfig", 3)
                        .add(devid)
                        .add(aiid)
                        .add(0)
                        .executeQuery();
                if (rs.next()) {
                    String configString = rs.getString("config");
                    AiBotConfig config = (AiBotConfig) serializer.deserialize(configString, AiBotConfig.class);
                    apiAi = new ApiAiWithConfig(apiAi, config);
                }
            }
            return apiAi;
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    /***
     * Load data for an AI and populate its backend server statuses
     * @param devid
     * @param aiid
     * @return an ai, or null if it was not found
     * @throws DatabaseException
     */
    public ApiAi getAI(final UUID devid, final UUID aiid, final JsonSerializer serializer)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            return getAI(devid, aiid, serializer, transaction);
        }
    }

    public boolean checkAIBelongsToDevId(final UUID devId, final UUID aiid) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // then load the AI
            ResultSet rs = transaction.getDatabaseCall().initialise("getAiSimple", 2)
                    .add(devId.toString())
                    .add(aiid)
                    .executeQuery();
            return rs.next();
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

    public boolean deleteAi(final UUID devid, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteAi", 2).add(devid).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateAiTrainingFile(final UUID aiUUID, final String trainingData) throws DatabaseException {
        return updateAiTrainingFile(aiUUID, trainingData, null);
    }

    public boolean updateAiTrainingFile(final UUID aiUUID, final String trainingData,
                                        final DatabaseTransaction transaction) throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("updateAiTrainingFile", 2).add(aiUUID).add(trainingData);
            return call.executeUpdate() > 0;
        }
    }

    public String getWebhookSecretForBot(final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getWebhookSecretForBot", 1).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("hmac_secret");
                }
                throw new DatabaseException("Webhook secret not found");
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean setWebhookSecretForBot(final UUID aiid, final String secret) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("setWebhookSecretForBot", 2).add(aiid).add(secret);
            return call.executeUpdate() > 0;
        }
    }

    public AiBotConfigDefinition getBotConfigDefinition(final UUID devid, final UUID aiid,
                                                        JsonSerializer serializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotConfigDefinition", 2)
                    .add(devid)
                    .add(aiid);
            ResultSet rs = call.executeQuery();
            try {
                AiBotConfigDefinition definition = null;
                if (rs.next()) {
                    String definitionString = rs.getString("api_keys_desc");
                    definition = (AiBotConfigDefinition) serializer.deserialize(definitionString,
                            AiBotConfigDefinition.class);
                }
                return definition;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean setBotConfigDefinition(final UUID devid, final UUID aiid,
                                          AiBotConfigDefinition definition,
                                          JsonSerializer serializer)
            throws DatabaseException {
        String apiKeyString = serializer.serialize(definition);
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("setBotConfigDefinition", 3)
                    .add(devid)
                    .add(aiid)
                    .add(apiKeyString);
            return call.executeUpdate() > 0;
        }
    }

    public boolean setAiBotConfig(final UUID devid, final UUID aiid, final int botId, AiBotConfig aiBotConfig,
                                  JsonSerializer serializer)
            throws DatabaseException {
        String aiBotConfigString = serializer.serialize(aiBotConfig);
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("setAiBotConfig", 4)
                    .add(devid)
                    .add(aiid)
                    .add(botId)
                    .add(aiBotConfigString);
            return call.executeUpdate() > 0;
        }
    }

    public AiBotConfig getBotConfigForWebhookCall(final UUID devId, final UUID originatingAiid,
                                                  UUID aiid,
                                                  final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotConfigForWebhookCall", 3)
                    .add(devId)
                    .add(originatingAiid)
                    .add(aiid);
            ResultSet rs = call.executeQuery();
            try {
                AiBotConfig config = null;
                if (rs.next()) {
                    String configString = rs.getString("config");
                    config = (AiBotConfig) jsonSerializer.deserialize(configString, AiBotConfig.class);
                }
                return config;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean createWebHook(final UUID aiid, final String intentName, final String endpoint, final boolean enabled,
                                 final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
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

    public boolean updateWebHook(final UUID aiid, final String intentName, final String endpoint, final boolean enabled,
                                 final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
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

    public List<AiBot> getBotsLinkedToAi(final UUID devId, final UUID aiid) throws DatabaseException {
        return getBotsLinkedToAi(devId, aiid, null);
    }

    public List<AiBot> getBotsLinkedToAi(final UUID devId, final UUID aiid, final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("getBotsLinkedToAi", 2).add(devId).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                return DatabaseMarketplace.getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public Pair<UUID, UUID> getIsBotLinkedToAi(final UUID devId, final UUID aiid, final int botId)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIsBotLinkedToAi", 3).add(devId).add(aiid).add(botId);

            try {
                ResultSet rs = call.executeQuery();

                Pair<UUID, UUID> linkedDevAiidPair = null;
                if (rs.next()) {
                    String linkedDevidString = rs.getString("dev_id");
                    UUID linkedDevid = UUID.fromString(linkedDevidString);
                    String linkedAiidString = rs.getString("aiid");
                    UUID linkedAiid = UUID.fromString(linkedAiidString);
                    linkedDevAiidPair = new Pair<>(linkedDevid, linkedAiid);
                }
                return linkedDevAiidPair;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public ApiLinkedBotData getLinkedBotData(final UUID devId, final UUID aiid, final int botId,
                                             final JsonSerializer serializer)
            throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            try {
                if (this.getIsBotLinkedToAi(devId, aiid, botId) == null) {
                    // bot not linked, not point in querying further
                    return null;
                }

                DatabaseCall call = transaction.getDatabaseCall();
                call.initialise("getBotstoreItem", 1).add(botId);
                ResultSet rs = call.executeQuery();
                ApiLinkedBotData botData = null;

                if (rs.next()) {
                    final AiBot aiBot = DatabaseMarketplace.getAiBotFromResultset(rs);
                    call = transaction.getDatabaseCall();
                    call.initialise("getAiBotConfig", 3).add(devId).add(aiid).add(botId);
                    rs = call.executeQuery();
                    AiBotConfig config = null;
                    if (rs.next()) {
                        String configString = rs.getString("config");
                        config = (AiBotConfig) serializer.deserialize(configString, AiBotConfig.class);
                    }
                    botData = new ApiLinkedBotData(aiBot, config);
                }
                return botData;
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

    public boolean linkBotToAi(final UUID devId, final UUID aiid, final int botId) throws DatabaseException {
        return this.linkBotToAi(devId, aiid, botId, null);
    }

    public boolean linkBotToAi(final UUID devId, final UUID aiid, final int botId,
                               final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("linkBotToAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public boolean unlinkBotFromAi(final UUID devId, final UUID aiid, final int botId) throws DatabaseException {
        return this.unlinkBotFromAi(devId, aiid, botId, null);
    }

    public boolean unlinkBotFromAi(final UUID devId, final UUID aiid, final int botId,
                                   final DatabaseTransaction transaction)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("unlinkBotFromAi", 3).add(devId).add(aiid).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    @SuppressWarnings("unchecked")
    public ChatState getChatState(final UUID devId, final UUID aiid, final UUID chatId,
                                  final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            ApiAi ai = getAI(devId, aiid, jsonSerializer, this.transactionProvider.get());
            if (ai == null) {
                return null;
            }
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
                    confidenceThreshold = ai.getConfidence();
                }
                return new ChatState(
                        new DateTime(rs.getTimestamp("timestamp")),
                        rs.getString("topic"),
                        rs.getString("history"),
                        lockedAiid != null ? UUID.fromString(lockedAiid) : null,
                        entities,
                        confidenceThreshold,
                        ChatHandoverTarget.fromInt(rs.getInt("chat_target"))
                );
            }
            ChatState chatState = ChatState.getEmpty();
            chatState.setConfidenceThreshold(ai.getConfidence());
            return chatState;
        } catch (Exception ex) {
            throw new DatabaseException(ex);
        }
    }

    public boolean saveChatState(final UUID devId, final UUID chatId, final ChatState chatState,
                                 final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            String lockedAiid = (chatState.getLockedAiid() != null) ? chatState.getLockedAiid().toString() : null;
            call.initialise("setChatState", 9)
                    .add(devId)
                    .add(chatId)
                    .add(chatState.getTimestamp())
                    .add(limitSize(chatState.getTopic(), 250))
                    .add(limitSize(chatState.getHistory(), 1024))
                    .add(lockedAiid)
                    .add(chatState.getEntityValues().isEmpty()
                            ? null : jsonSerializer.serialize(chatState.getEntityValues()))
                    .add(chatState.getConfidenceThreshold())
                    .add(chatState.getChatTarget().getIntValue());
            return call.executeUpdate() > 0;
        }
    }

    private static ApiAi getAiFromResultset(final ResultSet rs, final BackendStatus backendStatus,
                                     final JsonSerializer serializer)
            throws SQLException, DatabaseException {
        String localeString = rs.getString("ui_ai_language");
        String timezoneString = rs.getString("ui_ai_timezone");
        List<String> defaultChatResponses = serializer.deserializeList(rs.getString("default_chat_responses"));
        AiBotConfigDefinition definition = (AiBotConfigDefinition) serializer.deserialize(
                rs.getString("api_keys_desc"), AiBotConfigDefinition.class);

        int publishingStateOnDb = rs.getInt("publishing_state");
        // Note that if null we actually match as it will return 0 => NOT_PUBLISHED, but to make sure
        // we always get it right regardless of the default value
        AiBot.PublishingState publishingState;
        try {
            publishingState = rs.wasNull()
                    ? AiBot.PublishingState.NOT_PUBLISHED
                    : AiBot.PublishingState.from(publishingStateOnDb);
        } catch (IllegalArgumentException ex) {
            // Default to NOT_PUBLISHED if for some reason we can't understand what is in the db
            publishingState = AiBot.PublishingState.NOT_PUBLISHED;
        }

        // create one summary training status from the block of data
        ApiAi ai = new ApiAi(
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
                Locale.forLanguageTag(localeString),
                timezoneString,
                rs.getString("hmac_secret"),
                rs.getString("passthrough_url"),
                defaultChatResponses,
                definition);
        ai.setPublishingState(publishingState);
        ai.setReadOnly(publishingState == AiBot.PublishingState.SUBMITTED
                || publishingState == AiBot.PublishingState.PUBLISHED
                || publishingState == AiBot.PublishingState.REMOVED);
        return ai;
    }
}
