package com.hutoma.api.connectors.chat;

import com.google.common.base.Strings;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.containers.AiDevId;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.TrackedThreadSubPool;

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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

/**
 * Base class for backend controllers.
 */
public abstract class ChatBackendConnector {

    private static final String AI_HASH_PARAM = "ai_hash";

    protected final JerseyClient jerseyClient;
    protected final Tools tools;
    protected final Config config;
    protected final ILogger logger;
    protected final JsonSerializer serializer;
    private final TrackedThreadSubPool threadSubPool;
    private final ControllerConnector controllerConnector;

    @Inject
    public ChatBackendConnector(final JerseyClient jerseyClient, final Tools tools, final Config config,
                                final TrackedThreadSubPool threadSubPool,
                                final ILogger logger, final JsonSerializer serializer,
                                final ControllerConnector controllerConnector) {
        this.jerseyClient = jerseyClient;
        this.tools = tools;
        this.config = config;
        this.threadSubPool = threadSubPool;
        this.logger = logger;
        this.serializer = serializer;
        this.controllerConnector = controllerConnector;
    }

    protected abstract BackendServerType getServerType();

    List<RequestInProgress> issueChatRequests(final Map<String, String> chatParams,
                                                     final List<AiDevId> ais, ChatState chatState)
            throws NoServerAvailableException, AiControllerException {
        List<RequestCallable> callables = new ArrayList<>();

        for (AiDevId ai : ais) {
            Map<String, String> chatParamsThisAi = chatParams;
            if (ai.getAiid().equals(chatState.getLockedAiid())) {
                chatParamsThisAi = new HashMap<>(chatParams);
                chatParamsThisAi.put("history", chatState.getHistory());
                chatParamsThisAi.put("topic", chatState.getTopic());
            }
            IServerEndpoint endpoint = this.controllerConnector.getBackendEndpoint(ai.getAiid(), RequestFor.Chat);
            final String hash = this.controllerConnector.getHashCodeFor(ai.getAiid());
            // Note that it's ok to get a null/empty hash, we just won't use that endpoint.
            callables.add(new RequestCallable(
                    createCallable(endpoint.getServerUrl(), ai.getDevId(), ai.getAiid(), chatParamsThisAi, hash),
                    endpoint.getServerIdentifier()));
        }

        return this.execute(callables);
    }

    Map<UUID, ChatResult> waitForAll(final List<RequestInProgress> futures, final int timeoutMs)
            throws AiControllerException {
        // have we made the call at all?
        if (futures == null) {
            throw new AiControllerException("Can't await before making any calls");
        }

        Map<UUID, ChatResult> map = new HashMap<>();

        try {
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
                            ChatResult chatResult = (ChatResult) this.serializer.deserialize(content, ChatResult.class);
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
        } finally {
            futures.forEach(RequestInProgress::closeRequest);
        }
    }

    void abandonCalls() {
        this.threadSubPool.cancelAll();
    }

