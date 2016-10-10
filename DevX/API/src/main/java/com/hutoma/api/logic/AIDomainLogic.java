package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiDomains;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiDomain;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by Hutoma on 15/07/16.
 */
public class AIDomainLogic {

    private final String LOGFROM = "aidomainlogic";
    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    Logger logger;

    @Inject
    public AIDomainLogic(Config config, JsonSerializer jsonSerializer, Database database, Logger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
    }

    public ApiResult getDomains(
        SecurityContext securityContext) {

        try {
            this.logger.logDebug(this.LOGFROM, "request to get all domains ");
            List<AiDomain> domainList = this.database.getAiDomainList();
            if (domainList.size() == 0) {
                this.logger.logDebug(this.LOGFROM, "no domains found");
                return ApiError.getNotFound();
            }
            return new ApiAiDomains(domainList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logError(this.LOGFROM, "could not get all domains; " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}