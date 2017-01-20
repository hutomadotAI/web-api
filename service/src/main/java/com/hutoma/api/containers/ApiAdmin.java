package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

/**
 * API Administration result message.
 */
public class ApiAdmin extends ApiResult {

    @SerializedName("dev_token")
    private final String devToken;

    @SerializedName("devid")
    private final String devId;

    public ApiAdmin(String devToken, String devId) {
        this.devToken = devToken;
        this.devId = devId;
    }

    public String getDev_token() {
        return this.devToken;
    }

    public String getDevid() {
        return this.devId;
    }
}
