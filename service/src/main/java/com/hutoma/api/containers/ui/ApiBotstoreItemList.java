package com.hutoma.api.containers.ui;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiResult;

import java.util.List;

/**
 * List of items in the botstore.
 */
public class ApiBotstoreItemList extends ApiResult {
    @SerializedName("items")
    private final List<BotstoreItem> items;
    @SerializedName("page_start")
    private final int startItem;
    @SerializedName("total_page")
    private final int totalPage;
    @SerializedName("total_results")
    private final int totalResults;

    public ApiBotstoreItemList(final List<BotstoreItem> items, final int startItem, final int totalPage,
                               final int totalResults) {
        this.items = items;
        this.startItem = startItem;
        this.totalPage = totalPage;
        this.totalResults = totalResults;
    }

    public List<BotstoreItem> getItems() {
        return this.items;
    }

    public int getStartItem() {
        return this.startItem;
    }

    public int getTotalPage() {
        return this.totalPage;
    }

    public int getTotalResults() {
        return this.totalResults;
    }
}
