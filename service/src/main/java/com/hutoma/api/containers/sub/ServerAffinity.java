package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Expected payload from a back-end server
 * To tell the API which AIs it has in memory and would be the best server to serve chat requests for.
 */
public class ServerAffinity {

    @SerializedName("server_session_id")
    public UUID serverSessionID;
    @SerializedName("server_type")
    public String serverType;
    @SerializedName("ai_list")
    public List<UUID> aiList;

    /***
     * Only for use in testing
     * @param serverSessionID
     * @param serverType
     * @param aiList
     */
    public ServerAffinity(final UUID serverSessionID, final String serverType, final List<UUID> aiList) {
        this.serverSessionID = serverSessionID;
        this.serverType = serverType;
        this.aiList = aiList;
    }

    public String getServerType() {
        return this.serverType;
    }

    public List<UUID> getAiList() {
        return this.aiList;
    }

    public UUID getServerSessionID() {
        return this.serverSessionID;
    }
}