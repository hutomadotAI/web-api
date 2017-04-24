package com.hutoma.api.connectors;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerBase.RequestFor;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.InvocationResult;
import com.hutoma.api.controllers.ServerMetadata;

import org.apache.commons.io.Charsets;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class AIServices extends ServerConnector {

    private static final String LOGFROM = "aiservices";
    private static final String COMMAND_PARAM = "command";
    private static final String TRAINING_TIME_ALLOWED_PARAM = "training_time_allowed";

    private final ControllerWnet controllerWnet;
    private final ControllerRnn controllerRnn;

    @Inject
    public AIServices(final Database database, final ILogger logger, final JsonSerializer serializer,
                      final Tools tools, final Config config, final JerseyClient jerseyClient,
                      final ThreadSubPool threadSubPool,
                      final ControllerWnet controllerWnet, final ControllerRnn controllerRnn) {
        super(database, logger, serializer, tools, config, jerseyClient, threadSubPool);
        this.controllerWnet = controllerWnet;
        this.controllerRnn = controllerRnn;
    }

    public void startTraining(final String devId, final UUID aiid) throws AiServicesException {
        DevPlan devPlan;
        try {
            devPlan = this.database.getDevPlan(devId);
        } catch (Database.DatabaseException ex) {
            throw new AiServicesException("Could not get plan for devId " + devId);
        }
        this.logger.logDebug(LOGFROM, "Issuing \"start training\" command to backends for AI " + aiid.toString());
        HashMap<String, Callable<InvocationResult>> callables = getTrainingCallablesForCommand(devId, aiid,
                new HashMap<String, String>() {{
                    put(COMMAND_PARAM, "start");
                    put(TRAINING_TIME_ALLOWED_PARAM, Integer.toString(devPlan.getMaxTrainingMins()));
                }});
        executeAndWait(callables);
    }

    public void stopTraining(final String devId, final UUID aiid) throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = getTrainingCallablesForCommand(devId, aiid, "stop");
        executeAndWait(callables);
    }

    public void deleteAI(final String devId, final UUID aiid) throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        this.logger.logDebug(LOGFROM, "Issuing \"delete AI\" command to backends for AI " + aiid.toString());
        for (String endpoint : this.getAllEndpoints(aiid)) {
            callables.put(endpoint, () -> new InvocationResult(
                    aiid,
                    this.jerseyClient
                            .target(endpoint).path(devId).path(aiid.toString())
                            .request()
                            .delete(),
                    endpoint,
                    0));
        }
        executeAndWait(callables);
    }

    public void deleteDev(final String devId) throws AiServicesException {
        this.logger.logDebug(LOGFROM, "Issuing \"delete DEV\" command to backends for dev " + devId);
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        for (String endpoint : this.getAllEndpoints(null)) {
            callables.put(endpoint, () -> new InvocationResult(
                    null,
                    this.jerseyClient
                            .target(endpoint).path(devId)
                            .request()
                            .delete(), endpoint, 0));
        }
        executeAndWait(callables);
    }

    public void uploadTraining(final String devId, final UUID aiid, final String trainingMaterials)
            throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        this.logger.logDebug(LOGFROM, "Issuing \"upload training\" command to backends for AI " + aiid.toString());
        for (String endpoint : this.getAllEndpoints(aiid)) {
            this.logger.logDebug(LOGFROM, "Sending training data to: " + endpoint);
            FormDataContentDisposition dispo = FormDataContentDisposition
                    .name("filename")
                    .fileName("training.txt")
                    .size(trainingMaterials.getBytes(Charsets.UTF_8).length)
                    .build();
            FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, trainingMaterials);
            AiInfo info = new AiInfo(devId, aiid);
            FormDataMultiPart multipart = (FormDataMultiPart) new FormDataMultiPart()
                    .field("info", this.serializer.serialize(info), MediaType.APPLICATION_JSON_TYPE)
                    .bodyPart(bodyPart);
            callables.put(endpoint, () -> new InvocationResult(
                    aiid,
                    this.jerseyClient
                            .target(endpoint)
                            .request()
                            .post(Entity.entity(multipart, multipart.getMediaType())),
                    endpoint,
                    0));
        }
        executeAndWait(callables);
    }

    public void stopTrainingIfNeeded(final String devId, final UUID aiid)
            throws Database.DatabaseException {
        try {
            ApiAi ai = this.database.getAI(devId, aiid, this.serializer);
            TrainingStatus status = ai.getSummaryAiStatus();
            if (status == TrainingStatus.AI_TRAINING || status == TrainingStatus.AI_TRAINING_QUEUED) {
                this.stopTraining(devId, aiid);
            }
        } catch (ServerConnector.AiServicesException ex) {
            this.logger.logWarning(LOGFROM, "Could not stop training for ai " + aiid);
        }
    }

    private HashMap<String, Callable<InvocationResult>> getTrainingCallablesForCommand(
            final String devId, final UUID aiid, final String command) throws AiServicesException {
        return getTrainingCallablesForCommand(devId, aiid, new HashMap<String, String>() {{
            put(AIServices.COMMAND_PARAM, command);
        }});
    }

    private HashMap<String, Callable<InvocationResult>> getTrainingCallablesForCommand(
            final String devId, final UUID aiid, Map<String, String> params) throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        for (String endpoint : this.getAllEndpoints(aiid)) {

            JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId).path(aiid.toString());
            for (Map.Entry<String, String> param : params.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue());
            }

            final JerseyInvocation.Builder builder = target.request();
            callables.put(endpoint, () -> new InvocationResult(aiid, builder.post(null), endpoint, 0));
        }
        return callables;
    }

    private List<String> getAllEndpoints(UUID aiid) throws AiServicesException {
        try {
            return Arrays.asList(
                    this.controllerWnet.getBackendEndpoint(aiid, RequestFor.Training).getServerUrl(),
                    this.controllerRnn.getBackendEndpoint(aiid, RequestFor.Training).getServerUrl());
        } catch (ServerMetadata.NoServerAvailable noServer) {
            throw new AiServicesException(noServer.getMessage());
        }
    }


    public static class AiInfo {
        @SerializedName("ai_id")
        private final String aiid;
        @SerializedName("dev_id")
        private final String devId;

        public AiInfo(final String devId, final UUID aiid) {
            this.aiid = aiid.toString();
            this.devId = devId;
        }

        public String getAiid() {
            return this.aiid;
        }

        public String getDevId() {
            return this.devId;
        }
    }
}
