package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.IEntityRecognizer;

import javax.inject.Inject;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

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
            List<Entity> entityList = this.database.getEntities(devid);
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
        LogMap logMap = LogMap.map("Entity", entityName);
        try {
            final ApiEntity entity = this.database.getEntity(devid, entityName);
            if (entity == null) {
                this.logger.logUserWarnEvent(LOGFROM, "GetEntity - not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetEntity", devidString, logMap);
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
            final boolean created = this.database.getEntity(devid, entityName) == null;
            this.database.writeEntity(devid, entityName, entity);
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity", devidString, logMap);
            if (created) {
                return new ApiResult().setCreatedStatus("Entity created.");
            } else {
                return new ApiResult().setSuccessStatus("Entity updated.");
            }
        } catch (DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt to rename existing name",
                    devidString, logMap);
            return ApiError.getBadRequest("Entity name already in use.");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult replaceEntity(final UUID devid, final String entityName, final ApiEntity entity) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("Entity", entityName);
        try {
            if (entityName.startsWith(IEntityRecognizer.SYSTEM_ENTITY_PREFIX)) {
                this.logger.logUserTraceEvent(LOGFROM, "ReplaceEntity - attempted to replace a system entity.",
                        devidString, logMap);
                return ApiError.getBadRequest("Cannot replace a system entity.");
            }
            final boolean exists = this.database.getEntity(devid, entityName) != null;
            if (!exists) {
                return ApiError.getBadRequest("Entity doesn't exist.");
            }

            // Replace entity values.
            this.database.writeEntity(devid, entityName, entity);
            this.logger.logUserTraceEvent(LOGFROM, "ReplaceEntity", devidString, logMap);
            return new ApiResult().setSuccessStatus("Entity updated.");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "ReplaceEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteEntity(final UUID devid, final String entityName) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("Entity", entityName);
            OptionalInt entityId = this.database.getEntityIdForDev(devid, entityName);
            if (!entityId.isPresent()) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - not found", devidString, logMap);
                return ApiError.getNotFound();
            }

            List<UUID> referreingAis = this.database.getAisForEntity(devid, entityName);
            if (!referreingAis.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - in use, not deleted", devidString, logMap);
                return ApiError.getConflict(String.format("Entity is in use by %d bot%s", referreingAis.size(),
                        referreingAis.size() != 1 ? "s" : ""));
            }

            if (!this.database.deleteEntity(devid, entityId.getAsInt())) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - could not delete", devidString, logMap);
                return ApiError.getInternalServerError();
            }
            this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteEntity", devidString, e);
            return ApiError.getInternalServerError();
        }
    }
}
