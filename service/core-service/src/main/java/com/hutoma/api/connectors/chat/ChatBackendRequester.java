package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.logging.ILogger;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import com.hutoma.api.common.FeatureToggler;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public class ChatBackendRequester implements Callable<InvocationResult> {

    private static final String AI_HASH_PARAM = "ai_hash";

    private final Tools tools;
    private final JerseyClient jerseyClient;
    private final Config config;
    private final JsonSerializer serializer;
    private final FeatureToggler featureToggler;
    private final ILogger logger;

    private ControllerConnector controllerConnector;
    private AiIdentity ai;
    private ApiServerEndpointMulti.ServerEndpointResponse currentEndpointResponse;
    private ChatState chatState;
    private Map<String, String> chatParams;
    private long requesterStartTime;
    private long deadlineTimestamp;
    private int callAttempts;

    private InvocationResult lastAttemptResult = null;

    @Inject
    public ChatBackendRequester(final Tools tools,
                                final JerseyClient jerseyClient,
                                final Config config,
                                final JsonSerializer serializer,
                                final FeatureToggler featureToggler,
                                final ILogger logger) {
        this.tools = tools;
        this.jerseyClient = jerseyClient;
        this.config = config;
        this.serializer = serializer;
        this.featureToggler = featureToggler;
        this.logger = logger;
    }

    ChatBackendRequester initialise(final ControllerConnector controllerConnector,
                                    final AiIdentity ai,
                                    final ApiServerEndpointMulti.ServerEndpointResponse serverEndpointResponse,
                                    final Map<String, String> chatParams,
                                    final ChatState chatState,
                                    final long deadlineTimestamp) {
        this.ai = ai;
        this.currentEndpointResponse = serverEndpointResponse;
        this.chatState = chatState;
        this.chatParams = chatParams;
        this.deadlineTimestamp = deadlineTimestamp;
        this.controllerConnector = controllerConnector;
        return this;
    }

    @Override
    public InvocationResult call() throws Exception {

        this.requesterStartTime = this.tools.getTimestamp();

        // calculate time remaining but shave off a few milliseconds so that we
        // get a chance to return the last error in a series of errors rather
        // than just timing out on the last of a series of requests
        long timeRemaining = this.deadlineTimestamp - this.requesterStartTime - 10L;

        ApiServerEndpointMulti.ServerEndpointResponse serverEndpointResponse = this.currentEndpointResponse;
        ArrayList<String> alreadyTried = new ArrayList<>();
        this.callAttempts = 0;

        boolean retry;
        do {
            retry = false;
            this.lastAttemptResult = callBackend(serverEndpointResponse, timeRemaining, ++this.callAttempts);
            if (this.lastAttemptResult != null) {
                Response response = this.lastAttemptResult.getResponse();
                if ((response != null) && (response.getStatus() == HttpURLConnection.HTTP_UNAVAILABLE)) {

                    // remember the servers we already tried
                    alreadyTried.add(serverEndpointResponse.getServerIdentifier());

                    // call the controller again to get an endpoint
                    ServerEndpointRequestMulti serverEndpointRequestMulti = new ServerEndpointRequestMulti();
                    serverEndpointRequestMulti.add(new ServerEndpointRequestMulti.ServerEndpointRequest(
                            this.ai.getAiid(), this.ai.getLanguage(), this.ai.getServerVersion(), alreadyTried));
                    Map<UUID, ApiServerEndpointMulti.ServerEndpointResponse> endpointMap =
                            this.controllerConnector.getBackendChatEndpointMulti(
                                    serverEndpointRequestMulti,
                                    this.serializer);

                    // make sure that we got a valid endpoint back from the controller
                    serverEndpointResponse = endpointMap.get(this.ai.getAiid());
                    if ((serverEndpointResponse == null)
                            || StringUtils.isEmpty(serverEndpointResponse.getServerIdentifier())) {

                        // if not, throw a descriptive exception
                        throw new NoServerAvailableException.ServiceTooBusyException(alreadyTried);
                    }

                    // recalculate the amount of time we have left
                    timeRemaining = this.deadlineTimestamp - this.tools.getTimestamp();
                    if (timeRemaining > 0) {
                        retry = true;
                    }
                }
            }
        } while (retry);
        return this.lastAttemptResult;
    }

    protected InvocationResult callBackend(final ApiServerEndpointMulti.ServerEndpointResponse endpointResponse,
                                           final long timeRemaining,
                                           final int attemptNumber) {

        Map<String, String> chatParamsThisAi = new HashMap<>(this.chatParams);
        if (this.ai.getAiid().equals(this.chatState.getLockedAiid())) {
            chatParamsThisAi = new HashMap<>(this.chatParams);
            chatParamsThisAi.put("history", this.chatState.getHistory());
            chatParamsThisAi.put("topic", this.chatState.getTopic());
        }

        JerseyWebTarget target;
        long startTime = this.tools.getTimestamp();
        Response response;

        if (featureToggler.getStateForAiid(
                this.ai.getDevId(),
                this.ai.getAiid(),
                "entity-value-replacement") == FeatureToggler.FeatureState.T1) {
            logger.logUserTraceEvent("ChatBackendRequester",
                    "entity-value-replacement feature requested",
                    this.ai.getDevId().toString());
            // Construct payload
            EntityPayload body = new EntityPayload();
            body.conversation = chatParamsThisAi.get("q");
            body.entities = this.chatState.getCandidateValues();
            String bodyJson = serializer.serialize(body);

            logger.logUserTraceEvent("ChatBackendRequester",
                    "sending: " + bodyJson,
                    this.ai.getDevId().toString());

            target = this.jerseyClient
                    .target(endpointResponse.getServerUrl())
                    .path(this.ai.getDevId().toString())
                    .path(this.ai.getAiid().toString())
                    .path("chat_v2");

            response = target.request()
                    .property(CONNECT_TIMEOUT, (int) this.config.getBackendConnectCallTimeoutMs())
                    .property(READ_TIMEOUT, (int) timeRemaining)
                    .post(Entity.json(bodyJson));
        } else {
            target = this.jerseyClient
                    .target(endpointResponse.getServerUrl())
                    .path(this.ai.getDevId().toString())
                    .path(this.ai.getAiid().toString())
                    .path("chat");

            target = addTargetParameters(endpointResponse, chatParamsThisAi, target);

            response = target.request()
                    .property(CONNECT_TIMEOUT, (int) this.config.getBackendConnectCallTimeoutMs())
                    .property(READ_TIMEOUT, (int) timeRemaining)
                    .get();
        }

        // whatever the response, buffer it and close the underlying structure
        if (response != null) {
            response.bufferEntity();
        }

        long timeNow = this.tools.getTimestamp();
        return new InvocationResult(response, endpointResponse.getServerUrl(),
                timeNow - startTime, timeNow - this.requesterStartTime,
                attemptNumber, this.ai.getAiid());
    }

    protected JerseyWebTarget addTargetParameters(final ApiServerEndpointMulti.ServerEndpointResponse endpointResponse,
                                                final Map<String, String> chatParamsThisAi, JerseyWebTarget target) {
        // make a copy of the params list but ensure that we have empty strings in the place of nulls
        Map<String, Object> queryParamsWithoutNulls = chatParamsThisAi.entrySet()
                .stream()
                .collect(Collectors.toMap(p -> p.getKey(),
                        p -> StringUtils.defaultString(p.getValue())));

        // add the hashcode to the query string
        queryParamsWithoutNulls.put(AI_HASH_PARAM, StringUtils.defaultString(endpointResponse.getHash()));

        // create template
        for (String param : queryParamsWithoutNulls.keySet()) {
            target = target.queryParam(param, String.format("{%s}", param));
        }

        // encode parameters into template
        target = target.resolveTemplates(queryParamsWithoutNulls);
        return target;
    }

    private static class EntityPayload {
        private String conversation;
        private Map<String, List<String>> entities;

        EntityPayload() {
            entities = new HashMap<String, List<String>>();
        }
    }
}
