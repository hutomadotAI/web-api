package com.hutoma.api.connectors;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Optional;

public enum BackendServerType {

    @SerializedName("aiml")
    AIML("aiml"),

    @SerializedName("emb")
    EMB("emb"),

    @SerializedName("doc2chat")
    DOC2CHAT("doc2chat");

    private final String value;

    BackendServerType(final String value) {
        this.value = value;
    }

    public static BackendServerType forValue(final String value) {
        Optional<BackendServerType> serverType = Arrays.stream(BackendServerType.values())
                .filter(x -> x.value.equals(value))
                .findFirst();
        return serverType.orElse(null);
    }

    public String value() {
        return this.value;
    }

}
