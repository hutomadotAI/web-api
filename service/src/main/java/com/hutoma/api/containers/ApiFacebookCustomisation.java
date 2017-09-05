package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

public class ApiFacebookCustomisation extends ApiResult {

    @SerializedName("page_greeting")
    private String pageGreeting;

    @SerializedName("get_started_payload")
    private String getStartedPayload;

    public ApiFacebookCustomisation(final String pageGreeting, final String getStartedPayload) {
        this.pageGreeting = pageGreeting;
        this.getStartedPayload = getStartedPayload;
    }

    public String getPageGreeting() {
        return pageGreeting;
    }

    public String getGetStartedPayload() {
        return getStartedPayload;
    }
}
