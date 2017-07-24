package com.hutoma.api.containers.facebook;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacebookRichContentElement {

    @SerializedName("title")
    private String title;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("default_action")
    private TemplateDefaultAction defaultAction;

    @SerializedName("buttons")
    private List<FacebookRichContentButtons> buttons;

    public enum ActionType {
        web_url
    }

    private class TemplateDefaultAction {

        @SerializedName("type")
        private ActionType actionType;

        @SerializedName("url")
        private String actionUrl;

        @SerializedName("webview_height_ratio")
        private String webviewHeightRatio;

    }
}
