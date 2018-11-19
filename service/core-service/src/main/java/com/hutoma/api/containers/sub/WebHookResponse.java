package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.facebook.FacebookMessageNode;

/**
 * The structure for a response received from a WebHook.
 */
public class WebHookResponse {

    @SerializedName("text")
    private String text;

    @SerializedName("facebook")
    private FacebookMessageNode facebookNode;

    @SerializedName("context")
    private ChatContext chatContext;

    @SerializedName("webhook_token")
    private String token;

    public WebHookResponse(final String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public FacebookMessageNode getFacebookNode() {
        return this.facebookNode;
    }

    public void setFacebookNode(final FacebookMessageNode facebookNode) {
        this.facebookNode = facebookNode;
    }

    public void setChatContext(final ChatContext chatContext) {
        this.chatContext = chatContext;
    }

    public ChatContext getChatContext() {
        return this.chatContext;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
