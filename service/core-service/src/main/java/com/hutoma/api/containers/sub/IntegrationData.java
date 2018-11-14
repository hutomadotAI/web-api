package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class IntegrationData {

    @SerializedName("type")
    private final IntegrationType integrationType;
    @SerializedName("data")
    private Map<String, String> data = new HashMap<>();


    IntegrationData(final IntegrationData other) {
        this.integrationType = other.integrationType;
        this.data = other.data;
    }

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
