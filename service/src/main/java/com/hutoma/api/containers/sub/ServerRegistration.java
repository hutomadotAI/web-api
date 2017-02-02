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
    public List<ServerRegistrationAi> aiList;

    @SerializedName("server_type")
    public String serverType;

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
    public ServerRegistration(final String serverType, final int trainingCapacity, final int chatCapacity) {
        this.aiList = new ArrayList<>();
        this.serverType = serverType;
        this.trainingCapacity = trainingCapacity;
        this.chatCapacity = chatCapacity;
    }

    /***
     * This will only be used in unit tests
     * @param uuid
     * @param trainingStatus
     */
    public void addAI(UUID uuid, TrainingStatus trainingStatus) {
        this.aiList.add(new ServerRegistrationAi(uuid, trainingStatus));
    }

    public List<ServerRegistrationAi> getAiList() {
        return this.aiList;
    }

    public String getServerType() {
        return this.serverType;
    }

    public class ServerRegistrationAi {

        @SerializedName("ai_id")
        private UUID aiid;
        @SerializedName("training_status")
        private TrainingStatus trainingStatus;

        /***
         * This will only be used in unit tests
         * @param aiid
         * @param trainingStatus
         */
        public ServerRegistrationAi(final UUID aiid, final TrainingStatus trainingStatus) {
            this.aiid = aiid;
            this.trainingStatus = trainingStatus;
        }
    }
}
