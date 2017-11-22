package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookConnect {

    @SerializedName("connect_token")
    private String connectToken;

    @SerializedName("redirect_uri")
    private String redirectUri;

    public String getConnectToken() {
        return this.connectToken;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }
}
