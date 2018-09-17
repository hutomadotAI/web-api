package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.SupportedLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerEndpointRequestMulti {

    @SerializedName("endpoints")
    private final List<ServerEndpointRequest> endpointRequests;

    public List<ServerEndpointRequest> getEndpointRequests() {
        return this.endpointRequests;
    }

    public ServerEndpointRequestMulti() {
        this.endpointRequests = new ArrayList<>();
    }

    public void add(final ServerEndpointRequest endpointRequest) {
        this.endpointRequests.add(endpointRequest);
    }

    public static class ServerEndpointRequest {

        @SerializedName("aiid")
        private final UUID aiid;

        @SerializedName("language")
        private final SupportedLanguage language;

        @SerializedName("server_version")
        private final String serverVersion;

        @SerializedName("already_tried")
        private final List<String> alreadyTried;

        public ServerEndpointRequest(final UUID aiid,
                                     final SupportedLanguage language,
                                     final String serverVersion,
                                     final List<String> alreadyTried) {
            this.aiid = aiid;
            this.language = language;
            this.serverVersion  = serverVersion;
            this.alreadyTried = alreadyTried;
        }

        public UUID getAiid() {
            return this.aiid;
        }

        public SupportedLanguage getLanguage() {
            return this.language;
        }

        public String getServerVersion() {
            return this.serverVersion;
        }

        public List<String> getAlreadyTried() {
            return this.alreadyTried;
        }

    }
}
