package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.stream.Collectors;

public class FacebookRichContentNode {

    @SerializedName("type")
    private RichContentType contentType;

    @SerializedName("payload")
    private FacebookRichContentPayload payload;

    FacebookRichContentNode(final RichContentType contentType, final FacebookRichContentPayload payload) {
        this.contentType = contentType;
        this.payload = payload;
    }

    /***
     * Generator function to create a button template with only a title (required by Facebook)
     * and some buttons
     * @param title
     * @param buttons
     * @return
     */
    public static final FacebookRichContentNode createButtonTemplate(final String title, final List<String> buttons) {
        List<FacebookRichContentButton> buttonNodes = buttons.stream().map(key -> new FacebookRichContentButton(
                FacebookRichContentButton.ButtonType.postback, key, null, key))
                .collect(Collectors.toList());
        FacebookRichContentNode node = new FacebookRichContentNode(RichContentType.template,
                new FacebookRichContentPayload(null, FacebookRichContentPayload.TemplateType.button,
                        title, buttonNodes, null, null, null));
        return node;
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
