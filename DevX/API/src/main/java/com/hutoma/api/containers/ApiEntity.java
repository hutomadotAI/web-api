package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntity extends ApiResult {

    private final String entity_name;
    private List<String> entity_values;

    public ApiEntity(final String entityName) {
        this.entity_name = entityName;
    }

    public ApiEntity(final String entityName, final List<String> entityValues) {
        this.entity_name = entityName;
        this.entity_values = entityValues;
    }

    public String getEntityName() {
        return this.entity_name;
    }

    public List<String> getEntityValueList() {
        return this.entity_values;
    }
}
