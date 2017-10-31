package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class FacebookToken {

    // "access_token":"EAAUmrAUVXxMBAB0...CYq8ZD",
    // "token_type":"bearer",
    // "expires_in":5109097

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresInSeconds;

    @SerializedName("machine_id")
    private String machineId;

    private DateTime expires;

    public String getAccessToken() {
        return this.accessToken;
    }

    public DateTime getExpires() {
        return this.expires;
    }

    public void calculateExpiry() {
        if (this.expiresInSeconds > 0) {
            // typically either a few hours or two months
            this.expires = new DateTime().plusSeconds(this.expiresInSeconds);
        } else {
            // effectively forever
            this.expires = new DateTime().plusYears(100);
        }
    }

    public String getMachineId() {
        return this.machineId;
    }

    public int getExpiresInSeconds() {
        return this.expiresInSeconds;
    }
}