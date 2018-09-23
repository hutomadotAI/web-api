package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;

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
    private String trainingStatus;

    @SerializedName("ai_engine")
    private final BackendServerType aiEngine;

    @SerializedName("language")
    private SupportedLanguage aiEngineLanguage;

    @SerializedName("version")
    private String aiEngineVersion;

    @SerializedName("training_progress")
    private final double trainingProgress;

    @SerializedName("server_session_id")
    public UUID serverSessionID;

    @SerializedName("training_error")
    private double trainingError;

    @SerializedName("ai_hash")
    private String aiHash;

    private transient String serverIdentifier;

    public AiStatus(final String devId,
                    final UUID aiid,
                    final TrainingStatus trainingStatus,
                    final BackendServerType aiEngine,
                    final SupportedLanguage aiEngineLanguage,
                    final String aiEngineVersion,
                    final double trainingError,
                    final double trainingProgress,
                    final String aiHash,
                    final UUID serverSessionID) {
        this.devId = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
        this.aiEngine = aiEngine;
        this.aiEngineLanguage = aiEngineLanguage;
        this.aiEngineVersion = aiEngineVersion;
        this.trainingError = trainingError;
        this.trainingProgress = trainingProgress;
        this.aiHash = aiHash;
        this.serverSessionID = serverSessionID;
        this.serverIdentifier = null;
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

    public void setTrainingStatus(final TrainingStatus trainingStatus) {
        this.trainingStatus = trainingStatus.value();
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

    public String getServerIdentifier() {
        return this.serverIdentifier == null ? "" : this.serverIdentifier;
    }

    public void setServerIdentifier(final String serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
    }

    public SupportedLanguage getAiEngineLanguage() {
        return this.aiEngineLanguage;
    }

    public void setAiEngineLanguage(final SupportedLanguage language) {
        this.aiEngineLanguage = language;
    }

    public String getAiEngineVersion() {
        return this.aiEngineVersion;
    }

    public void setAiEngineVersion(final String version) {
        this.aiEngineVersion = version;
    }
}
