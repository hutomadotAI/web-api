package com.hutoma.api.containers;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAdmin extends ApiResult {

    private final String dev_token;
    private final String devid;

    public ApiAdmin(String devToken, String devid) {
        this.dev_token = devToken;
        this.devid = devid;
    }

    public String getDev_token() {
        return this.dev_token;
    }

    public String getDevid() {
        return this.devid;
    }
}
