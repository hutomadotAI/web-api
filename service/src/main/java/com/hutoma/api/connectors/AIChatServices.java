package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.controllers.InvocationResult;
import com.hutoma.api.controllers.RequestAiml;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.controllers.RequestRnn;
import com.hutoma.api.controllers.RequestWnet;

import org.glassfish.jersey.client.JerseyClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Inject;

/**
 * AI Chat services.
 */
public class AIChatServices extends ServerConnector {

    private static final int TIMEOUT_RNN_REQUESTS_MS = 5000;
    private static final int TIMEOUT_WNET_REQUESTS_MS = 2000;
    private static final int TIMEOUT_AIML_REQUESTS_MS = 2000;
    private final RequestWnet wnetController;
    private final RequestRnn rnnController;
    private final RequestAiml aimlController;
    private List<Future<InvocationResult>> wnetFutures;
    private List<Future<InvocationResult>> rnnFutures;
    private List<Future<InvocationResult>> aimlFutures;

    @Inject
    public AIChatServices(final Database database, final ILogger logger, final JsonSerializer serializer,
                          final Tools tools, final Config config, final JerseyClient jerseyClient,
                          final RequestWnet wnetController, final RequestRnn rnnController,
                          final RequestAiml aimlController) {
        super(database, logger, serializer, tools, config, jerseyClient);
        this.wnetController = wnetController;
        this.rnnController = rnnController;
        this.aimlController = aimlController;
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
                                  final String history, final String topicIn) throws AiServicesException {

        // generate the parameters to send
        HashMap<String, String> parameters = new HashMap<String, String>() {{
            put("chatId", chatId.toString());
            put("history", history);
            put("topic", topicIn);
            put("q", question);
        }};
        List<Pair<String, UUID>> ais = getLinkedBotsAndSelf(devId, aiid);
        this.wnetFutures = this.wnetController.issueChatRequests(parameters, ais);
        this.rnnFutures = this.rnnController.issueChatRequests(parameters, ais);
        this.aimlFutures = this.aimlController.issueChatRequests(parameters, ais);
    }

    /***
     * Waits for WNET calls to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public Map<UUID, ChatResult> awaitWnet() throws RequestBase.AiControllerException {
        return this.wnetController.waitForAll(this.wnetFutures, TIMEOUT_WNET_REQUESTS_MS);
    }

    /***
     * Waits for AIML calls to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public Map<UUID, ChatResult> awaitAiml() throws RequestBase.AiControllerException {
        return this.aimlController.waitForAll(this.aimlFutures, TIMEOUT_AIML_REQUESTS_MS);
    }

    /***
     * Waits for RNN calls to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public Map<UUID, ChatResult> awaitRnn() throws RequestBase.AiControllerException {
        return this.rnnController.waitForAll(this.rnnFutures, TIMEOUT_RNN_REQUESTS_MS);
    }

    public void abandonCalls() {
        this.wnetController.abandonCalls();
        this.rnnController.abandonCalls();
        this.aimlController.abandonCalls();
    }

    private List<Pair<String, UUID>> getLinkedBotsAndSelf(final String devId, final UUID aiid)
            throws AiServicesException {
        List<Pair<String, UUID>> ais = new ArrayList<>();
        // Add itself
        ais.add(new Pair<>(devId, aiid));
        try {
            List<AiBot> bots = this.database.getBotsLinkedToAi(devId, aiid);
            for (AiBot bot : bots) {
                ais.add(new Pair<>(bot.getDevId(), bot.getAiid()));
            }
        } catch (Database.DatabaseException ex) {
            throw new AiServicesException("Couldn't get the list of linked bots");
        }
        return ais;
    }
}
