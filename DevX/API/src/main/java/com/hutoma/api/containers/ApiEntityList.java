package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntityList extends ApiResult {

    List<String> entity_name;

    public ApiEntityList(List<String> entity_name) {
        this.entity_name = entity_name;
    }

    public List<String> getEntities() {
        return this.entity_name;
    }
}
