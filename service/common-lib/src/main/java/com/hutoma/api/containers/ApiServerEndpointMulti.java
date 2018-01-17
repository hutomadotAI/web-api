package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApiServerEndpointMulti extends ApiResult {

    @SerializedName("server_endpoints")
    private List<ServerEndpointResponse> serverEndpoints;

    public Map<UUID, ServerEndpointResponse> getEndpointMap() {
        if (serverEndpoints == null) {
            return new HashMap<>();
        }
        return serverEndpoints.stream().collect(
                        Collectors.toMap(ServerEndpointResponse::getAiid,
                        Function.identity()));
    }

    public ApiServerEndpointMulti(final List<ServerEndpointResponse> serverEndpoints) {
        this.serverEndpoints = serverEndpoints;
    }

    public static class ServerEndpointResponse {

        public ServerEndpointResponse(final UUID aiid,
                                      final String serverUrl, final String serverIdentifier,
                                      final String hash) {
            this.serverUrl = serverUrl;
            this.serverIdentifier = serverIdentifier;
            this.aiid = aiid;
            this.hash = hash;
        }

        @SerializedName("serverUrl")
        private String serverUrl;

        @SerializedName("serverIdentifier")
        private String serverIdentifier;

        @SerializedName("aiid")
        private UUID aiid;

        @SerializedName("hash")
        private String hash;

        public UUID getAiid() {
            return aiid;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public String getServerIdentifier() {
            return serverIdentifier;
        }

        public String getHash() {
            return hash;
        }
    }

}
