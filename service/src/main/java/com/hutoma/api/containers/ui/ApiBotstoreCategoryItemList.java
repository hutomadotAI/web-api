package com.hutoma.api.containers.ui;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiResult;

import java.util.HashMap;
import java.util.List;

/**
 * List of items in the botstore.
 */
public class ApiBotstoreCategoryItemList extends ApiResult {
    @SerializedName("categories")
    private HashMap<String, List<BotstoreItem>> categories = new HashMap<>();

    public ApiBotstoreCategoryItemList(final HashMap<String, List<BotstoreItem>> categories) {
        this.categories = categories;
    }

    public HashMap<String, List<BotstoreItem>> getCategoriesMap() {
        return this.categories;
    }
}
