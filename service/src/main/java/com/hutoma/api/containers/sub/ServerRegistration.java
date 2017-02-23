package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Expected payload from a back-end server
 * To tell it what kind of server it is,
 * what AIs it knows about and
 * what its training and chat capacity are
 */
public class ServerRegistration {

    @SerializedName("ai_list")
    public List<ServerAiEntry> aiList;

    @SerializedName("server_type")
    public BackendServerType serverType;

    @SerializedName("server_url")
    public String serverUrl;

    @SerializedName("training_capacity")
    public int trainingCapacity;

    @SerializedName("chat_capacity")
    public int chatCapacity;

    /***
     * This will only be used in unit tests
     * @param serverType
     * @param trainingCapacity
     * @param chatCapacity
     */
    public ServerRegistration(final BackendServerType serverType, final String serverUrl,
                              final int trainingCapacity, final int chatCapacity) {
        this.aiList = new ArrayList<>();
        this.serverType = serverType;
        this.serverUrl = serverUrl;
        this.trainingCapacity = trainingCapacity;
        this.chatCapacity = chatCapacity;
    }

    /***
     * This will only be used in unit tests
     * @param uuid
     * @param trainingStatus
     */
    public void addAI(UUID uuid, TrainingStatus trainingStatus, String trainingHash) {
        this.aiList.add(new ServerAiEntry(uuid, trainingStatus, trainingHash));
    }

    public List<ServerAiEntry> getAiList() {
        return this.aiList;
    }

    public BackendServerType getServerType() {
        return this.serverType;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public int getTrainingCapacity() {
        return this.trainingCapacity;
    }

    public int getChatCapacity() {
        return this.chatCapacity;
    }

}