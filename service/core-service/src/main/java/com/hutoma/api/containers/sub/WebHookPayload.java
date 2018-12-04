package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.AiBotConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The structure for serialising a WebHook payload.
 */
public class WebHookPayload {

    @SerializedName("intentName")
    private String intentName;

    @SerializedName("memoryVariables")
    private List<MemoryVariable> variables;

    @SerializedName("chatResult")
    private ChatResult chatResult;

    @SerializedName("variablesMap")
    private Map<String, MemoryVariable> variablesMap;

    @SerializedName("clientVariables")
    private Map<String, String> clientVariables;

    @SerializedName("originatingAiid")
    private String originatingAiid;

    @SerializedName("chatSession")
    private String chatSession;

    @SerializedName("config")
    private AiBotConfig config;

    @SerializedName("webhook_token")
    private String webhookToken;

    public WebHookPayload(final MemoryIntent intent,
                          final ChatResult chatResult,
                          final ChatRequestInfo chatInfo,
                          final AiBotConfig config) {
        this(chatResult, chatInfo, config);
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.variablesMap = intent.getVariablesMap();
    }

    public WebHookPayload(final ChatResult chatResult,
                          final ChatRequestInfo chatInfo,
                          final AiBotConfig config) {
        this.chatResult = chatResult;
        this.originatingAiid = chatInfo.getAiid().toString();
        this.clientVariables = chatInfo.getClientVariables();
        this.chatSession = chatResult.getChatState().getHashedChatId();
        this.webhookToken = generateWebhookToken(chatInfo.getDevId(), chatInfo.getAiid(), chatInfo.getChatId());
        this.config = config;
    }

    public String getObfuscatedChatSession() {
        return this.chatSession;
    }

    public String getWebhookToken() {
        return this.webhookToken;
    }

    public static String generateWebhookToken(final UUID devId, final UUID aiid, final UUID chatId) {
        return Tools.getHashedDigest(getStringFromClaims(devId, aiid, chatId));
    }

    private static String getStringFromClaims(final UUID devId, final UUID aiid, final UUID chatId) {
        // Build a string that contains the data for the token
        StringBuilder sb = new StringBuilder();
        sb.append(devId.toString()).append(aiid.toString()).append(chatId.toString());
        return sb.toString();
    }
}
