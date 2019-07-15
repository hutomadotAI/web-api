package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.EntityValueType;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.logic.chat.EntityRecognizerMessage;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by David MG on 05/10/2016.
 */
public class EntityLogic {

    private static final String LOGFROM = "entitylogic";
    private final Config config;
    private final ILogger logger;
    private final DatabaseEntitiesIntents database;
    private final TrainingLogic trainingLogic;
    private final EntityRecognizerService entityRecognizerService;
    private final FeatureToggler featureToggler;
    private final JsonSerializer jsonSerializer;

    @Inject
    public EntityLogic(final Config config,
                       final ILogger logger,
                       final DatabaseEntitiesIntents database,
                       final TrainingLogic trainingLogic,
                       final EntityRecognizerService entityRecognizerService,
                       final JsonSerializer jsonSerializer,
                       final FeatureToggler featureToggler) {
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.trainingLogic = trainingLogic;
        this.entityRecognizerService = entityRecognizerService;
        this.jsonSerializer = jsonSerializer;
        this.featureToggler = featureToggler;
    }

    public ApiResult getEntities(final UUID devid, final UUID aiid) {
        final String devidString = devid.toString();
        try {
            List<Entity> entityList = this.database.getEntities(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "GetEntities", devidString,
                    LogMap.map("Num Entities", entityList.size()));
            return new ApiEntityList(entityList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetEntities", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getEntity(final UUID devid, final String entityName, final UUID aiid) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("Entity", entityName);
        try {
            final ApiEntity entity = this.database.getEntity(devid, entityName, aiid);
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

    public ApiResult writeEntity(final UUID devid, final String entityName, final ApiEntity entity, final UUID aiid) {
        final String devidString = devid.toString();
        LogMap logMap = LogMap.map("Entity", entityName);
        try {
            if (entityName.startsWith(IEntityRecognizer.SYSTEM_ENTITY_PREFIX)) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt name an entity with system prefix",
                        devidString, logMap);
                return ApiError.getBadRequest("Cannot create an entity with a system prefix.");
            }

            if (entity.isSystem()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - attempt create a custom entity as system",
                        devidString, logMap);
                return ApiError.getBadRequest("Cannot create system entities.");
            }

            if (entity.getEntityValueList() != null
                    && entity.getEntityValueList().size() > this.config.getMaxEntityValuesPerEntity()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - exceeded max entity values per entity",
                        devidString, logMap.put("Max", this.config.getMaxTotalEntityValues())
                                .put("ValuesOnEntity", entity.getEntityValueList().size()));
                return ApiError.getBadRequest(String.format(
                        "Exceeds maximum number of values per entity - Max: %d, this entity: %d",
                        this.config.getMaxEntityValuesPerEntity(), entity.getEntityValueList().size()));
            }

            int expectedTotalEntityValuesCount =
                    this.database.getEntityValuesCountForDevExcludingEntity(devid, entityName, aiid)
                            + (entity.getEntityValueList() == null ? 0 : entity.getEntityValueList().size());

            if (expectedTotalEntityValuesCount > this.config.getMaxTotalEntityValues()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteEntity - exceeded max entity values per dev",
                        devidString, logMap.put("Max", this.config.getMaxEntityValuesPerEntity())
                                .put("ValuesOnEntity", expectedTotalEntityValuesCount));
                return ApiError.getBadRequest(String.format(
                        "Exceeds maximum number of values per account - Max: %d, with this entity: %d",
                        this.config.getMaxTotalEntityValues(), expectedTotalEntityValuesCount));
            }

            // Finally, validate the regex if this is a regex entity
            if (entity.getEntityValueType() == EntityValueType.REGEX) {
                EntityRecognizerMessage testMessage = new EntityRecognizerMessage();
                testMessage.setConversation("test conversation");
                testMessage.getRegexEntities().put(entity.getEntityName(), entity.getEntityValueList().get(0));

                // see if this can be executed
                try {
                    entityRecognizerService.findEntities(jsonSerializer.serialize(testMessage),
                            SupportedLanguage.EN);
                } catch (EntityRecognizerService.EntityRecognizerException ex) {
                    if (ex.getReason()
                            == EntityRecognizerService.EntityRecognizerException.EntityRecognizerError.INVALID_REGEX) {
                        logger.logUserTraceEvent(LOGFROM, "Test run of regex entity failed",
                                devidString);
                        return ApiError.getBadRequest("Invalid regex string found in entity");
                    }
                    // Something else unexpected
                    logger.logUserExceptionEvent(LOGFROM,
                            "Test run of regex entity failed - unknown reason",
                            devidString, ex);
                    return ApiError.getInternalServerError();
                } catch (Exception ex) {
                    // In this case its a system error
                    logger.logUserExceptionEvent(LOGFROM, "Test run of regex entity failed",
                            devidString, ex);
                    return ApiError.getInternalServerError();
                }
            }

            final boolean created = this.database.getEntity(devid, entityName, aiid) == null;
            this.database.writeEntity(devid, entityName, entity, aiid);
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

    public ApiResult deleteEntity(final UUID devid, final String entityName, final UUID aiid) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("Entity", entityName);

            if (this.database.getEntity(devid, entityName, aiid) == null) {
                return ApiError.getNotFound();
            }

            // Check if there are any AIs that use this entity
            Set<String> referreingAis = new HashSet<>();
            List<ApiIntent> allIntents = this.database.getAllIntents(devid);
            for (ApiIntent intent : allIntents) {
                if (!intent.getAiid().equals(aiid)) {
                    continue;
                }
                if (intent.getVariables() != null && !intent.getVariables().isEmpty()) {
                    for (IntentVariable variable : intent.getVariables()) {
                        if (variable.getEntityName().equals(entityName)) {
                            referreingAis.add(intent.getAiid().toString());
                        }
                    }
                }
            }

            if (!referreingAis.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteEntity - in use, not deleted", devidString, logMap);
                return ApiError.getConflict(String.format("Entity is in use by %d bot%s", referreingAis.size(),
                        referreingAis.size() != 1 ? "s" : ""));
            }

            if (!this.database.deleteEntityByName(devid, aiid, entityName, null)) {
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
