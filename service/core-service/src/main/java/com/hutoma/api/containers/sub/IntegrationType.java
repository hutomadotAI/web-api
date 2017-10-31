package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Optional;

public enum IntegrationType {

    @SerializedName("")
    NONE(""),

    @SerializedName("facebook")
    FACEBOOK("facebook"),

    @SerializedName("slack")
    SLACK("slack");

    private final String value;

    IntegrationType(final String value) {
        this.value = value;
    }

    public static IntegrationType forValue(final String value) {
        Optional<IntegrationType> integrationType = Arrays.stream(IntegrationType.values())
                .filter(x -> x.value.equals(value))
                .findFirst();
        return integrationType.isPresent() ? integrationType.get() : null;
    }

    public String value() {
        return this.value;
    }
}
