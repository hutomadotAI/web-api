package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookRichContentAttachment {

    @SerializedName("type")
    private FacebookRichContentAttachment.RichContentType contentType;

    @SerializedName("payload")
    private FacebookRichContentPayload payload;

    protected FacebookRichContentAttachment() {
    }

    public FacebookRichContentAttachment(final FacebookRichContentAttachment.RichContentType contentType,
                                  final FacebookRichContentPayload payload) {
        this.contentType = contentType;
        this.payload = payload;
    }

    public FacebookRichContentPayload getPayload() {
        return this.payload;
    }

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
