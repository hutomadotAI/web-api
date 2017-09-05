package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.Tools;
import com.hutoma.api.logic.ChatRequestInfo;

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


    public WebHookPayload(final MemoryIntent intent, final ChatResult chatResult, final ChatRequestInfo chatInfo) {
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.variablesMap = intent.getVariablesMap();
        this.chatResult = chatResult;
        this.clientVariables = chatInfo.clientVariables;
        this.originatingAiid = chatInfo.aiid.toString();
        this.chatSession = Tools.getHashedDigestFromUuid(chatInfo.chatId);
    }

    public WebHookPayload(final ChatResult chatResult, final ChatRequestInfo chatInfo) {
        this.chatResult = chatResult;
        this.originatingAiid = chatInfo.aiid.toString();
        this.clientVariables = chatInfo.clientVariables;
        this.chatSession = Tools.getHashedDigestFromUuid(chatInfo.chatId);
    }
}
