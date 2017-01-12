package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * AI Status.
 */
public class AiStatus {

    @SerializedName("ai_id")
    private final String aiid;

    @SerializedName("dev_id")
    private final String devId;

    @SerializedName("training_status")
    private final String trainingStatus;

    @SerializedName("ai_engine")
    private final String aiEngine;
    @SerializedName("training_progress")
    private final double trainingProgress;
    @SerializedName("training_error")
    private double trainingError;

    public AiStatus(final String devId, final UUID aiid, final TrainingStatus trainingStatus, final String aiEngine,
                    final double trainingError, final double trainingProgress) {
        this.devId = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
        this.aiEngine = aiEngine;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
    }

    public UUID getAiid() {
        return this.aiid == null ? null : UUID.fromString(this.aiid);
    }

    public String getDevId() {
        return this.devId;
    }

    public TrainingStatus getTrainingStatus() {
        return TrainingStatus.forValue(this.trainingStatus);
    }

    public String getAiEngine() {
        return this.aiEngine;
    }

    public double getTrainingError() {
        return this.trainingError;
    }

    public void setTrainingError(final double trainingError) {
        this.trainingError = trainingError;
    }

    public double getTrainingProgress() {
        return this.trainingProgress;
    }

}
