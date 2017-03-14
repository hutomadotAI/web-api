package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.InvocationResult;
import com.hutoma.api.controllers.RequestAiml;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.controllers.RequestRnn;
import com.hutoma.api.controllers.RequestWnet;
import com.hutoma.api.controllers.ServerMetadata;

import org.glassfish.jersey.client.JerseyClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.inject.Inject;


/**
 * AI Chat services.
 */
public class AIChatServices extends ServerConnector {

    private static final String LOGFROM = "aichatservices";
    private final RequestWnet requestWnet;
    private final RequestRnn requestRnn;
    private final RequestAiml requestAiml;
    private List<Future<InvocationResult>> wnetFutures;
    private List<Future<InvocationResult>> rnnFutures;
    private List<Future<InvocationResult>> aimlFutures;

    private long requestDeadline;

    @Inject
    public AIChatServices(final Database database, final ILogger logger, final JsonSerializer serializer,
                          final Tools tools, final Config config, final JerseyClient jerseyClient,
                          final ThreadSubPool threadSubPool,
                          final RequestWnet wnetController, final RequestRnn rnnController,
                          final RequestAiml aimlController) {
        super(database, logger, serializer, tools, config, jerseyClient, threadSubPool);
        this.requestWnet = wnetController;
        this.requestRnn = rnnController;
        this.requestAiml = aimlController;
    }

    /***
     * Creates n requests, one to each back-end server and starts them async
     * @param devId
     * @param aiid
     * @param chatId
     * @param question
     * @param history
     * @param topicIn
     * @throws AiServicesException
     */
    public void startChatRequests(final String devId, final UUID aiid, final UUID chatId, final String question,
                                  final String history, final String topicIn)
            throws AiServicesException, RequestBase.AiControllerException, ServerMetadata.NoServerAvailable {

        // calculate the exact deadline for this group of requests
        this.requestDeadline = this.tools.getTimestamp() + this.config.getBackendCombinedRequestTimeoutMs();

        // generate the parameters to send
        HashMap<String, String> parameters = new HashMap<String, String>() {{
            put("chatId", chatId.toString());
            put("history", history);
            put("topic", topicIn);
            put("q", question);
        }};
        List<Pair<String, UUID>> ais = this.getLinkedBotsAiids(devId, aiid);

        // If this AI is linked to the AIML "bot" then we need to issue a chat request to the AIML backend as well
        boolean usedAimlBot = false;
        HashSet<UUID> aimlBotIdsSet = new HashSet<>(this.config.getAimlBotAiids());
        if (!aimlBotIdsSet.isEmpty()) {
            Set<UUID> usedAimlAis = ais.stream().map(Pair::getB).collect(Collectors.toSet());
            // intersect the two sets to usedAimlAis retains only the AIML ais in use
            usedAimlAis.retainAll(aimlBotIdsSet);
            if (!usedAimlAis.isEmpty()) {
                List<Pair<String, UUID>> listAis = new ArrayList<>();
                usedAimlAis.forEach(x -> listAis.add(new Pair<>(/* ignored at the moment */devId, x)));
                this.aimlFutures = this.requestAiml.issueChatRequests(parameters, listAis);
                usedAimlBot = true;
                // remove the aiml bots ais from the list of ais
                List<Pair<String, UUID>> newList = new ArrayList<>();
                for (Pair<String, UUID> ai : ais) {
                    if (!usedAimlAis.contains(ai.getB())) {
                        newList.add(ai);
                    }
                }
                ais = newList;
            }
        }

        // find out which servers can chat with this AI
        Set<BackendServerType> canChatWith = canChatWithAi(devId, aiid);

        // make copies of the AI lists
        List<Pair<String, UUID>> wnetAIs = new ArrayList<>(ais);
        List<Pair<String, UUID>> rnnAIs = new ArrayList<>(ais);

        // add the AI to the list if the server can chat
        if (canChatWith.contains(BackendServerType.WNET)) {
            wnetAIs.add(new Pair<String, UUID>(devId, aiid));
        }
        if (canChatWith.contains(BackendServerType.RNN)) {
            rnnAIs.add(new Pair<String, UUID>(devId, aiid));
        }

        // If we're issuing a chat request but there are no AIs available to serve it, just fail
        if (wnetAIs.isEmpty() && rnnAIs.isEmpty() && !usedAimlBot) {
            throw new AiNotReadyToChat("No AIs ready to chat");
        }

        if (!wnetAIs.isEmpty()) {
            this.wnetFutures = this.requestWnet.issueChatRequests(parameters, wnetAIs);
        }
        if (!rnnAIs.isEmpty()) {
            this.rnnFutures = this.requestRnn.issueChatRequests(parameters, rnnAIs);
        }
    }

