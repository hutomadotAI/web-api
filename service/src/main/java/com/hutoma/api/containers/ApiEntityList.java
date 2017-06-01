package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.Entity;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntityList extends ApiResult {

    @SerializedName("entities")
    private final List<Entity> entities;

    public ApiEntityList(final List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return this.entities;
    }
}
