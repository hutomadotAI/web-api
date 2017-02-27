package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiBot;
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

        // If the current AI is in a state that can handle chat requests, then include it
        if (this.canChatWithAi(devId, aiid)) {
            ais.add(new Pair<>(devId, aiid));
        }

        // If this AI is linked to the AIML "bot" then we need to issue a chat request to the AIML backend as well
        boolean usedAimlBot = false;
        List<String> aimlBotIds = this.config.getAimlBotAiids();
        if (!aimlBotIds.isEmpty()) {
            Set<UUID> usedAimlAis = ais.stream().map(Pair::getB).collect(Collectors.toSet());
            Set<UUID> aimlBotIdsSet = aimlBotIds.stream().map(UUID::fromString).collect(Collectors.toSet());
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

        // If we're issuing a chat request but there are no AIs available to serve it, just fail
        if (ais.isEmpty() && !usedAimlBot) {
            throw new AiNotReadyToChat("No AIs ready to chat");
        }

        if (!ais.isEmpty()) {
            this.wnetFutures = this.requestWnet.issueChatRequests(parameters, ais);
            this.rnnFutures = this.requestRnn.issueChatRequests(parameters, ais);
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

    public boolean canChatWithAi(final String devId, final UUID aiid) {
        try {
            ApiAi apiAi = this.database.getAI(devId, aiid, this.serializer);
            return apiAi.getSummaryAiStatus() == TrainingStatus.AI_TRAINING_COMPLETE
                    || apiAi.getSummaryAiStatus() == TrainingStatus.AI_TRAINING;
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
        }
        return false;
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
