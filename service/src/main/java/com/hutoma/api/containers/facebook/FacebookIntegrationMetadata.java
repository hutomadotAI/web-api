package com.hutoma.api.containers.facebook;

import org.joda.time.DateTime;

public class FacebookIntegrationMetadata {

    private String accessToken;
    private String userName;
    private DateTime accessTokenExpiry;
    private String pageToken;
    private String pageName;

    public FacebookIntegrationMetadata() {
        this.accessToken = "";
        this.userName = "";
        this.accessTokenExpiry = DateTime.now();
    }

    public FacebookIntegrationMetadata(final String accessToken, final String userName,
                                       final DateTime accessTokenExpiry) {
        this.accessToken = accessToken;
        this.userName = userName;
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getUserName() {
        return this.userName;
    }

    public DateTime getAccessTokenExpiry() {
        return this.accessTokenExpiry;
    }

    public String getPageToken() {
        return this.pageToken;
    }

    public void setPageToken(final String pageToken) {
        this.pageToken = pageToken;
    }

    public String getPageName() {
        return this.pageName;
    }

    public void setPageName(final String pageName) {
        this.pageName = pageName;
    }

}
