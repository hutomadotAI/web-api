package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacebookRichContentPayload {

    @SerializedName("url")
    private String payloadUrl;

    @SerializedName("template_type")
    private TemplateType templateType;

    @SerializedName("text")
    private String text;

    @SerializedName("buttons")
    private List<FacebookRichContentButtons> buttons;

    @SerializedName("elements")
    private List<FacebookRichContentElement> elements;

    @SerializedName("sharable")
    private Boolean sharable = null;

    @SerializedName("image_aspect_ratio")
    private String imageAspectRatio;

    /***
     * ref: https://developers.facebook.com/docs/messenger-platform/send-api-reference/generic-template
     */
    public enum AspectRatio {
        horizontal,
        square
    }

    public enum TemplateType {
        button,
        generic,
        list
    }
}
