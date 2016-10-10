package com.hutoma.api.containers;

import java.util.List;

/**
 * Created by David MG on 19/08/2016.
 */
public class ApiMemory extends ApiResult {

    List<ApiMemoryToken> memoryList;

    public ApiMemory(List<ApiMemoryToken> memoryList) {
        this.memoryList = memoryList;
    }

    public List<ApiMemoryToken> getMemoryList() {
        return this.memoryList;
    }
}
