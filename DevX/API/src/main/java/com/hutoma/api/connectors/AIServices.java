package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.DevPlan;

import org.apache.commons.io.Charsets;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AIServices extends ServerConnector {

    private static final String LOGFROM = "aiservices";
    private static final String COMMAND_PARAM = "command";
    private static final String TRAINING_TIME_ALLOWED_PARAM = "training_time_allowed";

    @Inject
    public AIServices(final Database database, final ILogger logger, final JsonSerializer serializer, final Tools tools,
                      final Config config, final JerseyClient jerseyClient) {
        super(database, logger, serializer, tools, config, jerseyClient);
    }

    public void startTraining(final String devId, final UUID aiid) throws AiServicesException {
        DevPlan devPlan;
        try {
            devPlan = this.database.getDevPlan(devId);
        } catch (Database.DatabaseException ex) {
            throw new AiServicesException("Could not get plan for devId " + devId);
        }
        this.logger.logDebug(LOGFROM, "Issuing \"start training\" command to backends for AI " + aiid.toString());
        final int maxTrainingSecs = devPlan.getMaxTrainingSecs();
        HashMap<String, Callable<Response>> callables = getTrainingCallablesForCommand(devId, aiid,
                new HashMap<String, String>() {{
                    put(COMMAND_PARAM, "start");
                    put(TRAINING_TIME_ALLOWED_PARAM, Integer.toString(maxTrainingSecs));
                }});
        executeAndWait(callables);
    }

    public void stopTraining(final String devId, final UUID aiid) throws AiServicesException {
        HashMap<String, Callable<Response>> callables = getTrainingCallablesForCommand(devId, aiid, "stop");
        executeAndWait(callables);
    }

    public void deleteAI(final String devId, final UUID aiid) throws AiServicesException {
        HashMap<String, Callable<Response>> callables = new HashMap<>();
        this.logger.logDebug(LOGFROM, "Issuing \"delete AI\" command to backends for AI " + aiid.toString());
        for (String endpoint : this.getAllEndpoints()) {
            callables.put(endpoint, () -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
                    .request()
                    .delete());
        }
        executeAndWait(callables);
    }

    public void deleteDev(final String devId) throws AiServicesException {
        this.logger.logDebug(LOGFROM, "Issuing \"delete DEV\" command to backends for dev " + devId);
        HashMap<String, Callable<Response>> callables = new HashMap<>();
        for (String endpoint : this.getAllEndpoints()) {
            callables.put(endpoint, () -> this.jerseyClient
                    .target(endpoint).path(devId)
                    .request()
                    .delete());
        }
        executeAndWait(callables);
    }

    public void uploadTraining(final String devId, final UUID aiid, final String trainingMaterials)
            throws AiServicesException {
        HashMap<String, Callable<Response>> callables = new HashMap<>();
        this.logger.logDebug(LOGFROM, "Issuing \"upload training\" command to backends for AI " + aiid.toString());
        for (String endpoint : this.getAllEndpoints()) {
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
            callables.put(endpoint, () -> this.jerseyClient
                    .target(endpoint)
                    .request()
                    .post(Entity.entity(multipart, multipart.getMediaType())));
        }
        executeAndWait(callables);
    }

    private HashMap<String, Callable<Response>> getTrainingCallablesForCommand(final String devId, final UUID aiid,
                                                                               final String command) {
        return getTrainingCallablesForCommand(devId, aiid, new HashMap<String, String>() {{
            put(AIServices.COMMAND_PARAM, command);
        }});
    }

    private HashMap<String, Callable<Response>> getTrainingCallablesForCommand(final String devId, final UUID aiid,
                                                                               Map<String, String> params) {
        HashMap<String, Callable<Response>> callables = new HashMap<>();
        for (String endpoint : this.getAllEndpoints()) {

            JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId).path(aiid.toString());
            for (Map.Entry<String, String> param : params.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue());
            }

            final JerseyInvocation.Builder builder = target.request();
            callables.put(endpoint, () -> builder.post(null));
        }
        return callables;
    }

    private List<String> getAllEndpoints() {
        List<String> list = new ArrayList<>();
        String wnet = this.config.getWnetTrainingEndpoint();
        if (wnet != null) {
            list.add(wnet);
        }
        String rnn = this.config.getRnnTrainingEndpoint();
        if (rnn != null) {
            list.add(rnn);
        }
        return list;
    }

    public static class AiInfo {
        private final String ai_id;
        private final String dev_id;

        public AiInfo(final String devId, final UUID aiid) {
            this.ai_id = aiid.toString();
            this.dev_id = devId;
        }

        public String getAiid() {
            return this.ai_id;
        }

        public String getDevId() {
            return this.dev_id;
        }
    }
}
