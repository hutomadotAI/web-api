package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.AiBotConfig;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

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
                          final AiBotConfig config,
                          final String tokenEndcodingSecret) {
        this(chatResult, chatInfo, config, tokenEndcodingSecret);
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.variablesMap = intent.getVariablesMap();
    }

    public WebHookPayload(final ChatResult chatResult,
                          final ChatRequestInfo chatInfo,
                          final AiBotConfig config,
                          final String tokenEndcodingSecret) {
        this.chatResult = chatResult;
        this.originatingAiid = chatInfo.getAiid().toString();
        this.clientVariables = chatInfo.getClientVariables();
        this.chatSession = chatResult.getChatState().getHashedChatId();
        this.webhookToken = tokenEndcodingSecret == null
                ? null
                : generateWebhookToken(chatInfo.getDevId(), chatInfo.getAiid(),
                chatInfo.getChatId(), tokenEndcodingSecret);
        this.config = config;
    }

    public String getObfuscatedChatSession() {
        return this.chatSession;
    }

    public String getWebhookToken() {
        return this.webhookToken;
    }

    public static String generateWebhookToken(final UUID devId, final UUID aiid, final UUID chatId,
                                              final String tokenEndcodingSecret) {
        return Jwts.builder()
                .claim("AIID", aiid.toString())
                .claim("TokenId", UUID.randomUUID())
                .claim("ChatId", chatId)
                .setSubject(devId.toString())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, tokenEndcodingSecret)
                .compact();
    }
}
