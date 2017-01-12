package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAiList extends ApiResult {

    @SerializedName("ai_list")
    private final List<ApiAi> aiList;

    public ApiAiList(List<ApiAi> aiList) {
        this.aiList = aiList;
    }

    public List<ApiAi> getAiList() {
        return this.aiList;
    }
}
