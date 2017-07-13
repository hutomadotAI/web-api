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

import org.apache.commons.codec.binary.Hex;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import java.net.HttpURLConnection;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Management and execution of WebHooks.
 */
public class WebHooks {
    private static final String LOGFROM = "webhooks";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
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
     * @param webHook The web hook.
     * @param intent The intent.
     * @param chatResult The chat result for the request.
     * @return a WebHookResponse containing the returned data.
     * @throws IOException if the endpoint cannot be accessed.
     */
    public WebHookResponse executeWebHook(final WebHook webHook, final MemoryIntent intent, final ChatResult chatResult, final UUID devId) {
        final String devIdString = devId.toString();
        if (webHook == null || intent == null) {
            this.logger.logError(LOGFROM, "Invalid parameters passed.");
            return null;
        }

        String secret;
        try {
            secret = this.database.getWebhookSecretForBot(intent.getAiid());
        } catch (Database.DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devIdString, e);
            return null;
        }

        WebHookPayload payload = new WebHookPayload(intent, chatResult);

        String jsonPayload = null;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook Payload Serialisation Failed", devIdString, e);
            return null;
        }

        byte[] payloadBytes;
        try {
            payloadBytes = jsonPayload.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // should never hit this for UTF-8
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook payload encoding failed", devIdString, e);
            return null;
        }

        String calculatedHash = getMessageHash(devIdString, secret, payloadBytes);
        if (calculatedHash == null) return null;

        Response response = null;
        try {
            response = this.jerseyClient.target(webHook.getEndpoint())
                    .property("Content-Type", "application/json")
                    .property("Content-Length", String.valueOf(payloadBytes.length))
                    .property("X-Hub-Signature", "sha256=" + calculatedHash)
                    .property(ClientProperties.CONNECT_TIMEOUT, 10000)
                    .property(ClientProperties.READ_TIMEOUT, 10000)
                    .request()
                    .post(Entity.json(payloadBytes));
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Execution Failed", devIdString, e);
            return null;
        }

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            this.logger.logUserWarnEvent(LOGFROM,
                    "WebHook Failed (%s): intent %s for aiid %s at %s",
                    devIdString,
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

    public String getMessageHash(String devIdString, String secret, byte[] payloadBytes) {
        String calculatedHash;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("UTF-8"), HMAC_ALGORITHM);
            Mac macAlgorithm = Mac.getInstance(HMAC_ALGORITHM);
            macAlgorithm.init(signingKey);
            byte[] calculatedHashBytes = macAlgorithm.doFinal(payloadBytes);
            calculatedHash = Hex.encodeHexString(calculatedHashBytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException | InvalidKeyException e) {
            this.logger.logUserExceptionEvent(LOGFROM,
                    "Webhook Payload MAC calculation failed", devIdString, e);
            return null;
        }
        return calculatedHash;
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
     * @return the active WebHook if it exists, null otherwise.
     * @throws Database.DatabaseException if the WebHook cannot be retrieved.
     */
    public WebHook getWebHookForIntent(final MemoryIntent intent, final UUID devId) {
        WebHook webHook = null;
        try {
            webHook = this.database.getWebHook(intent.getAiid(), intent.getName());
        } catch (Database.DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devId.toString(), e);
        }

        return webHook;
    }
}
