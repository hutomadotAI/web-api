package com.hutoma.api.connectors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.AiBotConfig;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookPayload;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.logic.ChatRequestInfo;

import org.apache.commons.codec.binary.Hex;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;

import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    public WebHookResponse executeIntentWebHook(final WebHook webHook, final MemoryIntent intent,
                                                final ChatResult chatResult, final ChatRequestInfo chatInfo)
            throws WebHookException {
        final String devIdString = chatInfo.devId.toString();
        if (webHook == null) {
            throw new WebHookException("Webhook cannot be null");
        }
        if (intent == null) {
            throw new WebHookException("Intent cannot be null");
        }
        String webHookEndpoint = webHook.getEndpoint();

        AiBotConfig config;
        try {
            config = this.database.getBotConfigForWebhookCall(chatInfo.devId, chatInfo.aiid, intent.getAiid(),
                    this.serializer);
        } catch (Database.DatabaseException e) {
            throw new WebHookException("Webhook aborted due to failure to load config", e);
        }

        WebHookPayload payload = new WebHookPayload(intent, chatResult, chatInfo, config);

        WebHookResponse webHookResponse = this.executeWebhook(webHookEndpoint, payload, devIdString, chatInfo.aiid);

        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed webhook for aiid %s and intent %s",
                        intent.getAiid(), intent.getName()),
                LogMap.map("Intent", intent.getName())
                        .put("AIID", intent.getAiid())
                        .put("Endpoint", webHook.getEndpoint()));
        return webHookResponse;
    }

    private WebHookResponse executeWebhook(final String webHookEndpoint, final WebHookPayload payload, final String devIdString,
                                    final UUID aiid) throws WebHookException {
        String[] webHookSplit = webHookEndpoint.split(":", 2);
        if (webHookSplit.length < 2) {
            throw new WebHookCallException("Webhook endpoint invalid");
        }
        boolean isHttps = webHookSplit[0].equalsIgnoreCase("https");

        String jsonPayload;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            throw new WebHookException("Webhook Payload Serialisation Failed", e);
        }


        byte[] payloadBytes;
        try {
            payloadBytes = jsonPayload.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // should never hit this for UTF-8, not user's fault
            throw new WebHookException("Webhook payload encoding failed", e);
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
                throw new WebHookException("WebHook Database Error", e);
            }

            calculatedHash = getMessageHash(secret, payloadBytes);
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
            throw WebHookCallException.createWithTypeMessage("WebHook Execution Failed", e);
        }

        try {
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                throw new WebHookCallException(String.format("Webhook call failed (HTTP code %s)", response.getStatus()));
            }

            response.bufferEntity();
            WebHookResponse webHookResponse = this.deserializeResponse(response);
            return webHookResponse;
        }
        finally {
            response.close();
        }
    }

    public WebHookResponse executePassthroughWebhook(final String passthroughUrl, final ChatResult chatResult,
                                                     final ChatRequestInfo chatInfo)
            throws WebHookException {
        final String devIdString = chatInfo.devId.toString();

        if (passthroughUrl == null || passthroughUrl.isEmpty()) {
            throw new WebHookCallException("Invalid URL for passthrough webhook");
        }


        String webHookEndpoint = passthroughUrl;
        WebHookPayload payload = new WebHookPayload(chatResult, chatInfo, null);

        WebHookResponse webHookResponse = this.executeWebhook(webHookEndpoint, payload, devIdString, chatInfo.aiid);
        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed chat webhook for aiid %s",
                        chatInfo.aiid),
                LogMap.map("AIID", chatInfo.aiid)
                        .put("Endpoint", webHookEndpoint));
        return webHookResponse;
    }

    public String getMessageHash(String secret, byte[] payloadBytes) throws WebHookException {
        String calculatedHash;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("UTF-8"), HMAC_ALGORITHM);
            Mac macAlgorithm = Mac.getInstance(HMAC_ALGORITHM);
            macAlgorithm.init(signingKey);
            byte[] calculatedHashBytes = macAlgorithm.doFinal(payloadBytes);
            calculatedHash = Hex.encodeHexString(calculatedHashBytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException | InvalidKeyException e) {
            // should never happen
            throw new WebHookException("Webhook Payload MAC calculation failed", e);
        }
        return calculatedHash;
    }

    /***
     * Deserializes the json response to a WebHookResponse.
     * @param response the Response to deserialize.
     * @return The deserialized WebHookResponse or null.
     */
    public WebHookResponse deserializeResponse(final Response response) throws WebHookCallException {
        try {
            return (WebHookResponse) this.serializer.deserialize(response.readEntity(String.class),
                    WebHookResponse.class);
        } catch (JsonParseException e) {
            throw WebHookCallException.createWithTypeMessage("Failed to deserialize webhook response JSON", e);
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

    /**
     * Base class for exceptions due to web hooks.
     * A direct exception of this type would lead to an Internal Server Error being logged
     */
    public static class WebHookException extends Exception {

        public WebHookException(String message) {
            super(message);
        }

        public WebHookException(String message, Throwable e) {
            super(message, e);
        }
    }

    /**
     * Class for exceptions due to web hooks, where the web hook call failed.
     * Couldn't connect to the server, invalid URL specified, invalid response from webhook, non 200 return from webhook
     * etc.
     */
    public static class WebHookCallException extends WebHookException {
        public WebHookCallException(String message) {
            super(message);
        }

        public WebHookCallException(String message, Throwable e) {
            super(message, e);
        }

        public static WebHookCallException createWithTypeMessage(String message, Throwable e) {
            String messageWithType = String.format("%s - %s", message, e.getMessage());
            return new WebHookCallException(messageWithType, e);
        }
    }
}
