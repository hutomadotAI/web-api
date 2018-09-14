package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;

import javax.annotation.processing.SupportedOptions;
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
    private List<ServerAiEntry> aiList;

    @SerializedName("server_type")
    private BackendServerType serverType;

    @SerializedName("server_url")
    private String serverUrl;

    @SerializedName("training_capacity")
    private int trainingCapacity;

    @SerializedName("chat_capacity")
    private int chatCapacity;

    @SerializedName("language")
    private SupportedLanguage language;

    @SerializedName("version")
    private String version;

    /***
     * This will only be used in unit tests
     * @param serverType
     * @param trainingCapacity
     * @param chatCapacity
     */
    public ServerRegistration(final BackendServerType serverType,
                              final String serverUrl,
                              final int trainingCapacity,
                              final int chatCapacity,
                              final SupportedLanguage language,
                              final String version) {
        this.aiList = new ArrayList<>();
        this.serverType = serverType;
        this.serverUrl = serverUrl;
        this.trainingCapacity = trainingCapacity;
        this.chatCapacity = chatCapacity;
        this.language = language;
        this.version = version;
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

    public SupportedLanguage getLanguage() {
        return this.language;
    }

    public void setLanguage(final SupportedLanguage language) {
        this.language = language;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