    /***
     * Waits for WNET calls to complete and returns the result
     * @return map of results, or null if there were no requests
     * @throws RequestBase.AiControllerException
     */
    public Map<UUID, ChatResult> awaitWnet() throws RequestBase.AiControllerException {
        if (this.wnetFutures != null) {
            return this.requestWnet.waitForAll(this.wnetFutures, getRemainingTime());
        }
        return null;
    }

    /***
     * Waits for AIML calls to complete and returns the result
     * @return map of results, or null if there were no requests
     * @throws RequestBase.AiControllerException
     */
    public Map<UUID, ChatResult> awaitAiml() throws RequestBase.AiControllerException {
        if (this.aimlFutures != null) {
            return this.requestAiml.waitForAll(this.aimlFutures, getRemainingTime());
        }
        return null;
    }

    /***
     * Waits for RNN calls to complete and returns the result
     * @return map of results, or null if there were no requests
     * @throws RequestBase.AiControllerException
     */
    public Map<UUID, ChatResult> awaitRnn() throws RequestBase.AiControllerException {
        if (this.rnnFutures != null) {
            return this.requestRnn.waitForAll(this.rnnFutures, getRemainingTime());
        }
        return null;
    }

    public void abandonCalls() {
        this.requestWnet.abandonCalls();
        this.requestRnn.abandonCalls();
        this.requestAiml.abandonCalls();
    }

    public List<Pair<String, UUID>> getLinkedBotsAiids(final String devId, final UUID aiid)
            throws RequestBase.AiControllerException {
        List<Pair<String, UUID>> ais = new ArrayList<>();
        try {
            List<AiBot> bots = this.database.getBotsLinkedToAi(devId, aiid);
            for (AiBot bot : bots) {
                ais.add(new Pair<>(bot.getDevId(), bot.getAiid()));
            }
        } catch (Database.DatabaseException ex) {
            throw new RequestBase.AiControllerException("Couldn't get the list of linked bots");
        }
        return ais;
    }

    /***
     * Determine whether the AI is in a state where the user can interact with it
     * @param devId dev owner
     * @param aiid id
     * @return a set of servers that can interact with the AI (empty set if none)
     */
    public Set<BackendServerType> canChatWithAi(final String devId, final UUID aiid) {
        // by default chat with nothing
        HashSet<BackendServerType> chatSet = new HashSet<>();
        BackendStatus result = null;
        try {
            // try to get the real status from the database
            result = this.database.getAIStatusReadOnly(devId, aiid, this.serializer);
        } catch (Database.DatabaseException ex) {
            // if it fails, log the error and keep the set null
            this.logger.logException(LOGFROM, ex);
        }
        if (result != null) {
            // get the status of each backend server for this ai
            TrainingStatus wnetStatus = result.getEngineStatus(BackendServerType.WNET).getTrainingStatus();
            TrainingStatus rnnStatus = result.getEngineStatus(BackendServerType.RNN).getTrainingStatus();

            // wnet can only chat if training is complete
            if (wnetStatus == TrainingStatus.AI_TRAINING_COMPLETE) {
                chatSet.add(BackendServerType.WNET);
            }
            // rnn can chat if training is complete, stopped or in progress
            if (rnnStatus == TrainingStatus.AI_TRAINING_COMPLETE ||
                    rnnStatus == TrainingStatus.AI_TRAINING_STOPPED ||
                    rnnStatus == TrainingStatus.AI_TRAINING) {
                chatSet.add(BackendServerType.RNN);
            }
        }
        return chatSet;
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
        public AiNotReadyToChat(final String message) {
            super(message);
        }
    }
}
