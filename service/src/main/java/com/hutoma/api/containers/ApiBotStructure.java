package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;

/**
 * Created by bretc on 16/08/2017.
 */
public class ApiBotStructure extends ApiResult {

    private final BotStructure bot;

    public ApiBotStructure(final BotStructure bot) {
        this.bot = bot;
    }

    public BotStructure getBotStructure() {
        return this.bot;
    }
}
