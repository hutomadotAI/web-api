package com.hutoma.api.containers;

import java.util.List;

/**
 * Class describing the Bot configuration
 */
public class AiBotConfigDefinition {
    private AiBotConfig config;
    private List<ApiKeyDescription> descriptions;

    public AiBotConfigDefinition(AiBotConfig config, List<ApiKeyDescription> descriptions) {
        this.config = config;
        this.descriptions = descriptions;
    }

    public AiBotConfig getConfig() {
        return config;
    }

    public List<ApiKeyDescription> getDescriptions() {
        return descriptions;
    }

    public static class ApiKeyDescription {
        private String name;
        private String description;
        private String link;

        public ApiKeyDescription(String name, String description, String link) {
            this.name = name;
            this.description = description;
            this.link = link;
        }
    }
}
