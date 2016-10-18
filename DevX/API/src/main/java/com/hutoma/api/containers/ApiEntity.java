package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntity extends ApiResult {

    private final String entity_name;
    private List<String> entity_values;

    public ApiEntity(final String entity_name) {
        this.entity_name = entity_name;
    }

    public ApiEntity(final String entity_name, final List<String> entity_values) {
        this.entity_name = entity_name;
        this.entity_values = entity_values;
    }

    public String getEntityName() {
        return this.entity_name;
    }

    public List<String> getEntityValueList() {
        return this.entity_values;
    }
}
