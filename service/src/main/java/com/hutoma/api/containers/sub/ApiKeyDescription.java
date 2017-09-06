package com.hutoma.api.containers.sub;

/**
 Describes a single API key used in a bot/skill
 */
public class ApiKeyDescription {
    private String name;
    private String description;
    private String link;

    public ApiKeyDescription(final String name, final String description, final String link) {
        this.name = name;
        this.description = description;
        this.link = link;
    }
}
