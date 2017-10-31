package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntentList extends ApiResult {

    @SerializedName("intent_name")
    private final List<String> intentName;
    private final UUID aiid;

    public ApiIntentList(UUID aiid, List<String> intentName) {
        this.intentName = intentName;
        this.aiid = aiid;
    }

    public List<String> getIntentNames() {
        return this.intentName;
    }
}
