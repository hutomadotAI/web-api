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
    protected final JsonSerializer serializer;
    private final Database database;
    private final ILogger logger;
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
            throw new WebHookInternalException("Webhook cannot be null");
        }
        if (intent == null) {
            throw new WebHookInternalException("Intent cannot be null");
        }
        String webHookEndpoint = webHook.getEndpoint();

        AiBotConfig config;
        try {
            config = this.database.getBotConfigForWebhookCall(chatInfo.devId, chatInfo.aiid, intent.getAiid(),
                    this.serializer);
        } catch (Database.DatabaseException e) {
            throw new WebHookInternalException("Webhook aborted due to failure to load config", e);
        }

        WebHookPayload payload = new WebHookPayload(intent, chatResult, chatInfo, config);

        WebHookResponse webHookResponse = this.executeWebhook(webHookEndpoint, payload, devIdString, chatInfo.aiid);

        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed webhook for aiid %s and intent %s",
                        intent.getAiid(), intent.getName()),
                LogMap.map("Intent", intent.getName())
                        .put("AIID", intent.getAiid())
                        .put("Endpoint", webHook.getEndpoint())
                        .put("ChatId", chatInfo.chatId)
                        .put("ObfuscatedChatSession", payload.getObfuscatedChatSession()));
        return webHookResponse;
    }

    public WebHookResponse executePassthroughWebhook(final String passthroughUrl, final ChatResult chatResult,
                                                     final ChatRequestInfo chatInfo)
            throws WebHookException {
        final String devIdString = chatInfo.devId.toString();

        if (passthroughUrl == null || passthroughUrl.isEmpty()) {
            throw new WebHookExternalException("Invalid URL for passthrough webhook");
        }

        WebHookPayload payload = new WebHookPayload(chatResult, chatInfo, null);
        WebHookResponse webHookResponse = this.executeWebhook(passthroughUrl, payload, devIdString, chatInfo.aiid);
        this.logger.logInfo(LOGFROM,
                String.format("Successfully executed chat webhook for aiid %s",
                        chatInfo.aiid),
                LogMap.map("AIID", chatInfo.aiid)
                        .put("Endpoint", passthroughUrl)
                        .put("ChatId", chatInfo.chatId)
                        .put("ObfuscatedChatSession", payload.getObfuscatedChatSession()));
        return webHookResponse;
    }

    public String getMessageHash(String secret, byte[] payloadBytes) throws WebHookInternalException {
        String calculatedHash;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("UTF-8"), HMAC_ALGORITHM);
            Mac macAlgorithm = Mac.getInstance(HMAC_ALGORITHM);
            macAlgorithm.init(signingKey);
            byte[] calculatedHashBytes = macAlgorithm.doFinal(payloadBytes);
            calculatedHash = Hex.encodeHexString(calculatedHashBytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException | InvalidKeyException e) {
            // should never happen
            throw new WebHookInternalException("Webhook Payload MAC calculation failed", e);
        }
        return calculatedHash;
    }

    /***
     * Deserializes the json response to a WebHookResponse.
     * @param response the Response to deserialize.
     * @return The deserialized WebHookResponse or null.
     */
    public WebHookResponse deserializeResponse(final Response response) throws WebHookExternalException {
        WebHookResponse deserializedResponse = null;
        try {
            deserializedResponse = (WebHookResponse) this.serializer.deserialize(getEntity(response),
                    WebHookResponse.class);
        } catch (JsonParseException e) {
            throw WebHookExternalException.createWithTypeMessage("Failed to deserialize webhook response JSON", e);
        }
        if (deserializedResponse == null) {
            throw new WebHookExternalException("Failed to deserialize webhook response JSON");
        }
        return deserializedResponse;
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

    private WebHookResponse executeWebhook(final String webHookEndpoint, final WebHookPayload payload,
                                           final String devIdString, final UUID aiid)
            throws WebHookException {

        String[] webHookSplit = webHookEndpoint.split(":", 2);
        if (webHookSplit.length < 2) {
            throw new WebHookExternalException("Webhook endpoint invalid");
        }
        boolean isHttps = webHookSplit[0].equalsIgnoreCase("https");

        String jsonPayload;
        try {
            jsonPayload = this.serializer.serialize(payload);
        } catch (JsonIOException e) {
            throw new WebHookInternalException("Webhook Payload Serialisation Failed", e);
        }


        byte[] payloadBytes;
        try {
            payloadBytes = jsonPayload.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // should never hit this for UTF-8, not user's fault
            throw new WebHookInternalException("Webhook payload encoding failed", e);
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
                throw new WebHookInternalException("WebHook Database Error", e);
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
            throw WebHookExternalException.createWithTypeMessage("WebHook Execution Failed", e);
        }

        try {
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                throw new WebHookExternalException(String.format("Webhook call failed (HTTP code %s)",
                        response.getStatus()));
            }

            response.bufferEntity();
            return this.deserializeResponse(response);
        } finally {
            response.close();
        }
    }

    /**
     * Base class for exceptions due to web hooks.
     * Shouldn't directly use this class, use one of the derived classes instead.
     */
    public static class WebHookException extends Exception {
        protected WebHookException(String message) {
            super(message);
        }

        protected WebHookException(String message, Throwable e) {
            super(message, e);
        }

    }

    /**
     * Class for exceptions due to web hooks, where the web hook call failed due to an internal platform issue.
     * A direct exception of this type would lead to an Internal Server Error being logged.
     */
    public static class WebHookInternalException extends WebHookException {
        public WebHookInternalException(String message) {
            super(message);
        }

        public WebHookInternalException(String message, Throwable e) {
            super(message, e);
        }
    }

    /**
     * Class for exceptions due to web hooks, where the web hook call failed due to an external factor.
     * Couldn't connect to the server, invalid URL specified, invalid response from webhook, non 200 return from webhook
     * etc.
     */
    public static class WebHookExternalException extends WebHookException {
        public WebHookExternalException(String message) {
            super(message);
        }

        public WebHookExternalException(String message, Throwable e) {
            super(message, e);
        }

        public static WebHookExternalException createWithTypeMessage(String message, Throwable e) {
            String messageWithType = String.format("%s - %s", message, e.getMessage());
            return new WebHookExternalException(messageWithType, e);
        }
    }

    protected String getEntity(Response response) {
        return response.readEntity(String.class);
    }
}
