package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

/**
 * User information.
 */
public class UserInfo {
    @SerializedName("created")
    private DateTime created;
    @SerializedName("valid")
    private boolean valid;
    @SerializedName("internal")
    private boolean internal;
    @SerializedName("dev_id")
    private final String devId;
    @SerializedName("id")
    private final int id;
    @SerializedName("dev_token")
    private String devToken;
    @SerializedName("plan_id")
    private final int planId;

    public UserInfo(final DateTime created, final boolean valid, final boolean internal,
                    final String devId, final int id, final String devToken, final int planId) {
        this.created = created;
        this.valid = valid;
        this.internal = internal;
        this.devId = devId;
        this.id = id;
        this.devToken = devToken;
        this.planId = planId;
    }

    public String getDevId() {
        return this.devId;
    }

    public String getDevToken() {
        return this.devToken;
    }

    public int getPlanId() {
        return this.planId;
    }

    public void setDevToken(final String devToken) {
        this.devToken = devToken;
    }
}
