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
    private final BackendServerType aiEngine;

    @SerializedName("training_progress")
    private final double trainingProgress;

    @SerializedName("server_session_id")
    public UUID serverSessionID;

    @SerializedName("training_error")
    private double trainingError;

    @SerializedName("ai_hash")
    private String aiHash;

    public AiStatus(final String devId, final UUID aiid, final TrainingStatus trainingStatus,
                    final BackendServerType aiEngine,
                    final double trainingError, final double trainingProgress, final String aiHash,
                    final UUID serverSessionID) {
        this.devId = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
        this.aiEngine = aiEngine;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
        this.aiHash = aiHash;
        this.serverSessionID = serverSessionID;
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

    public BackendServerType getAiEngine() {
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

    public String getAiHash() {
        return this.aiHash;
    }

    public UUID getServerSessionID() {
        return this.serverSessionID;
    }

    public String getEndpoint() {
        return "unknown";
    }
}
