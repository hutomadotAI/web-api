package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * WebHook structure for running external code.
 */
public class WebHook {
    @SerializedName("aiid")
    private UUID aiid;
    @SerializedName("intent_name")
    private String intentName;
    @SerializedName("enabled")
    private boolean enabled;
    @SerializedName("endpoint")
    private String endpoint;

    public WebHook(UUID aiid, String intentName, String endpoint, boolean enabled) {
        this.aiid = aiid;
        this.intentName = intentName;
        this.endpoint = endpoint;
        this.enabled = enabled;
    }

    public UUID getAiid() { return this.aiid; }
    public String getIntentName() { return this.intentName; }
    public boolean isEnabled() { return this.enabled; }
    public String getEndpoint() { return this.endpoint; }
}
