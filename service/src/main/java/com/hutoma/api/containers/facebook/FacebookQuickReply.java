package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookQuickReply {

    @SerializedName("content_type")
    private QuickReplyContentType contentType;

    @SerializedName("title")
    private String title;

    @SerializedName("payload")
    private String payload;

    @SerializedName("image_url")
    private String imageUrl;

    public FacebookQuickReply(final String title, final String payload) {
        this.contentType = QuickReplyContentType.text;
        this.title = title;
        this.payload = payload;
    }

    private enum QuickReplyContentType {
        text,
        location
    }
}
