package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.UserInfo;

/**
 * Created by pedrotei on 18/05/17.
 */
public class ApiUserInfo extends ApiResult {
    @SerializedName("user")
    private UserInfo userInfo;

    public ApiUserInfo(final UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
