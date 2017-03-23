package com.hutoma.api.containers.sub;

import java.net.URL;
import java.util.UUID;

/**
 * WebHook structure for running external code.
 */
public class WebHook {
    private UUID aiid;
    private String intentName;
    private boolean enabled;
    private String endpoint;

    public WebHook(UUID aiid, String intentName, String endpoint, boolean enabled) {
        this.aiid = aiid;
        this.intentName = intentName;
        this.endpoint = endpoint;
        this.enabled = enabled;
    }

    public UUID getAiid() { return this.aiid; }
    public String getIntentName() { return this.intentName; }
    public boolean getEnabled() { return this.enabled; }
    public String getEndpoint() { return this.endpoint; }
}
