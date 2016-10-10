package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAiList extends ApiResult {

    List<ApiAi> ai_list;

    public ApiAiList(List<ApiAi> ai_list) {
        this.ai_list = ai_list;
    }

    public List<ApiAi> getAiList() {
        return this.ai_list;
    }
}
