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
    private UUID serverSessionID;
    @SerializedName("ai_list")
    private List<UUID> aiList;

    /***
     * Only for use in testing
     * @param serverSessionID
     * @param aiList
     */
    public ServerAffinity(final UUID serverSessionID, final List<UUID> aiList) {
        this.serverSessionID = serverSessionID;
        this.aiList = aiList;
    }

    public List<UUID> getAiList() {
        return this.aiList;
    }

    public UUID getServerSessionID() {
        return this.serverSessionID;
    }
}
