package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public class BackendEngineStatus {

    @SerializedName("training_status")
    private TrainingStatus trainingStatus;

    @SerializedName("training_error")
    private double trainingError;

    @SerializedName("training_progress")
    private double trainingProgress;

    @SerializedName("ai_hash")
    private String aiHash;

    public BackendEngineStatus(final TrainingStatus trainingStatus, final double trainingError, final double trainingProgress) {
        this.trainingStatus = trainingStatus;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
    }

    public BackendEngineStatus() {
        this(TrainingStatus.AI_UNDEFINED, 0.0d, 0.0d);
        this.aiHash = "";
    }

    public BackendEngineStatus(AiStatus aiStatus) {
        this.trainingStatus = aiStatus.getTrainingStatus();
        this.trainingError = aiStatus.getTrainingError();
        this.trainingProgress = aiStatus.getTrainingProgress();
        this.aiHash = aiStatus.getAiHash();
    }

    public TrainingStatus getTrainingStatus() {
        return this.trainingStatus;
    }

    public double getTrainingError() {
        return this.trainingError;
    }

    public double getTrainingProgress() {
        return this.trainingProgress;
    }
}