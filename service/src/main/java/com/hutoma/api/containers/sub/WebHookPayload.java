package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.Tools;

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

    public WebHookPayload(final MemoryIntent intent, final ChatResult chatResult, final UUID originatingAiid,
                          final Map<String, String> clientVariables) {
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.variablesMap = intent.getVariablesMap();
        this.chatResult = chatResult;
        this.clientVariables = clientVariables;
        this.originatingAiid = originatingAiid.toString();
        this.chatSession = Tools.getHashedDigestFromUuid(chatResult.getChatId());
    }

    public WebHookPayload(final ChatResult chatResult, final UUID originatingAiid, final Map<String, String> clientVariables) {
        this.chatResult = chatResult;
        this.originatingAiid = originatingAiid.toString();
        this.clientVariables = clientVariables;
    }
}
