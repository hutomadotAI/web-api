package com.hutoma.api.containers.ui;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiResult;

/**
 * Bot store item.
 */
public class ApiBotstoreItem extends ApiResult {

    @SerializedName("item")
    private final BotstoreItem item;

    public ApiBotstoreItem(final BotstoreItem item) {
        this.item = item;
    }

    public BotstoreItem getItem() {
        return this.item;
    }
}
