package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiStore;

import java.util.List;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAiStore extends ApiResult {

    List<AiStore> _domainList;

    public ApiAiStore(List<AiStore> domainList) {
        this._domainList = domainList;
    }

    public List<AiStore> getDomainList() {
        return this._domainList;
    }

}
