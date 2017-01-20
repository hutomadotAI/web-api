package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntity extends ApiResult {

    @SerializedName("entity_name")
    private final String entityName;
    @SerializedName("entity_values")
    private List<String> entityValues;

    public ApiEntity(final String entityName) {
        this.entityName = entityName;
    }

    public ApiEntity(final String entityName, final List<String> entityValues) {
        this.entityName = entityName;
        this.entityValues = entityValues;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public List<String> getEntityValueList() {
        return this.entityValues;
    }
}
