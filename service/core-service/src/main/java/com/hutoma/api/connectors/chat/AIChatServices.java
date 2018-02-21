package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.IConnectConfig;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.AiDevId;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.AiMinP;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;


/**
 * AI Chat services.
 */
public class AIChatServices extends ServerConnector {

    private static final String LOGFROM = "aichatservices";
    private final ChatWnetConnector backendWnetConnector;
    private final ChatAimlConnector backendAimlConnector;
    private List<ChatBackendConnector.RequestInProgress> wnetFutures;
    private List<ChatBackendConnector.RequestInProgress> aimlFutures;
    private Map<UUID, Double> minPMap = new HashMap<>();
    private final Config config;
    private final DatabaseAI databaseAi;

    private long requestDeadline;

    @Inject
    public AIChatServices(final DatabaseAI databaseAi, final ILogger logger,
                          final IConnectConfig connectConfig,
                          final JsonSerializer serializer,
                          final Tools tools, final Config config, final JerseyClient jerseyClient,
                          final TrackedThreadSubPool threadSubPool,
                          final ChatWnetConnector backendWnetConnector,
                          final ChatAimlConnector backendAimlConnector) {
        super(logger, connectConfig, serializer, tools, jerseyClient, threadSubPool);
        this.backendWnetConnector = backendWnetConnector;
        this.backendAimlConnector = backendAimlConnector;
        this.config = config;
        this.databaseAi = databaseAi;
    }

    /***
     * Creates n requests, one to each back-end server and starts them async
     * @param devId
     * @param aiid
     * @param chatId
     * @param question
     * @param chatState
     * @throws AiServicesException
     */
    public void startChatRequests(final UUID devId, final UUID aiid, final UUID chatId, final String question,
                                  final ChatState chatState)
            throws AiServicesException, ChatBackendConnector.AiControllerException, NoServerAvailableException {

        // calculate the exact deadline for this group of requests
        this.requestDeadline = this.tools.getTimestamp() + this.config.getBackendCombinedRequestTimeoutMs();

        // generate the parameters to send
        HashMap<String, String> parameters = new HashMap<String, String>() {{
            put("chatId", chatId.toString());
            put("q", question);
        }};
        List<AiMinP> ais = this.getAIsLinkedToAi(devId, aiid);
        this.minPMap = ais.stream().collect(Collectors.toMap(AiMinP::getAiid, AiMinP::getMinP));
        // add self
        this.minPMap.put(aiid, chatState.getConfidenceThreshold());

        // If this AI is linked to the AIML "bot" then we need to issue a chat request to the AIML backend as well
        boolean usedAimlBot = false;
        HashSet<UUID> aimlBotIdsSet = new HashSet<>(this.config.getAimlBotAiids());

        List<AiDevId> aimlAis = new ArrayList<>();

        // map all the linked AIs by aiid
        Map<UUID, AiDevId> map = ais.stream()
                .collect(Collectors.toMap(AiDevId::getAiid, Function.identity()));

        // extract the AIs that are AIML based
        for (Map.Entry<UUID, AiDevId> entry : map.entrySet()) {
            if (aimlBotIdsSet.contains(entry.getKey())) {
                aimlAis.add(new AiDevId(entry.getValue().getDevId(), entry.getValue().getAiid()));
            }
        }

        // if there are any AIML bots then start the calls
        if (!aimlAis.isEmpty()) {
            this.aimlFutures = this.backendAimlConnector.issueChatRequests(parameters, aimlAis, chatState);
            usedAimlBot = true;
        }

        // remove any bots that are AIML from the list of linked list still to be processed
        List<AiDevId> listAis = ais.stream()
                .filter(ai -> !aimlBotIdsSet.contains(ai.getAiid()))
                .map(x -> new AiDevId(x.getDevId(), x.getAiid())).collect(Collectors.toList());

        // find out which servers can chat with this AI
        Set<BackendServerType> canChatWith = canChatWithAi(devId, aiid);

        // make copies of the AI lists
        List<AiDevId> wnetAIs = new ArrayList<>(listAis);

        // add the AI to the list if the server can chat
        if (canChatWith.contains(BackendServerType.WNET)) {
            wnetAIs.add(new AiDevId(devId, aiid));
        }

        // If we're issuing a chat request but there are no AIs available to serve it, just fail
        if (wnetAIs.isEmpty() && !usedAimlBot) {
            throw new AiNotReadyToChat("No AIs ready to chat");
        }

        if (!wnetAIs.isEmpty()) {
            this.wnetFutures = this.backendWnetConnector.issueChatRequests(parameters, wnetAIs, chatState);
        }
    }

