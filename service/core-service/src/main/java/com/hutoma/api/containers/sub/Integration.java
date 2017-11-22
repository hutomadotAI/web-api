package com.hutoma.api.containers.sub;

/**
 * Created by Andrea MG on 30/09/2016.
 */
public class Integration {

    private final int id;
    private final String name;
    private final String description;
    private final String icon;
    private final boolean available;

    public Integration(int id, String name, String description, String icon, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.available = available;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

}
