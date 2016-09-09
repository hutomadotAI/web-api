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
        return dom_id;
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

    public String getColor() {
        return color;
    }

    public boolean isAvailable() {
        return available;
    }
}
