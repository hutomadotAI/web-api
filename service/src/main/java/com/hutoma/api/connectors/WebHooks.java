package com.hutoma.api.connectors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookPayload;
import com.hutoma.api.containers.sub.WebHookResponse;

import org.apache.commons.codec.binary.Hex;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Management and execution of WebHooks.
 */
public class WebHooks {
    public static final int HMAC_SECRET_LENGTH = 40;
    private static final String LOGFROM = "webhooks";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final Database database;
    private final ILogger logger;
    private final JsonSerializer serializer;
    private final JerseyClient jerseyClient;
    private final Tools tools;

    @Inject
    public WebHooks(final Database database, final ILogger logger, final JsonSerializer serializer,
                    final JerseyClient jerseyClient, final Tools tools) {
        this.database = database;
        this.logger = logger;
        this.serializer = serializer;
        this.jerseyClient = jerseyClient;
        this.tools = tools;
    }

    /***
     * Executes the WebHook for an intent.
     * @param webHook The web hook.
     * @param intent The intent.
     * @param chatResult The chat result for the request.
     * @return a WebHookResponse containing the returned data.
     */
    public WebHookResponse executeIntentWebHook(final WebHook webHook, final MemoryIntent intent, final ChatResult chatResult,
                                                final Map<String, String> clientVariables, final UUID devId,
                                                final UUID originatingAiid) {
        final String devIdString = devId.toString();
        if (webHook == null || intent == null) {
            this.logger.logError(LOGFROM, "Invalid parameters passed.");
            return null;
        }

        String webHookEndpoint = webHook.getEndpoint();
        String[] webHookSplit = webHookEndpoint.split(":", 2);
        if (webHookSplit.length < 2) {
            this.logger.logUserWarnEvent(LOGFROM, "Webhook endpoint invalid",
                    devIdString,
                    LogMap.map("Endpoint", webHookEndpoint)
                            .put("Intent", intent.getName())
                            .put("AIID", intent.getAiid())
                            .put("Endpoint", webHook.getEndpoint()));
            return null;
        }
        boolean isHttps = webHookSplit[0].equalsIgnoreCase("https");

        WebHookPayload payload = new WebHookPayload(intent, chatResult, originatingAiid, clientVariables);

        String jsonPayload;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook Payload Serialisation Failed", devIdString, e);
            return null;
        }

        Response response = this.executeWebhook(webHookEndpoint, jsonPayload, devIdString, isHttps, originatingAiid);

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            this.logger.logUserWarnEvent(LOGFROM,
                    String.format("WebHook Failed (%s): intent %s for aiid %s at %s",
                            response.getStatus(),
                            intent.getName(),
                            intent.getAiid(),
                            webHook.getEndpoint()),
                    devIdString,
                    LogMap.map("ResponseStatus", response.getStatus())
                            .put("Intent", intent.getName())
                            .put("AIID", intent.getAiid())
                            .put("Endpoint", webHook.getEndpoint()));
            return null;
        }

        response.bufferEntity();
        WebHookResponse webHookResponse = this.deserializeResponse(response);
        response.close();

        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed webhook for aiid %s and intent %s",
                        intent.getAiid(), intent.getName()),
                LogMap.map("Intent", intent.getName())
                        .put("AIID", intent.getAiid())
                        .put("Endpoint", webHook.getEndpoint()));
        return webHookResponse;
    }

    private Response executeWebhook(String webHookEndpoint, String jsonPayload, String devIdString, boolean isHttps, UUID aiid) {
        byte[] payloadBytes;
        try {
            payloadBytes = jsonPayload.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // should never hit this for UTF-8
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook payload encoding failed", devIdString, e);
            return null;
        }

        String calculatedHash = null;
        if (isHttps) {
            String secret;
            try {
                secret = this.database.getWebhookSecretForBot(aiid);
                if (secret == null) {
                    this.logger.logUserWarnEvent(LOGFROM, "Webhook secret null, regenerating", devIdString,
                            LogMap.map("AIID", aiid));
                    secret = this.tools.generateRandomHexString(HMAC_SECRET_LENGTH);
                    this.database.setWebhookSecretForBot(aiid, secret);
                }
            } catch (Database.DatabaseException e) {
                this.logger.logUserExceptionEvent(LOGFROM, "WebHook Database Error", devIdString, e);
                return null;
            }

            // getMessageHash logs internally, no need to log again
            calculatedHash = getMessageHash(devIdString, secret, payloadBytes);
            if (calculatedHash == null) return null;
        }

        Response response;
        try {
            JerseyInvocation.Builder builder = this.jerseyClient.target(webHookEndpoint)
                    .property("Content-Type", "application/json")
                    .property("Content-Length", String.valueOf(payloadBytes.length))
                    .property(ClientProperties.CONNECT_TIMEOUT, 10000)
                    .property(ClientProperties.READ_TIMEOUT, 10000)
                    .request();
            if (isHttps) {
                builder = builder.header("X-Hub-Signature", "sha256=" + calculatedHash);
            }
            response = builder.post(Entity.json(payloadBytes));
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WebHook Execution Failed", devIdString, e);
            return null;
        }

        return response;
    }

    public WebHookResponse executePassthroughWebhook(final String passthroughUrl, final ChatResult chatResult,
                                                     final Map<String, String> clientVariables, final UUID devId,
                                                     final UUID originatingAiid) {
        final String devIdString = devId.toString();

        if (passthroughUrl == null || passthroughUrl == "") {
            this.logger.logError(LOGFROM, "Invalid url passed.");
            return null;
        }

        String webHookEndpoint = passthroughUrl;
        String[] webHookSplit = webHookEndpoint.split(":", 2);
        if (webHookSplit.length < 2) {
            this.logger.logUserWarnEvent(LOGFROM, "Webhook endpoint invalid",
                    devIdString,
                    LogMap.map("Endpoint", webHookEndpoint)
                            .put("AIID", originatingAiid)
                            .put("Endpoint", webHookEndpoint));
            return null;
        }
        boolean isHttps = webHookSplit[0].equalsIgnoreCase("https");

        WebHookPayload payload = new WebHookPayload(chatResult, originatingAiid, clientVariables);

        String jsonPayload;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Webhook Payload Serialisation Failed", devIdString, e);
            return null;
        }

        Response response = this.executeWebhook(webHookEndpoint, jsonPayload, devIdString, isHttps, originatingAiid);

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            this.logger.logUserWarnEvent(LOGFROM,
                    String.format("Chat WebHook Failed (%s): aiid %s at %s",
                            response.getStatus(),
                            originatingAiid,
                            webHookEndpoint),
                    devIdString,
                    LogMap.map("ResponseStatus", response.getStatus())
                            .put("AIID", originatingAiid)
                            .put("Endpoint", webHookEndpoint));
            return null;
        }

        response.bufferEntity();
        WebHookResponse webHookResponse = this.deserializeResponse(response);
        response.close();

        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed chat webhook for aiid %s",
                        originatingAiid),
                LogMap.map("AIID", originatingAiid)
                        .put("Endpoint", webHookEndpoint));
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
