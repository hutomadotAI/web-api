package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class EntityLogic {

    private final Config config;
    private final Logger logger;
    private final Database database;

    @Inject
    public EntityLogic(final Config config, final Logger logger, final Database database) {
        this.config = config;
        this.logger = logger;
        this.database = database;
    }

    private final String LOGFROM = "entitylogic";

    public ApiResult getEntities(final SecurityContext securityContext, final String devid) {
        try {
            this.logger.logDebug(this.LOGFROM, "request to list entities from " + devid);
            final List<ApiEntity> entityList = this.database.getEntities(devid);
            if (entityList.isEmpty()) {
                return ApiError.getNotFound();
            }
            return new ApiEntityList(entityList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(this.LOGFROM, "error getting entities: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getEntity(final SecurityContext securityContext, final String devid, final String entityName) {
        try {
            this.logger.logDebug(this.LOGFROM, "request to list entity " + entityName + " from " + devid);
            final ApiEntity entity = this.database.getEntity(devid, entityName);
            if (null == entity) {
                return ApiError.getNotFound();
            }
            return entity.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(this.LOGFROM, "error getting entity: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}
