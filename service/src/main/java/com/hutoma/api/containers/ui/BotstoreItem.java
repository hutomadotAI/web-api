package com.hutoma.api.containers.ui;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.AiBotConfigDefinition;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.ApiKeyDescription;
import com.hutoma.api.containers.sub.DeveloperInfo;

import java.util.List;

/**
 * Item in the botstore.
 */
public class BotstoreItem {
    @SerializedName("metadata")
    private final AiBot bot;
    @SerializedName("developer")
    private final DeveloperInfo devInfo;
    @SerializedName("order")
    private final int order;
    @SerializedName("owned")
    private boolean owned;

    @SerializedName("bot_config_definition")
    private AiBotConfigDefinition botConfigDefinition;

    public BotstoreItem(final int order, final AiBot bot, final DeveloperInfo devInfo, final boolean owned) {
        this.order = order;
        this.bot = bot;
        this.devInfo = devInfo;
        this.owned = owned;
    }

    public AiBot getMetadata() {
        return this.bot;
    }

    public boolean isOwned() {
        return this.owned;
    }

    public void setOwned(final boolean owned) {
        this.owned = owned;
    }

    public DeveloperInfo getDeveloperInfo() {
        return this.devInfo;
    }

    public AiBotConfigDefinition getBotConfigDefinition() { return this.botConfigDefinition; }

    public void setBotConfigDefinition(AiBotConfigDefinition botConfigDefinition) {
        this.botConfigDefinition = botConfigDefinition;
    }

}
