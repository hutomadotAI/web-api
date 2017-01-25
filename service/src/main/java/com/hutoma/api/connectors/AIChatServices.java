package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatResult;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Created by David MG on 01/12/2016.
 */
public class AIChatServices extends ServerConnector {

    public static final String WNET = "wnet";
    public static final String AIML = "aiml";
    public static final String RNN = "rnn";
    // active requests
    private HashMap<String, Future<InvocationResult>> requestMap;

    @Inject
    public AIChatServices(final Database database, final ILogger logger, final JsonSerializer serializer,
                          final Tools tools, final Config config, final JerseyClient jerseyClient) {
        super(database, logger, serializer, tools, config, jerseyClient);
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

        // store the call map
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();

        // generate the parameters to send
        HashMap<String, String> parameters = new HashMap<String, String>() {{
            put("chatId", chatId.toString());
            put("history", history);
            put("topic", topicIn);
            put("q", question);
        }};

        // create the calls
        createCallable(WNET, this.config.getWnetChatEndpoint(), devId, aiid, parameters, callables);
        createCallable(AIML, this.config.getAimlChatEndpoint(), devId, aiid, parameters, callables);
        createCallable(RNN, this.config.getRnnChatEndpoint(), devId, aiid, parameters, callables);

        // execute the calls
        this.requestMap = this.execute(callables);
    }

    /***
     * Waits for WNET call to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public ChatResult awaitWnet() throws AiServicesException {
        return this.waitForAndGet(WNET);
    }

    /***
     * Waits for AIML call to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public ChatResult awaitAiml() throws AiServicesException {
        return this.waitForAndGet(AIML);
    }

    /***
     * Waits for RNN call to complete and returns the result
     * @return
     * @throws AiServicesException
     */
    public ChatResult awaitRnn() throws AiServicesException {
        return this.waitForAndGet(RNN);
    }

    private ChatResult waitForAndGet(String label) throws AiServicesException {

        // have we made the call at all?
        if (this.requestMap == null) {
            throw new AiServicesException("Can't await before making any calls");
        }

        // get and wait for the call to complete
        InvocationResult response = null;
        try {
            if (!this.requestMap.get(label).isDone()) {
                response = this.waitFor(label, this.requestMap);
            } else {
                response = this.requestMap.get(label).get();
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new AiServicesException(String.format("%s: %s", label, e.getMessage()));
        }

        Response.StatusType statusInfo = response.getResponse().getStatusInfo();
        switch (statusInfo.getStatusCode()) {
            case HttpURLConnection.HTTP_OK:
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new AiNotFoundException(label);
            default:
                // generate error text from the HTTP result only
                // (ignores response body for now)
                String errorText = String.format("%s http error: %d %s)",
                        label,
                        statusInfo.getStatusCode(),
                        statusInfo.getReasonPhrase());
                throw new AiServicesException(errorText);
        }

        // otherwise attempt to deserialize the chat result
        try {
            String content = response.getResponse().readEntity(String.class);
            ITelemetry.addTelemetryEvent(this.logger, "chat response", new HashMap<String, String>() {{
                this.put("From", label);
            }});
            ChatResult chatResult = new ChatResult((ChatResult) this.serializer.deserialize(content, ChatResult.class));
            chatResult.setElapsedTime(response.getDurationMs() / 1000.0);
            return chatResult;
        } catch (JsonParseException jpe) {
            throw new AiServicesException(jpe.getMessage());
        }
    }

    private void createCallable(final String label, final String endpoint,
                                final String devId, final UUID aiid,
                                final HashMap<String, String> params,
                                final HashMap<String, Callable<InvocationResult>> callables) {

        // create call to back-end chat endpoints
        // e.g.
        //     http://wnet:8083/ai/c930c441-bd90-4029-b2df-8dbb08b37b32/9f376458-20ca-4d13-a04c-4d835232b90b/chat
        //     ?q=my+name+is+jim&chatId=8fb944b8-d2d0-4a42-870b-4347c9689fae&topic=&history=
        JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId).path(aiid.toString()).path("chat");
        for (Map.Entry<String, String> param : params.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }

        final JerseyInvocation.Builder builder = target.request();
        callables.put(label, () -> {
            long startTime = AIChatServices.this.tools.getTimestamp();
            Response response = builder.get();
            return new InvocationResult(response, endpoint, AIChatServices.this.tools.getTimestamp() - startTime);
        });
    }

    public static class AiNotFoundException extends AiServicesException {
        public AiNotFoundException(final String message) {
            super(message);
        }
    }

    public static class AiRejectedStatusException extends AiServicesException {
        public AiRejectedStatusException(final String message) {
            super(message);
        }
    }

}
