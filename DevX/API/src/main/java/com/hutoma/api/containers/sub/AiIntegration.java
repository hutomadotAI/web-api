package com.hutoma.api.containers.sub;

/**
 * Created by Andrea MG on 30/09/2016.
 */
public class AiIntegration {

    private final String integrationId;
    private final String name;
    private final String description;
    private final String icon;
    private final boolean available;

    public AiIntegration(String integrationId, String name, String description, String icon, boolean available) {
        this.integrationId = integrationId;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.available = available;
    }

    public String getIntID() {
        return this.integrationId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIcon() {
        return this.icon;
    }

    public boolean isAvailable() {
        return this.available;
    }
}
