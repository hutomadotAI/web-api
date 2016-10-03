package com.hutoma.api.containers.sub;

/**
 * Created by Andrea MG on 30/09/2016.
 */
public class AiIntegration {

    String int_id;
    String name;
    String description;
    String icon;
    boolean available;

    public AiIntegration(String int_id, String name, String description, String icon, boolean available) {
        this.int_id = int_id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.available = available;
    }

    public String getIntID() {
        return int_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isAvailable() {
        return available;
    }
}
