package com.hutoma.api.connectors;

import com.google.gson.annotations.SerializedName;

public class ServerTrackerInfo {

    @SerializedName("training_capacity")
    private int trainingCapacity;
    @SerializedName("chat_capacity")
    private int chatCapacity;
    @SerializedName("server_identifier")
    private String serverIdentifier;
    @SerializedName("server_url")
    private String serverUrl;
    @SerializedName("can_train")
    private boolean canTrain;
    @SerializedName("is_endpoint_verified")
    private boolean isEndpointVerified;

    public ServerTrackerInfo(final String serverUrl, final String serverIdentifier, final int chatCapacity,
                             final int trainingCapacity, final boolean canTrain, final boolean isEndpointVerified) {
        this.serverIdentifier = serverIdentifier;
        this.serverUrl = serverUrl;
        this.chatCapacity = chatCapacity;
        this.trainingCapacity = trainingCapacity;
        this.canTrain = canTrain;
        this.isEndpointVerified = isEndpointVerified;
    }

    public int getTrainingCapacity() {
        return this.trainingCapacity;
    }

    public int getChatCapacity() {
        return this.chatCapacity;
    }

    public String getServerIdentifier() {
        return this.serverIdentifier;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public boolean canTrain() {
        return this.canTrain;
    }

    public boolean isEndpointVerified() {
        return this.isEndpointVerified;
    }
}
