package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.Status;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAdmin extends ApiResult {

    String dev_token;
    String devid;

    public ApiAdmin(String dev_token, String devid) {
        this.dev_token = dev_token;
        this.devid = devid;
    }

    public String getDev_token() {
        return dev_token;
    }

    public String getDevid() {
        return devid;
    }
}
