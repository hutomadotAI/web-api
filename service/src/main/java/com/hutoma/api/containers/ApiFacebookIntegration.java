package com.hutoma.api.containers;


import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.Map;

public class ApiFacebookIntegration extends ApiResult {

    @SerializedName("has_access_token")
    boolean hasAccessToken;

    @SerializedName("access_token_expiry")
    DateTime tokenExpiry;

    @SerializedName("facebook_username")
    String username;

    @SerializedName("page_list")
    Map<String, String> pageList;

    @SerializedName("page_integrated_name")
    String pageIntegratedName;

    @SerializedName("page_integrated_id")
    String pageIntegratedId;

    @SerializedName("integration_active")
    boolean integrationActive;

    @SerializedName("integration_status")
    String integrationStatus;

    @SerializedName("facebook_app_id")
    String facebookAppId;

    @SerializedName("success")
    boolean success;

    public ApiFacebookIntegration(final String facebookAppId, final boolean hasAccessToken, final DateTime tokenExpiry,
                                  final String username, final boolean integrationActive, final String integrationStatus) {
        this.facebookAppId = facebookAppId;
        this.hasAccessToken = hasAccessToken;
        this.tokenExpiry = tokenExpiry;
        this.username = username;
        this.integrationActive = integrationActive;
        this.integrationStatus = integrationStatus;
        this.success = true;
    }

    public boolean hasAccessToken() {
        return this.hasAccessToken;
    }

    public Map<String, String> getPageList() {
        return this.pageList;
    }

    public void setPageList(final Map<String, String> pageList) {
        this.pageList = pageList;
    }

    public String getPageIntegratedId() {
        return this.pageIntegratedId;
    }

    public void setPageIntegrated(final String pageId, final String pageName) {
        this.pageIntegratedId = pageId;
        this.pageIntegratedName = pageName;
    }

    public void setIntegrationStatus(final String integrationStatus) {
        this.integrationStatus = integrationStatus;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }
}
