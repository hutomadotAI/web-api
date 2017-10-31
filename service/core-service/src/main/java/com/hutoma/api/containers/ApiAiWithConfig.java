package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * ApiAi class with additional bot configuration.
 * E.g.: API key/value pairs
 */
public class ApiAiWithConfig extends ApiAi {
    private AiBotConfig config;

    public ApiAiWithConfig(ApiAi apiAi, AiBotConfig config) {
        super(apiAi);
        this.config = config;
    }
}
