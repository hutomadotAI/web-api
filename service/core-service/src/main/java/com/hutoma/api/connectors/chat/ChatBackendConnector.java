package com.hutoma.api.connectors.chat;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.ITrackedThreadSubPool;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.*;

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
    private final ITrackedThreadSubPool threadSubPool;
    private final ControllerConnector controllerConnector;
    private final Provider<ChatBackendRequester> requesterProvider;

    private long deadlineTimestamp;

    @Inject
    public ChatBackendConnector(final JerseyClient jerseyClient, final Tools tools, final Config config,
                                final ITrackedThreadSubPool threadSubPool,
                                final ILogger logger, final JsonSerializer serializer,
                                final ControllerConnector controllerConnector,
                                final Provider<ChatBackendRequester> requesterProvider) {
        this.jerseyClient = jerseyClient;
        this.tools = tools;
        this.config = config;
        this.threadSubPool = threadSubPool;
        this.logger = logger;
        this.serializer = serializer;
        this.controllerConnector = controllerConnector;
        this.requesterProvider = requesterProvider;
    }

    protected abstract BackendServerType getServerType();

    List<RequestInProgress> issueChatRequests(final Map<String, String> chatParams,
                                              final List<AiIdentity> ais,
                                              final ChatState chatState)
            throws NoServerAvailableException, AiControllerException {
        List<RequestCallable> callables = new ArrayList<>();

        // calculate the time at which we give up on all calls to backend servers
        this.deadlineTimestamp = this.tools.getTimestamp() + this.config.getBackendCombinedRequestTimeoutMs();

        ServerEndpointRequestMulti serverEndpointRequestMulti = new ServerEndpointRequestMulti();
        ais.forEach(ai -> {
            serverEndpointRequestMulti.add(new ServerEndpointRequestMulti.ServerEndpointRequest(
                    ai.getAiid(),
                    ai.getLanguage(),
                    ai.getServerVersion(),
                    Collections.emptyList()));
        });
        Map<UUID, ApiServerEndpointMulti.ServerEndpointResponse> endpointMap = ais.isEmpty()
                ? new HashMap<>()
                : this.controllerConnector.getBackendChatEndpointMulti(serverEndpointRequestMulti, this.serializer);

        // check that there is an endpoint for each ai we need to call
        for (AiIdentity ai : ais) {
            UUID aiid = ai.getAiid();
            ApiServerEndpointMulti.ServerEndpointResponse endpoint = endpointMap.get(aiid);
            if ((endpoint == null) || (StringUtils.isEmpty(endpoint.getServerUrl()))) {
                throw new NoServerAvailableException(String.format("No server available for %s for %s on %s",
                        aiid.toString(), RequestFor.Chat.toString(), this.getServerType().value()));
            }
        }

        // generate the callables
        for (AiIdentity ai : ais) {
            UUID aiid = ai.getAiid();
            ApiServerEndpointMulti.ServerEndpointResponse endpoint = endpointMap.get(aiid);
            ChatBackendRequester chatBackendRequester = this.requesterProvider.get()
                    .initialise(this.controllerConnector, ai, endpoint, chatParams, chatState, this.deadlineTimestamp);
            callables.add(new RequestCallable(chatBackendRequester, endpoint.getServerIdentifier()));
        }

        // start the calls
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

                LogMap logMap = LogMap.map("Op", "AwaitChat");

                if (result != null) {
                    logMap.add("Attempts", result.getAttemptNumber());
                    logMap.add("TotalRequestDurationMs", result.getDurationMs());
                    logMap.add("CallDurationMs", result.getChatCallDurationMs());
                    Response.StatusType statusInfo = result.getResponse().getStatusInfo();
                    switch (statusInfo.getStatusCode()) {
                        case HttpURLConnection.HTTP_OK:
                            break;
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            this.logger.logError(this.getLogFrom(),
                                    "AI not found: " + result.getAiid(),
                                    logMap);
                            throw new AiNotFoundException("AI was not found");
                        default:
                            // generate error text from the HTTP result only
                            // (ignores response body for now)
                            String errorText = String.format("http error: %d %s)",
                                    statusInfo.getStatusCode(),
                                    statusInfo.getReasonPhrase());
                            this.logger.logError(this.getLogFrom(), errorText, logMap);
                            throw new AiControllerException(errorText);
                    }

                    if (statusInfo.getStatusCode() == HttpURLConnection.HTTP_OK) {
                        // otherwise attempt to deserialize the chat result
                        try {
                            String content = result.getResponse().readEntity(String.class);
                            this.logger.logDebug("requestbase",
                                    "chat response from " + result.getEndpoint(),
                                    logMap);
                            ChatResult chatResult = (ChatResult) this.serializer.deserialize(content, ChatResult.class);
                            UUID aiid = result.getAiid();
                            chatResult.setAiid(aiid);
                            chatResult.setElapsedTime(result.getDurationMs() / 1000.0);
                            map.put(aiid, chatResult);
                        } catch (JsonParseException jpe) {
                            this.logger.logException(this.getLogFrom(), jpe, logMap);
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
            if (invocationResult == null) {
                throw new AiControllerTimeoutException(String.format("No response from %s",
                        requestInProgress.getEndpointIdentifier()));
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
            if (cause instanceof NoServerAvailableException.ServiceTooBusyException) {
                throw new AiControllerException(
                        String.format("Chat-core too busy to accept the chat (tried %d servers)",
                                ((NoServerAvailableException.ServiceTooBusyException)cause).getAlreadyTried().size()));
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

        public AiControllerException(String message) {
            super(message);
        }

        public AiControllerException(final Throwable cause) {
            super(cause);
        }
    }

    static class AiControllerTimeoutException extends AiControllerException {
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
