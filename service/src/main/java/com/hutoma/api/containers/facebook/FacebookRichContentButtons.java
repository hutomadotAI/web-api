package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookRichContentButtons {

    @SerializedName("type")
    public ButtonType buttonType;

    @SerializedName("title")
    public String title;

    @SerializedName("url")
    public String buttonUrl;

    @SerializedName("payload")
    public String payload;

    public enum ButtonType {
        web_url,
        phone_number,
        element_share,
        postback
    }

}
