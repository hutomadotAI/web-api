package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

/**
 * API Administration result message.
 */
public class ApiAdmin extends ApiResult {

    @SerializedName("dev_token")
    private final String devToken;

    @SerializedName("devid")
    private final UUID devId;

    public ApiAdmin(String devToken, UUID devId) {
        this.devToken = devToken;
        this.devId = devId;
    }

    public String getDev_token() {
        return this.devToken;
    }

    public UUID getDevid() {
        return this.devId;
    }
}
