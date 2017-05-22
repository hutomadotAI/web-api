package com.hutoma.api.controllers;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AiDevId;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.controllers.ControllerBase.RequestFor;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Base class for backend controllers.
 */
public abstract class RequestBase {

    private static final String AI_HASH_PARAM = "ai_hash";

    protected final JerseyClient jerseyClient;
    protected final Tools tools;
    protected final Config config;
    protected final ILogger logger;
    protected final JsonSerializer serializer;
    protected final ControllerBase controller;
    private final ThreadSubPool threadSubPool;

    @Inject
    public RequestBase(final JerseyClient jerseyClient, final Tools tools, final Config config,
                       final ThreadSubPool threadSubPool,
                       final ILogger logger, final JsonSerializer serializer, final ControllerBase controller) {
        this.jerseyClient = jerseyClient;
        this.tools = tools;
        this.config = config;
        this.threadSubPool = threadSubPool;
        this.logger = logger;
        this.serializer = serializer;
        this.controller = controller;
    }

    public List<RequestInProgress> issueChatRequests(final Map<String, String> chatParams,
                                                     final List<AiDevId> ais)
            throws ServerMetadata.NoServerAvailable {
        List<RequestCallable> callables = new ArrayList<>();

        for (AiDevId ai : ais) {
            IServerEndpoint endpoint = this.controller.getBackendEndpoint(ai.ai, RequestFor.Chat);
            callables.add(new RequestCallable(
                    createCallable(endpoint.getServerUrl(), ai.dev, ai.ai, chatParams,
                            this.controller.getHashCodeFor(ai.ai)),
                    endpoint.getServerIdentifier()));
        }

        return this.execute(callables);
    }

    public Map<UUID, ChatResult> waitForAll(final List<RequestInProgress> futures, final int timeoutMs)
            throws AiControllerException {
        // have we made the call at all?
        if (futures == null) {
            throw new AiControllerException("Can't await before making any calls");
        }

        Map<UUID, ChatResult> map = new HashMap<>();

        // get and wait for all the calls to complete
        for (RequestInProgress future : futures) {
            final InvocationResult result = waitForResult(future, timeoutMs);

            if (result != null) {
                Response.StatusType statusInfo = result.getResponse().getStatusInfo();
                switch (statusInfo.getStatusCode()) {
                    case HttpURLConnection.HTTP_OK:
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        this.logger.logError(this.getLogFrom(), "AI not found: " + result.getAiid());
                        throw new AiNotFoundException("AI was not found");
                    default:
                        // generate error text from the HTTP result only
                        // (ignores response body for now)
                        String errorText = String.format("http error: %d %s)",
                                statusInfo.getStatusCode(),
                                statusInfo.getReasonPhrase());
                        this.logger.logError(this.getLogFrom(), errorText);
                        throw new AiControllerException(errorText);
                }

                if (statusInfo.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    // otherwise attempt to deserialize the chat result
                    try {
                        String content = result.getResponse().readEntity(String.class);
                        this.logger.logDebug("requestbase", "chat response from " + result.getEndpoint());
                        ChatResult chatResult = new ChatResult((ChatResult)
                                this.serializer.deserialize(content, ChatResult.class));
                        UUID aiid = result.getAiid();
                        chatResult.setAiid(aiid);
                        chatResult.setElapsedTime(result.getDurationMs() / 1000.0);
                        map.put(aiid, chatResult);
                    } catch (JsonParseException jpe) {
                        this.logger.logException(this.getLogFrom(), jpe);
                        throw new AiControllerException(jpe.getMessage());
                    }
                }
            }

        }
        return map;
    }

    public void abandonCalls() {
        this.threadSubPool.cancelAll();
    }

    private InvocationResult waitForResult(final RequestInProgress requestInProgress, final int timeoutMs)
            throws AiControllerException {
        InvocationResult invocationResult;
        Future<InvocationResult> future = requestInProgress.getFuture();

        try {
            if (future.isDone()) {
                invocationResult = future.get();
            } else {
                invocationResult = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            }
        } catch (ExecutionException | TimeoutException ex) {
            throw new AiControllerException(String.format("Error executing request to %s: %s",
                    requestInProgress.getEndpointIdentifier(), ex.getClass().getSimpleName()));
        } catch (InterruptedException ex) {
            throw new AiControllerException(String.format("Interrupted request to %s",
                    requestInProgress.getEndpointIdentifier()));
        }
        return invocationResult;
    }

    public static class AiControllerException extends Exception {
        public AiControllerException(String message) {
            super(message);
        }
    }

    public static class AiNotFoundException extends AiControllerException {
        public AiNotFoundException(final String message) {
            super(message);
        }
    }

    public static class RequestInProgress {
        private Future<InvocationResult> future;
        private String endpointIdentifier;

        public RequestInProgress(final Future<InvocationResult> future, final String endpointIdentifier) {
            this.future = future;
            this.endpointIdentifier = endpointIdentifier;
        }

        public Future<InvocationResult> getFuture() {
            return this.future;
        }

        public String getEndpointIdentifier() {
            return this.endpointIdentifier;
        }
    }

    public static class RequestCallable {
        private Callable<InvocationResult> callable;
        private String endpointIdentifier;

        public RequestCallable(final Callable<InvocationResult> callable, final String endpointIdentifier) {
            this.callable = callable;
            this.endpointIdentifier = endpointIdentifier;
        }

        public Callable<InvocationResult> getCallable() {
            return this.callable;
        }

        public String getEndpointIdentifier() {
            return this.endpointIdentifier;
        }
    }

    protected Callable<InvocationResult> createCallable(final String endpoint, final UUID devId, final UUID aiid,
                                                        final Map<String, String> params, final String aiHash) {

        // create call to back-end chat endpoints
        // e.g.
        //     http://wnet:8083/ai/c930c441-bd90-4029-b2df-8dbb08b37b32/9f376458-20ca-4d13-a04c-4d835232b90b/chat
        //     ?q=my+name+is+jim&chatId=8fb944b8-d2d0-4a42-870b-4347c9689fae&topic=&history=
        JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId.toString()).path(aiid.toString()).path("chat");
        for (Map.Entry<String, String> param : params.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }
        // add the hashcode to the query string
        target = target.queryParam(AI_HASH_PARAM, aiHash);

        final JerseyInvocation.Builder builder = target.request();
        return () -> {
            long startTime = RequestBase.this.tools.getTimestamp();
            Response response = builder.get();
            return new InvocationResult(aiid, response, endpoint,
                    RequestBase.this.tools.getTimestamp() - startTime);
        };
    }

    protected List<RequestInProgress> execute(
            final List<RequestCallable> callables) {
        this.logger.logDebug(this.getLogFrom(), String.format("Issuing %d requests for %s",
                callables.size(), this.tools.getCallerMethod(3)));
        List<RequestInProgress> futures = new ArrayList<>();

        // get a named future for a named callable response
        callables.forEach((entry) -> {
            futures.add(new RequestInProgress(this.threadSubPool.submit(
                    entry.getCallable()),
                    entry.getEndpointIdentifier()));
        });

        return futures;
    }

    protected abstract String getLogFrom();
}
