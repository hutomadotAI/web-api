package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.DeveloperInfo;

/**
 * API Developer Info container.
 */
public class ApiDeveloperInfo extends ApiResult {

    private final DeveloperInfo info;

    public ApiDeveloperInfo(final DeveloperInfo info) {
        this.info = info;
    }

    public DeveloperInfo getInfo() {
        return this.info;
    }
}
