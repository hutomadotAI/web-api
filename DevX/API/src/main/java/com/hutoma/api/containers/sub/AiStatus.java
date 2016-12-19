package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * AI Status.
 */
public class AiStatus {

    @SerializedName("ai_id")
    private final String aiid;
    private final String dev_id;

    @SerializedName("training_status")
    private final String trainingStatus;

    @SerializedName("ai_engine")
    private final String aiEngine;

    @SerializedName("training_error")
    private final double trainingError;

    @SerializedName("training_progress")
    private final double trainingProgress;

    public AiStatus(final String devId, final UUID aiid, final TrainingStatus trainingStatus, final String aiEngine,
                    final double trainingError, final double trainingProgress) {
        this.dev_id = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
        this.aiEngine = aiEngine;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
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
            case NEW_AI_TRAINING_QUEUED:
                return TrainingStatus.QUEUED;
            case NEW_AI_TRAINING:
                return TrainingStatus.IN_PROGRESS;
            case NEW_AI_TRAINING_COMPLETE:
                return TrainingStatus.COMPLETED;
            case NEW_AI_TRAINING_STOPPED:
                return TrainingStatus.STOPPED;
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

    public String getAiEngine() {
        return this.aiEngine;
    }

    public double getTrainingError() {
        return this.trainingError;
    }

    public double getTrainingProgress() {
        return this.trainingProgress;
    }

}
