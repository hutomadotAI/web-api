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

    private static final String LOGFROM = "entitylogic";
    private final Config config;
    private final Logger logger;
    private final Database database;

    @Inject
    public EntityLogic(final Config config, final Logger logger, final Database database) {
        this.config = config;
        this.logger = logger;
        this.database = database;
    }

    public ApiResult getEntities(final SecurityContext securityContext, final String devid) {
        try {
            this.logger.logDebug(LOGFROM, "request to list entities from " + devid);
            final List<String> entityList = this.database.getEntities(devid);
            if (entityList.isEmpty()) {
                return ApiError.getNotFound();
            }
            return new ApiEntityList(entityList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error getting entities: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getEntity(final SecurityContext securityContext, final String devid, final String entityName) {
        try {
            this.logger.logDebug(LOGFROM, "request to list entity " + entityName + " from " + devid);
            final ApiEntity entity = this.database.getEntity(devid, entityName);
            return entity.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error getting entity: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeEntity(final String devid, final String entityName, final ApiEntity entity) {
        try {
            this.logger.logDebug(LOGFROM, "request to edit entity " + entityName + " from " + devid);
            this.database.writeEntity(devid, entityName, entity);
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseIntegrtityViolationException dive) {
            this.logger.logDebug(LOGFROM, "attempt to rename to existing name");
            return ApiError.getBadRequest("entity name already in use");
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error writing entity: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteEntity(final String devid, final String entityName) {
        try {
            this.logger.logDebug(LOGFROM, "request to delete entity " + entityName + " from " + devid);
            if (!this.database.deleteEntity(devid, entityName)) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error writing entity: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}
