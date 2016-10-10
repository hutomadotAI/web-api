package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiDomain;

import java.util.List;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAiDomains extends ApiResult {

    List<AiDomain> _domainList;

    public ApiAiDomains(List<AiDomain> domainList) {
        this._domainList = domainList;
    }

    public List<AiDomain> getDomainList() {
        return this._domainList;
    }

}
