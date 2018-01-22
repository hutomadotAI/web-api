package com.hutoma.api.connectors.chat;

import com.google.common.base.Strings;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.containers.AiDevId;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.sub.ChatState;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public class ChatBackendRequester implements Callable<InvocationResult> {

    private static final String AI_HASH_PARAM = "ai_hash";

    private final Tools tools;
    private final JerseyClient jerseyClient;
    private final Config config;

    private AiDevId ai;
    private ApiServerEndpointMulti.ServerEndpointResponse endpointResponse;
    private ChatState chatState;
    private Map<String, String> chatParams;
    private long deadlineTimestamp;

    @Inject
    public ChatBackendRequester(final Tools tools, final JerseyClient jerseyClient, final Config config) {
        this.tools = tools;
        this.jerseyClient = jerseyClient;
        this.config = config;
    }

    ChatBackendRequester initialise(final AiDevId ai,
                                    final ApiServerEndpointMulti.ServerEndpointResponse serverEndpointResponse,
                                    final Map<String, String> chatParams,
                                    final ChatState chatState,
                                    final long deadlineTimestamp) {
        this.ai = ai;
        this.endpointResponse = serverEndpointResponse;
        this.chatState = chatState;
        this.chatParams = chatParams;
        this.deadlineTimestamp = deadlineTimestamp;
        return this;
    }

    @Override
    public InvocationResult call() throws Exception {

        InvocationResult invocationResult;
        long timeRemaining = this.deadlineTimestamp - tools.getTimestamp();

        boolean retry;
        do {
            retry = false;
            invocationResult = callBackend(timeRemaining);
            if (invocationResult != null) {
                Response response = invocationResult.getResponse();
                if ((response != null) && (response.getStatus() == HttpURLConnection.HTTP_UNAVAILABLE)) {

                    // recalculate the amount of time we have left
                    timeRemaining = this.deadlineTimestamp - tools.getTimestamp();

                    // if there is time, make the call again
                    // TODO call the controller to get a new endpoint here
                    if (timeRemaining > 0) {
                        retry = true;
                    }
                }
            }
        } while (retry);
        return invocationResult;
    }

    protected InvocationResult callBackend(final long timeRemaining) {

        Map<String, String> chatParamsThisAi = new HashMap<>(chatParams);
        if (ai.getAiid().equals(chatState.getLockedAiid())) {
            chatParamsThisAi = new HashMap<>(chatParams);
            chatParamsThisAi.put("history", chatState.getHistory());
            chatParamsThisAi.put("topic", chatState.getTopic());
        }

        JerseyWebTarget target = this.jerseyClient
                .target(endpointResponse.getServerUrl())
                .path(ai.getDevId().toString())
                .path(ai.getAiid().toString())
                .path("chat");

        // make a copy of the params list but ensure that we have empty strings in the place of nulls
        Map<String, Object> queryParamsWithoutNulls = chatParamsThisAi.entrySet()
                .stream()
                .collect(Collectors.toMap(p -> p.getKey(),
                        p -> Strings.nullToEmpty(p.getValue())));

        // add the hashcode to the query string
        queryParamsWithoutNulls.put(AI_HASH_PARAM, Strings.nullToEmpty(endpointResponse.getHash()));

        // create template
        for (String param : queryParamsWithoutNulls.keySet()) {
            target = target.queryParam(param, String.format("{%s}", param));
        }

        // encode parameters into template
        target = target.resolveTemplates(queryParamsWithoutNulls);

        long startTime = this.tools.getTimestamp();
        Response response = target.request()
                .property(CONNECT_TIMEOUT, (int) this.config.getBackendConnectCallTimeoutMs())
                .property(READ_TIMEOUT, (int) timeRemaining)
                .get();

        // whatever the response, buffer it and close the underlying structure
        if (response != null) {
            response.bufferEntity();
        }

        return new InvocationResult(ai.getAiid(), response, endpointResponse.getServerUrl(),
                this.tools.getTimestamp() - startTime);

    }
}
