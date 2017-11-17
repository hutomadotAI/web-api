package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.connectors.ServerTrackerInfo;

import java.util.Map;

public class ApiServerTrackerInfoMap extends ApiResult {

    @SerializedName("map")
    private final Map<String, ServerTrackerInfo> map;

    public ApiServerTrackerInfoMap(final Map<String, ServerTrackerInfo> map) {
        this.map = map;
    }

    public Map<String, ServerTrackerInfo> getMap() {
        return map;
    }
}
