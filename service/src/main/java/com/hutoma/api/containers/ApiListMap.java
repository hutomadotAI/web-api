package com.hutoma.api.containers;

import java.util.List;
import java.util.Map;

/**
 * List of maps.
 */
public class ApiListMap extends ApiResult {

    private List<Map<String, Object>> objects;

    public ApiListMap(final List<Map<String, Object>> objects) {
        this.objects = objects;
    }
}
