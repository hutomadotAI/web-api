package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.Integration;

import java.util.List;

/**
 * Created by Andrea on 30/09/2016.
 */
public class ApiIntegrationList extends ApiResult {

    @SerializedName("integration_list")
    private final List<Integration> integrationList;

    public ApiIntegrationList(List<Integration> integrationList) {
        this.integrationList = integrationList;
    }

    public List<Integration> getIntegrationList() {
        return this.integrationList;
    }

}
