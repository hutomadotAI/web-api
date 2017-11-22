package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

/**
 * Created by David MG on 23/05/2017.
 */
public class Entity {

    @SerializedName("entity_name")
    private final String name;

    @SerializedName("is_system")
    private final boolean isSystem;

    public Entity(final String name, final boolean isSystem) {
        this.name = name;
        this.isSystem = isSystem;
    }

    public String getName() {
        return this.name;
    }
}
