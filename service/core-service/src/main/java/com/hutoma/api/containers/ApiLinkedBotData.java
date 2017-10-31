package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.AiBot;

import java.util.Map;

/**
 * Container for a linked bot - contains the bot data and configuration data such as API keys.
 */
public class ApiLinkedBotData extends ApiResult {
    private AiBot bot;
    private AiBotConfig config;

    public ApiLinkedBotData(AiBot bot, AiBotConfig config) {
        this.bot = bot;
        this.config = config;
    }

    public AiBot getBot() {
        return bot;
    }

    public AiBotConfig getConfig() {
        return config;
    }
}
