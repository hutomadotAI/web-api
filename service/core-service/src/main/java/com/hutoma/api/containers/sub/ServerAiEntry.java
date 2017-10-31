package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class ServerAiEntry {

    @SerializedName("ai_id")
    private final UUID aiid;
    @SerializedName("training_status")
    private final TrainingStatus trainingStatus;
    @SerializedName("ai_hash")
    private final String aiHash;

    /***
     * This will only be used in unit tests
     * @param aiid
     * @param trainingStatus
     */
    public ServerAiEntry(final UUID aiid, final TrainingStatus trainingStatus, final String aiHash) {
        this.aiid = aiid;
        this.trainingStatus = trainingStatus;
        this.aiHash = aiHash;
    }

    public TrainingStatus getTrainingStatus() {
        return trainingStatus;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public String getAiHash() {
        return aiHash;
    }
}