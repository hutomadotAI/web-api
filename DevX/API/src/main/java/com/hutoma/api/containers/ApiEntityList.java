package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiEntityList extends ApiResult {

    List<ApiEntity> entities;

    public ApiEntityList(final List<ApiEntity> entities) {
        this.entities = entities;
    }

    public List<ApiEntity> getEntities() {
        return this.entities;
    }
}