    /***
     * Waits for WNET calls to complete and returns the result
     * @return map of results, or null if there were no requests
     * @throws ChatBackendConnector.AiControllerException
     */
    public Map<UUID, ChatResult> awaitWnet() throws ChatBackendConnector.AiControllerException {
        if (this.wnetFutures != null) {
            return this.backendWnetConnector.waitForAll(this.wnetFutures, getRemainingTime());
        }
        return null;
    }

    /***
     * Waits for AIML calls to complete and returns the result
     * @return map of results, or null if there were no requests
     * @throws ChatBackendConnector.AiControllerException
     */
    public Map<UUID, ChatResult> awaitAiml() throws ChatBackendConnector.AiControllerException {
        if (this.aimlFutures != null) {
            return this.backendAimlConnector.waitForAll(this.aimlFutures, getRemainingTime());
        }
        return null;
    }

    @Override
    public void abandonCalls() {
        if (this.wnetFutures != null) {
            this.wnetFutures.forEach(ChatBackendConnector.RequestInProgress::closeRequest);
        }
        if (this.aimlFutures != null) {
            this.aimlFutures.forEach(ChatBackendConnector.RequestInProgress::closeRequest);
        }
        this.backendWnetConnector.abandonCalls();
        this.backendAimlConnector.abandonCalls();
    }

    public List<AiMinP> getAIsLinkedToAi(final UUID devId, final UUID aiid)
            throws ChatBackendConnector.AiControllerException {
        try {
            return this.databaseAi.getAisLinkedToAi(devId, aiid);
        } catch (DatabaseException ex) {
            throw new ChatBackendConnector.AiControllerException("Couldn't get the list of linked bots");
        }
    }

    /***
     * Determine whether the AI is in a state where the user can interact with it
     * @param devId dev owner
     * @param aiid id
     * @return a set of servers that can interact with the AI (empty set if none)
     */
    Set<BackendServerType> canChatWithAi(final UUID devId, final UUID aiid) {
        // by default chat with nothing
        HashSet<BackendServerType> chatSet = new HashSet<>();
        BackendStatus result = null;
        try {
            // try to get the real status from the database
            result = this.databaseAi.getAIStatusReadOnly(devId, aiid);
        } catch (DatabaseException ex) {
            // if it fails, log the error and keep the set null
            this.logger.logException(LOGFROM, ex);
        }
        if (result != null) {
            // get the status of each backend server for this ai
            TrainingStatus wnetStatus = result.getEngineStatus(BackendServerType.WNET).getTrainingStatus();
            // wnet can only chat if training is complete
            if (wnetStatus == TrainingStatus.AI_TRAINING_COMPLETE) {
                chatSet.add(BackendServerType.WNET);
            }
        }
        return chatSet;
    }

    public Map<UUID, Double> getMinPMap() {
        return this.minPMap;
    }

    public String getAIPassthroughUrl(UUID devid, UUID aiid) throws ChatLogic.ChatFailedException {
        String result = null;
        try {
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.serializer);
            if (ai == null) {
                this.logger.logUserWarnEvent(LOGFROM, "Request for unknown AIID for user", devid.toString(),
                        LogMap.map("AIID", aiid));
                throw new ChatLogic.ChatFailedException(ApiError.getNotFound("AIID not found"));
            }
            result = ai.getPassthroughUrl();
        } catch (DatabaseException e) {
            this.logger.logException("Database exception attempting to retrieve PassthroughUrl", e);
        }
        return result;
    }

    /***
     * Assuming that we pre-calculated the deadline when we started the chat requests,
     * this calculates the time remaining until we reach the deadline
     * so that it can be used as a timeout for awaiting calls
     * @return time left, in ms
     */
    private int getRemainingTime() {
        return (int) Math.max(0, this.requestDeadline - this.tools.getTimestamp());
    }

    public static class AiNotReadyToChat extends AiServicesException {
        AiNotReadyToChat(final String message) {
            super(message);
        }
    }
}
