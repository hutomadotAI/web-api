package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public class WebHookSession {

    public static final int DEFAULT_MAX_USES = 1;
    public static final long DEFAULT_EXPIRY_MILLIS = 1000 * 60 * 60; // 1 hour

    @SerializedName("token")
    private final String token;
    @SerializedName("max_uses")
    private int maxUses;
    @SerializedName("expiry_timestamp")
    private final long expiryTimestamp;

    public WebHookSession(final String token) {
        this(token, DEFAULT_MAX_USES, System.currentTimeMillis() + DEFAULT_EXPIRY_MILLIS);
    }

    public WebHookSession(final String token, final int maxUses, final long expiryTimestamp) {
        this.token = token;
        this.maxUses = maxUses;
        this.expiryTimestamp = expiryTimestamp;
    }

    public long getExpiryTimestamp() {
        return this.expiryTimestamp;
    }

    public String getToken() {
        return this.token;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void decrementUses() {
        this.maxUses--;
    }
}
