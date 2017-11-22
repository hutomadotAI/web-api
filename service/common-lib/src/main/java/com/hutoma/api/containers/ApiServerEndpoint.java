package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.connectors.IServerEndpoint;

public class ApiServerEndpoint extends ApiResult {

    @SerializedName("serverUrl")
    private final String serverUrl;

    @SerializedName("serverIdentifier")
    private final String serverIdentifier;

    public ApiServerEndpoint(final IServerEndpoint endpoint) {
        this.serverUrl = endpoint.getServerUrl();
        this.serverIdentifier = endpoint.getServerIdentifier();
    }

    public IServerEndpoint asServerEndpoint() {
        return new IServerEndpoint() {
            @Override
            public String getServerUrl() {
                return ApiServerEndpoint.this.serverUrl;
            }

            @Override
            public String getServerIdentifier() {
                return ApiServerEndpoint.this.serverIdentifier;
            }
        };
    }
}
