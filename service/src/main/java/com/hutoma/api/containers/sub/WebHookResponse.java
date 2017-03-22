package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

/**
 * The structure for a response received from a WebHook.
 */
public class WebHookResponse {

    @SerializedName("text")
    private String text;

    public WebHookResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
