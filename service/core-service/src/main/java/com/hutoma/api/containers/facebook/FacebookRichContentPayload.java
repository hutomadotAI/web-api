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
    private List<FacebookRichContentButton> buttons;

    @SerializedName("elements")
    private List<FacebookRichContentElement> elements;

    @SerializedName("sharable")
    private Boolean sharable = null;

    @SerializedName("image_aspect_ratio")
    private String imageAspectRatio;

    @SerializedName("top_element_style")
    private ElementStyle topElementStyle;

    FacebookRichContentPayload(final String payloadUrl, final TemplateType templateType, final String text,
                               final List<FacebookRichContentButton> buttons,
                               final List<FacebookRichContentElement> elements,
                               final Boolean sharable, final String imageAspectRatio) {
        this.payloadUrl = payloadUrl;
        this.templateType = templateType;
        this.text = text;
        this.buttons = buttons;
        this.elements = elements;
        this.sharable = sharable;
        this.imageAspectRatio = imageAspectRatio;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public List<FacebookRichContentButton> getButtons() {
        return buttons;
    }

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

    public enum ElementStyle {
        large,
        compact
    }

}
