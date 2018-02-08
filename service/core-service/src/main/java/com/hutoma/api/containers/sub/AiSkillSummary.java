package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

/**
 * AI Skill Summary
 */
public class AiSkillSummary {

    @SerializedName("name")
    private String name;
    @SerializedName("bot_id")
    private int botId;
    @SerializedName("category")
    private String category;

    public AiSkillSummary(final String name, final int botId) {
        this.name = name;
        this.botId = botId;
    }

    public AiSkillSummary(final AiBot bot) {
        this.name = bot.getName();
        this.botId = bot.getBotId();
        this.category = bot.getCategory();
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public int getBotId() {
        return this.botId;
    }
}