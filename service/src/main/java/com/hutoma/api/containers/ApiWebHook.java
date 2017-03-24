package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.WebHook;

/**
 * WebHook API Result.
 */
public class ApiWebHook extends ApiResult {

    private final WebHook webHook;

    public ApiWebHook(final WebHook webHook) {
        this.webHook = webHook;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }
}
