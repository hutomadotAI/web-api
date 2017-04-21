package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
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

/**
 * Created by David MG on 05/10/2016.
 */
public class EntityLogic {

    private static final String LOGFROM = "entitylogic";
    private final Config config;
    private final ILogger logger;
    private final DatabaseEntitiesIntents database;
    private final TrainingLogic trainingLogic;

    @Inject
    public EntityLogic(final Config config, final ILogger logger, final DatabaseEntitiesIntents database,
                       final TrainingLogic trainingLogic) {
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.trainingLogic = trainingLogic;
    }

    public ApiResult getEntities(final String devid) {
        try {

            final List<String> entityList = this.database.getEntities(devid);
            if (entityList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetEntities", devid,
                        LogMap.map("Num Entities", "0"));
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetEntities", devid,
                    LogMap.map("Num Entities", entityList.size()));
            return new ApiEntityList(entityList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetEntities", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getEntity(final String devid, final String entityName) {
        try {
            final ApiEntity entity = this.database.getEntity(devid, entityName);
            this.logger.logUserTraceEvent(LOGFROM, "GetEntity", devid, LogMap.map("Entity", entityName));
            return entity.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetEntity", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeEntity(final String devid, final String entityName, final ApiEntity entity) {
        LogMap logMap = LogMap.map("Entity", entityName);
        try {

            stopTrainingIfEntityInUse(devid, entityName);
            this.database.writeEntity(devid, entityName, entity);
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity", devid, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt to rename existing name", devid, logMap);
            return ApiError.getBadRequest("entity name already in use");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteEntity", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteEntity(final String devid, final String entityName) {
        try {
            LogMap logMap = LogMap.map("Entity", entityName);
            if (!this.database.deleteEntity(devid, entityName)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - not found", devid, logMap);
                return ApiError.getNotFound();
            }
            stopTrainingIfEntityInUse(devid, entityName);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity", devid, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteEntity", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    private List<UUID> getAisWithEntityInUse(final String devid, final String entityName) {
        try {
            return this.database.getAisForEntity(devid, entityName);
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetAisWithEntityInUse", devid, e);
        }
        return new ArrayList<>();
    }

    private void stopTrainingIfEntityInUse(final String devid, final String entityName) {
        List<UUID> ais = getAisWithEntityInUse(devid, entityName);
        if (!ais.isEmpty()) {
            for (UUID ai : ais) {
                this.trainingLogic.stopTraining(devid, ai);
            }
        }
    }
}
