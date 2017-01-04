package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiBot;

/**
 * API response for AI Bot.
 */
public class ApiAiBot extends ApiResult {

    private final AiBot bot;

    public ApiAiBot(final AiBot bot) {
        this.bot = bot;
    }

    public AiBot getBot() {
        return this.bot;
    }
}
