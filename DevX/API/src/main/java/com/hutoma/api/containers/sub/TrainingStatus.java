package com.hutoma.api.containers.sub;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by mauriziocibelli on 11/10/16.
 */
public enum TrainingStatus {
    STOPPED("training_stopped"),
    NOT_STARTED("training_not_started"),
    QUEUED("training_queued"),
    IN_PROGRESS("training_in_progress"),
    STOPPED_MAX_TIME("training_stopped_maxtime"),
    COMPLETED("training_completed"),
    DELETED("training_deleted");
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
