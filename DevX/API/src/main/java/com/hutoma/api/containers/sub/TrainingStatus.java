package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by mauriziocibelli on 11/10/16.
 */
public enum TrainingStatus {

    // ai created but no training uploaded
    @SerializedName("ai_undefined")
    AI_UNDEFINED("ai_undefined"),

    // training file uploaded but training not started
    @SerializedName("ai_ready_to_train")
    AI_READY_TO_TRAIN("ai_ready_to_train"),

    // queued for training but server has not actually started training yet
    @SerializedName("ai_training_queued")
    AI_TRAINING_QUEUED("ai_training_queued"),

    // training in progress
    @SerializedName("ai_training")
    AI_TRAINING("ai_training"),

    // training has been stopped but is not complete
    @SerializedName("ai_training_stopped")
    AI_TRAINING_STOPPED("ai_training_stopped"),

    // training has completed
    @SerializedName("ai_training_complete")
    AI_TRAINING_COMPLETE("ai_training_complete"),

    // training encountered an error
    @SerializedName("ai_error")
    AI_ERROR("ai_error");

    private final String value;

    TrainingStatus(final String value) {
        this.value = value;
    }

    public static TrainingStatus forValue(final String value) {
        Optional<TrainingStatus> status = Arrays.stream(TrainingStatus.values())
                .filter(x -> x.value.equals(value))
                .findFirst();
        return status.isPresent() ? status.get() : null;
    }

    public String value() {
        return this.value;
    }
}
