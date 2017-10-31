package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookRichContentButton {

    @SerializedName("type")
    private ButtonType buttonType;

    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String buttonUrl;

    @SerializedName("payload")
    private String payload;

    FacebookRichContentButton(final ButtonType buttonType, final String title,
                              final String buttonUrl, final String payload) {
        this.buttonType = buttonType;
        this.title = title;
        this.buttonUrl = buttonUrl;
        this.payload = payload;
    }

    public enum ButtonType {
        web_url,
        phone_number,
        element_share,
        postback
    }
}
