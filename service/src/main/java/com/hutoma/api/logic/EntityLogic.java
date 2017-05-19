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
import com.hutoma.api.memory.IEntityRecognizer;

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

    public ApiResult getEntities(final UUID devid) {
        final String devidString = devid.toString();
        try {
            final List<String> entityList = this.database.getEntities(devid);
            if (entityList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetEntities", devidString,
                        LogMap.map("Num Entities", "0"));
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetEntities", devidString,
                    LogMap.map("Num Entities", entityList.size()));
            return new ApiEntityList(entityList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetEntities", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getEntity(final UUID devid, final String entityName) {
        final String devidString = devid.toString();
        try {
            final ApiEntity entity = this.database.getEntity(devid, entityName);
            this.logger.logUserTraceEvent(LOGFROM, "GetEntity", devidString, LogMap.map("Entity", entityName));
            return entity.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeEntity(final UUID devid, final String entityName, final ApiEntity entity) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("Entity", entityName);
        try {
            if (entityName.startsWith(IEntityRecognizer.SYSTEM_ENTITY_PREFIX)) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt name an entity with system prefix",
                        devidString, logMap);
                return ApiError.getBadRequest("Cannot create an entity with a system prefix.");
            }
            stopTrainingIfEntityInUse(devid, entityName);
            this.database.writeEntity(devid, entityName, entity);
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt to rename existing name",
                    devidString, logMap);
            return ApiError.getBadRequest("entity name already in use");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteEntity(final UUID devid, final String entityName) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("Entity", entityName);
            if (!this.database.deleteEntity(devid, entityName)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            stopTrainingIfEntityInUse(devid, entityName);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    private List<UUID> getAisWithEntityInUse(final UUID devid, final String entityName) {
        try {
            return this.database.getAisForEntity(devid, entityName);
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetAisWithEntityInUse", devid.toString(), e);
        }
        return new ArrayList<>();
    }

    private void stopTrainingIfEntityInUse(final UUID devid, final String entityName) {
        List<UUID> ais = getAisWithEntityInUse(devid, entityName);
        if (!ais.isEmpty()) {
            for (UUID ai : ais) {
                this.trainingLogic.stopTraining(devid, ai);
            }
        }
    }
}
