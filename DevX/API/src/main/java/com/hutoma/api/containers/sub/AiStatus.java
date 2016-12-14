package com.hutoma.api.containers.sub;

import java.util.UUID;

/**
 * AI Status.
 */
public class AiStatus {

    private final String aiid;
    private final String dev_id;
    private final String trainingStatus;

    public AiStatus(final String devId, final UUID aiid, final TrainingStatus trainingStatus) {
        this.dev_id = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
    }

    public static TrainingStatus interpretNewStatus(TrainingStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case NEW_AI_UNDEFINED:
                return TrainingStatus.NOTHING_TO_TRAIN;
            case NEW_AI_READY_TO_TRAIN:
                return TrainingStatus.NOT_STARTED;
            case NEW_AI_TRAINING:
                return TrainingStatus.QUEUED;
            case NEW_AI_TRAINING_WITH_CHAT:
                return TrainingStatus.IN_PROGRESS;
            case NEW_AI_READY_FOR_CHAT:
                return TrainingStatus.COMPLETED;
            case NEW_AI_ERROR:
                return TrainingStatus.MALFORMEDFILE;
            default:
                break;
        }
        return status;
    }

    public UUID getAiid() {
        return this.aiid == null ? null : UUID.fromString(this.aiid);
    }

    public String getDevId() {
        return this.dev_id;
    }

    public TrainingStatus getTrainingStatus() {
        return interpretNewStatus(TrainingStatus.forValue(this.trainingStatus));
    }
}