    protected InvocationResult waitForResult(final RequestInProgress requestInProgress, final int timeoutMs)
            throws AiControllerException {
        InvocationResult invocationResult;
        Future<InvocationResult> future = requestInProgress.getFuture();

        try {
            if (future.isDone()) {
                invocationResult = future.get();
            } else {
                invocationResult = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException | ExecutionException ex) {
            Throwable cause = ex;
            // execution exception may wrap a more interesting exception
            if ((ex instanceof ExecutionException) && (ex.getCause() != null)) {
                cause = ex.getCause();
            }
            // was this a timeout? throw a more specific exception
            if (cause instanceof TimeoutException) {
                throw new AiControllerTimeoutException(String.format("Timeout executing request to %s: %s",
                        requestInProgress.getEndpointIdentifier(), cause.getClass().getSimpleName()));
            }
            // otherwise throw the generic form of the error
            throw new AiControllerException(String.format("Error executing request to %s: %s",
                    requestInProgress.getEndpointIdentifier(), cause.getClass().getSimpleName()));
        } catch (InterruptedException ex) {
            throw new AiControllerException(String.format("Interrupted request to %s",
                    requestInProgress.getEndpointIdentifier()));
        }
        return invocationResult;
    }


    public static class AiControllerException extends Exception {
        AiControllerException(String message) {
            super(message);
        }
    }

    public static class AiControllerTimeoutException extends AiControllerException {
        AiControllerTimeoutException(String message) {
            super(message);
        }
    }

    public static class AiNotFoundException extends AiControllerException {
        AiNotFoundException(final String message) {
            super(message);
        }
    }

    static class RequestInProgress {
        private Future<InvocationResult> future;
        private String endpointIdentifier;

        RequestInProgress(final Future<InvocationResult> future, final String endpointIdentifier) {
            this.future = future;
            this.endpointIdentifier = endpointIdentifier;
        }

        Future<InvocationResult> getFuture() {
            return this.future;
        }

        String getEndpointIdentifier() {
            return this.endpointIdentifier;
        }

        /***
         * Closes the call no matter what state it is in
         * Suppresses an exception if one occurred
         */
        void closeRequest() {
            try {
                if (this.future != null) {
                    Response response = this.future.isDone()
                            ? this.future.get().getResponse()
                            : this.future.get(0, TimeUnit.MILLISECONDS).getResponse();
                    response.close();
                }
            } catch (Exception e) {
                // suppressed
            }
        }
    }

    public static class RequestCallable {
        private Callable<InvocationResult> callable;
        private String endpointIdentifier;

        RequestCallable(final Callable<InvocationResult> callable, final String endpointIdentifier) {
            this.callable = callable;
            this.endpointIdentifier = endpointIdentifier;
        }

        Callable<InvocationResult> getCallable() {
            return this.callable;
        }

        String getEndpointIdentifier() {
            return this.endpointIdentifier;
        }
    }

    Callable<InvocationResult> createCallable(final String endpoint, final UUID devId, final UUID aiid,
                                                        final Map<String, String> params, final String aiHash) {

        // create call to back-end chat endpoints
        // e.g.
        //     http://wnet:8083/ai/c930c441-bd90-4029-b2df-8dbb08b37b32/9f376458-20ca-4d13-a04c-4d835232b90b/chat
        //     ?q=my+name+is+jim&chatId=8fb944b8-d2d0-4a42-870b-4347c9689fae
        JerseyWebTarget target = this.jerseyClient
                .target(endpoint)
                .path(devId.toString())
                .path(aiid.toString())
                .path("chat");

        // make a copy of the params list but ensure that we have empty strings in the place of nulls
        Map<String, Object> queryParamsWithoutNulls = params.entrySet()
                .stream()
                .collect(Collectors.toMap(p -> p.getKey(),
                        p -> Strings.nullToEmpty(p.getValue())));

        // add the hashcode to the query string
        queryParamsWithoutNulls.put(AI_HASH_PARAM, Strings.nullToEmpty(aiHash));

        // create template
        for (String param : queryParamsWithoutNulls.keySet()) {
            target = target.queryParam(param, String.format("{%s}", param));
        }

        // encode parameters into template
        target = target.resolveTemplates(queryParamsWithoutNulls);

        final JerseyInvocation.Builder builder = target.request();
        return () -> {
            long startTime = ChatBackendConnector.this.tools.getTimestamp();
            Response response = builder
                    .property(CONNECT_TIMEOUT, (int) this.config.getBackendConnectCallTimeoutMs())
                    .property(READ_TIMEOUT, (int) this.config.getBackendCombinedRequestTimeoutMs())
                    .get();
            return new InvocationResult(aiid, response, endpoint,
                    ChatBackendConnector.this.tools.getTimestamp() - startTime);
        };
    }

    private List<RequestInProgress> execute(
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
