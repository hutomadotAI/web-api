package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiBot;

import java.util.List;

/**
 * API response for AI Bot list.
 */
public class ApiAiBotList extends ApiResult {

    private final List<AiBot> bots;

    public ApiAiBotList(final List<AiBot> bots) {
        this.bots = bots;
    }

    public List<AiBot> getBotList() {
        return this.bots;
    }
}
