package com.hutoma.api.connectors;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Optional;

public enum BackendServerType {

    @SerializedName("wnet")
    WNET("wnet"),

    @SerializedName("aiml")
    AIML("aiml");

    private final String value;

    BackendServerType(final String value) {
        this.value = value;
    }

    public static BackendServerType forValue(final String value) {
        Optional<BackendServerType> serverType = Arrays.stream(BackendServerType.values())
                .filter(x -> x.value.equals(value))
                .findFirst();
        return serverType.isPresent() ? serverType.get() : null;
    }

    public String value() {
        return this.value;
    }

}
