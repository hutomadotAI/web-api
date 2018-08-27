package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.FeatureToggler;

public class ApiExperiment extends ApiResult {
    @SerializedName("name")
    private final String name;
    @SerializedName("state")
    private final String state;

    public ApiExperiment(final String name, final String state) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public FeatureToggler.FeatureState getState() {
        return FeatureToggler.FeatureState.valueOf(this.state);
    }
}
