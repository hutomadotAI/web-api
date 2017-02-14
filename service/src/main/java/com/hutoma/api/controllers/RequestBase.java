package com.hutoma.api.controllers;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
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

    public List<Future<InvocationResult>> issueChatRequests(final Map<String, String> chatParams,
                                                            final List<Pair<String, UUID>> ais) throws ServerMetadata.NoServerAvailable {
        List<Callable<InvocationResult>> callables = new ArrayList<>();

        for (Pair<String, UUID> ai : ais) {
            callables.add(createCallable(controller.getBackendEndpoint(ai.getB(), RequestFor.Chat),
                    ai.getA(), ai.getB(), chatParams));
        }

        return this.execute(callables);
    }

    public Map<UUID, ChatResult> waitForAll(final List<Future<InvocationResult>> futures, final int timeoutMs)
            throws AiControllerException {
        // have we made the call at all?
        if (futures == null) {
            throw new AiControllerException("Can't await before making any calls");
        }

        Map<UUID, ChatResult> map = new HashMap<>();

        // get and wait for all the calls to complete
        for (Future<InvocationResult> future : futures) {
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
                        ITelemetry.addTelemetryEvent(this.logger, "chat response", new HashMap<String, String>() {{
                            this.put("From", result.getEndpoint());
                        }});
                        ChatResult chatResult = new ChatResult((ChatResult)
                                this.serializer.deserialize(content, ChatResult.class));
                        chatResult.setElapsedTime(result.getDurationMs() / 1000.0);
                        map.put(result.getAiid(), chatResult);
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

    private InvocationResult waitForResult(final Future<InvocationResult> future, final int timeoutMs)
            throws AiControllerException {
        InvocationResult invocationResult;
        try {
            try {
                if (future.isDone()) {
                    invocationResult = future.get();
                } else {
                    invocationResult = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                }
            } catch (ExecutionException | TimeoutException ex) {
                this.logger.logException(this.getLogFrom(), ex);
                throw new AiControllerException("Error executing request: " + ex.getClass().getSimpleName());
            }
        } catch (InterruptedException ex) {
            this.logger.logException(this.getLogFrom(), ex);
            throw new AiControllerException("Interrupted");
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

    public static class AiRejectedStatusException extends AiControllerException {
        public AiRejectedStatusException(final String message) {
            super(message);
        }
    }

    protected Callable<InvocationResult> createCallable(final String endpoint, final String devId, final UUID aiid,
                                                        final Map<String, String> params) {

        // create call to back-end chat endpoints
        // e.g.
        //     http://wnet:8083/ai/c930c441-bd90-4029-b2df-8dbb08b37b32/9f376458-20ca-4d13-a04c-4d835232b90b/chat
        //     ?q=my+name+is+jim&chatId=8fb944b8-d2d0-4a42-870b-4347c9689fae&topic=&history=
        JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId).path(aiid.toString()).path("chat");
        for (Map.Entry<String, String> param : params.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }
        final JerseyInvocation.Builder builder = target.request();
        return () -> {
            long startTime = RequestBase.this.tools.getTimestamp();
            Response response = builder.get();
            return new InvocationResult(aiid, response, endpoint,
                    RequestBase.this.tools.getTimestamp() - startTime);
        };
    }

    protected List<Future<InvocationResult>> execute(
            final List<Callable<InvocationResult>> callables) {
        this.logger.logDebug(this.getLogFrom(), String.format("Issuing %d requests for %s",
                callables.size(), this.tools.getCallerMethod(3)));
        List<Future<InvocationResult>> futures = new ArrayList<>();

        // get a named future for a named callable response
        callables.forEach((entry) -> {
            futures.add(this.threadSubPool.submit(entry));
        });

        return futures;
    }

    protected abstract String getLogFrom();
}
