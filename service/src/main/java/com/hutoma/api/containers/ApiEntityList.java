package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntityList extends ApiResult {

    @SerializedName("entity_name")
    private final List<String> entityName;

    public ApiEntityList(List<String> entityName) {
        this.entityName = entityName;
    }

    public List<String> getEntities() {
        return this.entityName;
    }
}
