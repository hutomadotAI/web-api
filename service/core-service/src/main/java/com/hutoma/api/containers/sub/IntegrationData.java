package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class IntegrationData {

    @SerializedName("type")
    private final IntegrationType integrationType;
    @SerializedName("data")
    private final Map<String, String> data = new HashMap<>();


    IntegrationData() {
        this(IntegrationType.NONE);
    }

    IntegrationData(final IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    protected Map<String, String> getData() {
        return this.data;
    }

    public IntegrationType getIntegrationType() {
        return this.integrationType;
    }
}
