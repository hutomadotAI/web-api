package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * Class describing the Bot configuration's definition
 */
public class AiBotConfigDefinition {
    @SerializedName("api_keys")
    private List<ApiKeyDescription> apiKeys;

    public AiBotConfigDefinition(List<ApiKeyDescription> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public List<ApiKeyDescription> getApiKeys() {
        return apiKeys;
    }

    public static class ApiKeyDescription {
        public String name;
        public String description;
        public String link;

        public ApiKeyDescription(String name, String description, String link) {
            this.name = name;
            this.description = description;
            this.link = link;
        }
    }

    public boolean isEmpty() {
        if (apiKeys == null || apiKeys.isEmpty()) {
            return true;
        }
        return false;
    }
}

