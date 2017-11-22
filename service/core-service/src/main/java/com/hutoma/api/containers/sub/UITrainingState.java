package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public class UITrainingState {

    @SerializedName("status")
    public final Status uiTrainingStatus;
    @SerializedName("progress")
    public final int trainingProgressPercent;
    @SerializedName("message")
    public final String errorMessage;

    public UITrainingState(final TrainingStatus apiTrainingStatus, final double trainingProgress,
                           final String errorMessage) {

        switch (apiTrainingStatus) {
            case AI_READY_TO_TRAIN:
                this.uiTrainingStatus = Status.retrain;
                break;
            case AI_TRAINING_QUEUED:
            case AI_TRAINING:
                this.uiTrainingStatus = Status.training;
                break;
            case AI_TRAINING_COMPLETE:
                this.uiTrainingStatus = Status.completed;
                break;
            case AI_TRAINING_STOPPED:
            case AI_UNDEFINED:
                this.uiTrainingStatus = Status.empty;
                break;
            case AI_ERROR:
            default:
                this.uiTrainingStatus = Status.error;
                break;
        }
        this.errorMessage = this.uiTrainingStatus == Status.error ? errorMessage : "";
        this.trainingProgressPercent = (int) Math.round(trainingProgress * 100.0);
    }

    /***
     * Given the status of the servers, determine whether we can chat to this bot
     * @param wnetTrainingStatus
     * @return
     */
    public static boolean canChat(TrainingStatus wnetTrainingStatus) {
        return wnetTrainingStatus == TrainingStatus.AI_TRAINING_COMPLETE;
    }

    public Status getUiTrainingStatus() {
        return this.uiTrainingStatus;
    }

    public int getTrainingProgressPercent() {
        return this.trainingProgressPercent;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public enum Status {
        empty,
        training,
        completed,
        error,
        retrain
    }
}
