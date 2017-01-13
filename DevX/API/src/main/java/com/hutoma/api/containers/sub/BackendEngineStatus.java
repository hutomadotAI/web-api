package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public class BackendEngineStatus {

    @SerializedName("training_status")
    private TrainingStatus trainingStatus;

    @SerializedName("training_error")
    private double trainingError;

    @SerializedName("training_progress")
    private double trainingProgress;

    public BackendEngineStatus() {
        this.trainingStatus = TrainingStatus.AI_UNDEFINED;
        this.trainingError = 0.0d;
        this.trainingProgress = 0.0d;
    }

    public BackendEngineStatus(AiStatus aiStatus) {
        this.trainingStatus = aiStatus.getTrainingStatus();
        this.trainingError = aiStatus.getTrainingError();
        this.trainingProgress = aiStatus.getTrainingProgress();
    }

    public TrainingStatus getTrainingStatus() {
        return this.trainingStatus;
    }
}