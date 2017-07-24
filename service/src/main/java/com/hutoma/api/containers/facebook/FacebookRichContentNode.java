package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookRichContentNode {

    @SerializedName("type")
    private RichContentType contentType;

    @SerializedName("payload")
    private FacebookRichContentPayload payload;

    public RichContentType getContentType() {
        return this.contentType;
    }

    public enum RichContentType {
        audio,
        video,
        file,
        image,
        template
    }

}
