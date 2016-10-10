package com.hutoma.api.containers.sub;

/**
 * Created by David MG on 15/08/2016.
 */
public class AiDomain {

    String dom_id;
    String name;
    String description;
    String icon;
    String color;
    boolean available;

    public AiDomain(String dom_id, String name, String description, String icon, String color, boolean available) {
        this.dom_id = dom_id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.available = available;
    }

    public String getDomID() {
        return this.dom_id;
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
