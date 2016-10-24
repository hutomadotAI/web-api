package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntityList extends ApiResult {

    List<String> entity_name;

    public ApiEntityList(List<String> entityName) {
        this.entity_name = entityName;
    }

    public List<String> getEntities() {
        return this.entity_name;
    }
}
