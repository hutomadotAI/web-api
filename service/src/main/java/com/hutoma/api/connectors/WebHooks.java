package com.hutoma.api.connectors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
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
    public WebHooks(final Database database, final ILogger logger, final JsonSerializer serializer, final JerseyClient jerseyClient) {
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
                    intent.getName(),
                    intent.getAiid().toString());
            return null;
        }

        WebHookPayload payload = new WebHookPayload(intent, chatResult);
        WebHookResponse webHookResponse = null;

        String jsonPayload = null;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook Payload Serialisation Failed", devId, e);
            return null;
        }

        Response response = this.jerseyClient.target(webHook.getEndpoint())
                .property("Content-Type", "application/json")
                .property("Content-Length", String.valueOf(jsonPayload.length()))
                .request()
                .post(Entity.json(jsonPayload));

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            this.logger.logUserWarnEvent(LOGFROM,
                    "WebHook Failed (%s): intent %s for aiid %s at %s",
                    devId,
                    String.valueOf(response.getStatus()),
                    intent.getName(),
                    intent.getAiid().toString(),
                    webHook.getEndpoint());
            return null;
        }

        try {
            webHookResponse = (WebHookResponse) this.serializer.deserialize((String) response.getEntity(), WebHookResponse.class);
        } catch (JsonParseException e) {
            this.logger.logException(LOGFROM, e);
            return null;
        }

        this.logger.logInfo(LOGFROM, String.format("Successfully executed webhook for aiid %s and intent %s", intent.getAiid(), intent.getName()));
        return webHookResponse;
    }

    /***
     * Determines whether an active WebHook exists.
     * @param intent The intent.
     * @return true if an active WebHook exists, else false.
     * @throws Database.DatabaseException if the WebHook cannot be retrieved.
     */
    public boolean activeWebhookExists(final MemoryIntent intent, final String devId)  {
        WebHook webHook = null;
        try {
            webHook = this.database.getWebHook(intent.getAiid(), intent.getName());
        } catch (Database.DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devId, e);
        }

        return webHook != null && webHook.getEnabled();
    }
}
