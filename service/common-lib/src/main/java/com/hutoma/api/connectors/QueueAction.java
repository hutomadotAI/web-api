package com.hutoma.api.connectors;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Optional;

public enum QueueAction {

    @SerializedName("")
    NONE(""),

    @SerializedName("train")
    TRAIN("train"),

    @SerializedName("delete")
    DELETE("delete");

    private final String value;

    QueueAction(final String value) {
        this.value = value;
    }

    public static QueueAction forValue(final String value) {
        Optional<QueueAction> queueAction = Arrays.stream(QueueAction.values())
                .filter(x -> x.value.equals(value))
                .findFirst();
        return queueAction.isPresent() ? queueAction.get() : null;
    }

    public String value() {
        return this.value;
    }
}
