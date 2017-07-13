package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

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

    public WebHookPayload(MemoryIntent intent, ChatResult chatResult) {
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.variablesMap = intent.getVariablesMap();
        this.chatResult = chatResult;
    }
}
