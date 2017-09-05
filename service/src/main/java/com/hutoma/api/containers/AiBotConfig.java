package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Configuration for a bot.
 * E.g. API keys
 */
public class AiBotConfig {
    /**
     * The current version
     */
    public final static int CURRENT_VERSION = 1;

    private int version;

    @SerializedName("api_keys")
    private Map<String,String> apiKeys;

    public AiBotConfig(Map<String, String> apiKeys) {
        this.version = CURRENT_VERSION;
        this.apiKeys = apiKeys;
    }

    /**
     * Version number of config JSON - if unspecified will default to CURRENT_VERSION
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, String> getApiKeys() {
        return apiKeys;
    }

    public boolean isEmpty() {
        if (apiKeys == null || apiKeys.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isValid() {
        // just check that the version isn't from the future
        if (this.version > CURRENT_VERSION) {
            return false;
        }
        return true;
    }
}
