package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

public class ApiServerHashcode extends ApiResult {

    @SerializedName("hash")
    private final String hash;

    public ApiServerHashcode(final String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return this.hash;
    }
}
