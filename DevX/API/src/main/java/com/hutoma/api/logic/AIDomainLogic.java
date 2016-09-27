package com.hutoma.api.logic;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.ApiAiDomains;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by Hutoma on 15/07/16.
 */
public class AIDomainLogic {

    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    Logger logger;

    private final String LOGFROM = "aidomainlogic";

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
            logger.logDebug(LOGFROM, "request to get all domains ");
            List<AiDomain> domainList = database.getAiDomainList();
            if (domainList.size()==0) {
                logger.logDebug(LOGFROM, "no domains found");
                return ApiError.getNotFound();
            }
            return new ApiAiDomains(domainList).setSuccessStatus();
        }
        catch (Exception e){
            logger.logError(LOGFROM, "could not get all domains; " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}