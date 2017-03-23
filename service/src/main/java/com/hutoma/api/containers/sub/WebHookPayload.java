package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    public WebHookPayload(MemoryIntent intent, ChatResult chatResult) {
        this.intentName = intent.getName();
        this.variables = intent.getVariables();
        this.chatResult = chatResult;
    }
}
