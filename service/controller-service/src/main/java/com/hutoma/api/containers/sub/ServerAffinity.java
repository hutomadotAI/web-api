package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.SupportedLanguage;

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

    @SerializedName("language")
    private SupportedLanguage language;

    @SerializedName("version")
    private String version;

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
