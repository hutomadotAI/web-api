package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.facebook.FacebookRichContentNode;

/**
 * The structure for a response received from a WebHook.
 */
public class WebHookResponse {

    @SerializedName("text")
    private String text;

    @SerializedName("facebook")
    private FacebookRichContentNode facebookNode;

    public WebHookResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public FacebookRichContentNode getFacebookNode() {
        return this.facebookNode;
    }

    public void setFacebookNode(final FacebookRichContentNode facebookNode) {
        this.facebookNode = facebookNode;
    }
}
