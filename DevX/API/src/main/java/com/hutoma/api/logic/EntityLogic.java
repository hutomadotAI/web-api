package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 05/10/2016.
 */
public class EntityLogic {

    private static final String LOGFROM = "entitylogic";
    private final Config config;
    private final Logger logger;
    private final DatabaseEntitiesIntents database;
    private final TrainingLogic trainingLogic;

    @Inject
    public EntityLogic(final Config config, final Logger logger, final DatabaseEntitiesIntents database,
                       final TrainingLogic trainingLogic) {
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.trainingLogic = trainingLogic;
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
            stopTrainingIfEntityInUse(devid, entityName);
            this.database.writeEntity(devid, entityName, entity);
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseIntegrityViolationException dive) {
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
            stopTrainingIfEntityInUse(devid, entityName);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error writing entity: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    private List<UUID> getAisWithEntityInUse(final String devid, final String entityName) {
        try {
            return this.database.getAisForEntity(devid, entityName);
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error getting entities: " + e.toString());
        }
        return new ArrayList<>();
    }

    private void stopTrainingIfEntityInUse(final String devid, final String entityName) {
        List<UUID> ais = getAisWithEntityInUse(devid, entityName);
        if (!ais.isEmpty()) {
            for (UUID ai : ais) {
                this.trainingLogic.stopTraining(null, devid, ai);
            }
        }
    }
}
