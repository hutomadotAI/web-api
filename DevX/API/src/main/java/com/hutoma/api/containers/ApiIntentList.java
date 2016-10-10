package com.hutoma.api.containers;

import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntentList extends ApiResult {

    private List<String> intent_name;
    private UUID aiid;

    public ApiIntentList(UUID aiid, List<String> intent_name) {
        this.intent_name = intent_name;
        this.aiid = aiid;
    }

    public List<String> getIntentNames() {
        return this.intent_name;
    }
}
