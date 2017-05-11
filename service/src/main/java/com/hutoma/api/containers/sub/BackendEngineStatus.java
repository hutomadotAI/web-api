package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.UUID;

public class BackendEngineStatus {

    @SerializedName("training_status")
    private TrainingStatus trainingStatus;

    @SerializedName("training_error")
    private double trainingError;

    @SerializedName("training_progress")
    private double trainingProgress;

    @SerializedName("ai_hash")
    private String aiHash;

    private transient QueueAction queueAction;
    private transient String serverIdentifier;
    private transient DateTime updateTime;
    private transient UUID aiid;
    private transient String devId;
    private transient boolean deleted;

    public BackendEngineStatus(final TrainingStatus trainingStatus, final double trainingError,
                               final double trainingProgress) {
        this.trainingStatus = trainingStatus;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
        this.deleted = false;
    }

    public BackendEngineStatus(final UUID aiid,
                               final TrainingStatus trainingStatus, final double trainingError,
                               final double trainingProgress, final QueueAction action,
                               final String serverEndpoint,
                               final DateTime updateTime) {
        this(trainingStatus, trainingError, trainingProgress);
        this.aiid = aiid;
        this.queueAction = action;
        this.serverIdentifier = serverEndpoint;
        this.updateTime = updateTime;
    }

    public BackendEngineStatus() {
        this(TrainingStatus.AI_UNDEFINED, 0.0d, 0.0d);
        this.aiHash = "";
    }

    public BackendEngineStatus(AiStatus aiStatus) {
        this(aiStatus.getTrainingStatus(), aiStatus.getTrainingError(), aiStatus.getTrainingProgress());
        this.aiHash = aiStatus.getAiHash();
    }

    public DateTime getUpdateTime() {
        return this.updateTime;
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

    public String getServerIdentifier() {
        return this.serverIdentifier;
    }

    public QueueAction getQueueAction() {
        return this.queueAction;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public String getDevId() {
        return this.devId;
    }

    public void setDevId(final String devId) {
        this.devId = devId;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }
}