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
    private final List<ApiIntent> intents;

    public ApiIntentList(UUID aiid, List<String> intentName) {
        this.intentName = intentName;
        this.aiid = aiid;
        this.intents = null;
    }

    public ApiIntentList(UUID aiid, List<String> intentName, List<ApiIntent> intents) {
        this.intentName = intentName;
        this.aiid = aiid;
        this.intents = intents;
    }

    public List<String> getIntentNames() {
        return this.intentName;
    }

    public List<ApiIntent> getIntents() {
        return this.intents;
    }
}
