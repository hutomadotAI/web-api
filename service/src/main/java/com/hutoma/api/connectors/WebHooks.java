package com.hutoma.api.connectors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookPayload;
import com.hutoma.api.containers.sub.WebHookResponse;

import org.glassfish.jersey.client.JerseyClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Management and execution of WebHooks.
 */
public class WebHooks {
    private static final String LOGFROM = "webhooks";
    private final Database database;
    private final ILogger logger;
    private final JsonSerializer serializer;
    private final JerseyClient jerseyClient;

    @Inject
    public WebHooks(final Database database, final ILogger logger, final JsonSerializer serializer,
                    final JerseyClient jerseyClient) {
        this.database = database;
        this.logger = logger;
        this.serializer = serializer;
        this.jerseyClient = jerseyClient;
    }

    /***
     * Executes the WebHook for an intent.
     * @param intent The intent.
     * @param chatResult The chat result for the request.
     * @return a WebHookResponse containing the returned data.
     * @throws IOException if the endpoint cannot be accessed.
     */
    public WebHookResponse executeWebHook(final MemoryIntent intent, final ChatResult chatResult, final String devId) {
        if (intent == null) {
            this.logger.logError(LOGFROM, "Invalid parameters passed.");
            return null;
        }

        WebHook webHook = null;

        try {
            webHook = this.database.getWebHook(intent.getAiid(), intent.getName());
        } catch (Database.DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devId, e);
            return null;
        }

        if (webHook == null) {
            this.logger.logUserErrorEvent(LOGFROM,
                    "WebHook not found at execution for intent %s in aiid %s",
                    devId,
                    LogMap.map("Intent", intent.getName()).put("AIID", intent.getAiid()));
            return null;
        }

        WebHookPayload payload = new WebHookPayload(intent, chatResult);

        String jsonPayload = null;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook Payload Serialisation Failed", devId, e);
            return null;
        }

        Response response = null;
        try {
            response = this.jerseyClient.target(webHook.getEndpoint())
                    .property("Content-Type", "application/json")
                    .property("Content-Length", String.valueOf(jsonPayload.length()))
                    .request()
                    .post(Entity.json(jsonPayload));
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Execution Failed", devId, e);
            return null;
        }

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            this.logger.logUserWarnEvent(LOGFROM,
                    "WebHook Failed (%s): intent %s for aiid %s at %s",
                    devId,
                    LogMap.map("ResponseStatus", response.getStatus()).put("Intent", intent.getName())
                            .put("AIID", intent.getAiid()).put("Endpoint", webHook.getEndpoint()));
            return null;
        }

        response.bufferEntity();
        WebHookResponse webHookResponse = this.deserializeResponse(response);
        response.close();

        this.logger.logInfo(LOGFROM, String.format("Successfully executed webhook for aiid %s and intent %s",
                intent.getAiid(), intent.getName()));
        return webHookResponse;
    }

    /***
     * Deserializes the json response to a WebHookResponse.
     * @param response the Response to deserialize.
     * @return The deserialized WebHookResponse or null.
     */
    public WebHookResponse deserializeResponse(final Response response) {
        try {
            return (WebHookResponse) this.serializer.deserialize(response.readEntity(String.class),
                    WebHookResponse.class);
        } catch (JsonParseException e) {
            this.logger.logException(LOGFROM, e);
            return null;
        }
    }

    /***
     * Determines whether an active WebHook exists.
     * @param intent The intent.
     * @return true if an active WebHook exists, else false.
     * @throws Database.DatabaseException if the WebHook cannot be retrieved.
     */
    public boolean activeWebhookExists(final MemoryIntent intent, final String devId) {
        WebHook webHook = null;
        try {
            webHook = this.database.getWebHook(intent.getAiid(), intent.getName());
        } catch (Database.DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devId, e);
        }

        return webHook != null && webHook.isEnabled();
    }
}
