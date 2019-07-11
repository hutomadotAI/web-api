package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;

/***
 * List of available servers and their capabilities
 */
public class ApiServersAvailable extends ApiResult {

    @SerializedName("servers")
    private final Collection<ServiceIdentity> servers;

    public ApiServersAvailable(Collection<ServiceIdentity> servers) {
        this.servers = servers;
    }

    public Collection<ServiceIdentity> getServers() {
        return servers;
    }
}
