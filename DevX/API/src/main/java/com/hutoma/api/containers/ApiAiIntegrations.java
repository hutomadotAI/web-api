package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.AiIntegration;

import java.util.List;

/**
 * Created by Andrea MG on 30/09/2016.
 */
public class ApiAiIntegrations extends ApiResult {

    private final List<AiIntegration> integrationList;

    public ApiAiIntegrations(List<AiIntegration> integrationList) {
        this.integrationList = integrationList;
    }

    public List<AiIntegration> getIntegrationList() {
        return this.integrationList;
    }

}
