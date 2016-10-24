package com.hutoma.api.containers.sub;

/**
 * Created by David MG on 15/08/2016.
 */
public class AiDomain {

    private final String domainId;
    private final String name;
    private final String description;
    private final String icon;
    private final String color;
    private final boolean available;

    public AiDomain(String domainId, String name, String description, String icon, String color, boolean available) {
        this.domainId = domainId;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.available = available;
    }

    public String getDomID() {
        return this.domainId;
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

    public String getColor() {
        return this.color;
    }

    public boolean isAvailable() {
        return this.available;
    }
}
