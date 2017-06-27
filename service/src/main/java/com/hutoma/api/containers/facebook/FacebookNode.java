package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacebookNode {

    private String id;
    private String name;
    private List<String> perms;

    @SerializedName("access_token")
    private String accessToken;

    public FacebookNode(final String id, final String name, final List<String> perms, final String accessToken) {
        this.id = id;
        this.name = name;
        this.perms = perms;
        this.accessToken = accessToken;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getPerms() {
        return this.perms;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}
