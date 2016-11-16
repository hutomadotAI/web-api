package com.hutoma.api.connectors;

import com.google.common.base.Charsets;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by pedrotei on 07/11/16.
 */
public class AIServices {

    private static final String LOGFROM = "aiservices";
    private static final String COMMAND_PARAM = "command";

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final JsonSerializer serializer;
    private final ILogger logger;
    private final Tools tools;
    private final Database database;
    private final Config config;
    private final JerseyClient jerseyClient;

    @Inject
    public AIServices(final Database database, final ILogger logger, final JsonSerializer serializer,
                      final Tools tools, final Config config, final JerseyClient jerseyClient) {
        this.database = database;
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
        this.config = config;
        this.jerseyClient = jerseyClient;
        this.jerseyClient.register(MultiPartWriter.class);
    }

    public void startTraining(final String devId, final UUID aiid) throws AiServicesException {
        List<Callable<Response>> callables = getTrainingCallablesForCommand(devId, aiid, "start");
        executeAndWait(callables);
    }

    public void stopTraining(final String devId, final UUID aiid) throws AiServicesException {
        List<Callable<Response>> callables = getTrainingCallablesForCommand(devId, aiid, "stop");
        executeAndWait(callables);
    }

    public void wakeNeuralNet(final String devId, final UUID aiid) throws AiServicesException {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.config.getGpuTrainingEndpoints()) {
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
                    .queryParam(COMMAND_PARAM, "wake")
                    .request()
                    .post(null));
        }
        executeAndWait(callables);
    }

    public void deleteAI(final String devId, final UUID aiid) throws AiServicesException {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.getAllEndpoints()) {
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
                    .request()
                    .delete());
        }
        executeAndWait(callables);
    }

    public void deleteDev(final String devId) throws AiServicesException {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.getAllEndpoints()) {
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId)
                    .request()
                    .delete());
        }
        executeAndWait(callables);
    }

    public void updateTraining(final String devId, final UUID aiid) throws AiServicesException {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.getAllEndpoints()) {
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
                    .request()
                    .put(Entity.json("")));
        }
        executeAndWait(callables);
    }

    public void uploadTraining(final String devId, final UUID aiid, final String trainingMaterials)
            throws AiServicesException {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.getAllEndpoints()) {
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
            callables.add(() -> this.jerseyClient
                    .target(endpoint)
                    .request()
                    .post(Entity.entity(multipart, multipart.getMediaType())));
        }
        executeAndWait(callables);
    }

    private List<Callable<Response>> getTrainingCallablesForCommand(final String devId, final UUID aiid,
                                                                    final String command) {
        List<Callable<Response>> callables = new ArrayList<>();
        for (String endpoint : this.getAllEndpoints()) {
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
                    .queryParam(COMMAND_PARAM, command)
                    .request()
                    .post(null));
        }
        return callables;
    }

    private List<String> getAllEndpoints() {
        List<String> list = new ArrayList<>(this.config.getWnetTrainingEndpoints());
        list.addAll(this.config.getGpuTrainingEndpoints());
        return list;
    }

    private void executeAndWait(final List<Callable<Response>> callables) throws AiServicesException {
        try {
            this.logger.logDebug(LOGFROM, String.format("Issuing %d requests for %s",
                    callables.size(), this.tools.getCallerMethod()));
            List<Future<Response>> futures = this.executor.invokeAll(callables);
            List<String> errors = new ArrayList<>();
            for (Future<Response> future : futures) {
                Response response = future.get();
                if (response.getStatusInfo().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    errors.add(String.format("%d %s", response.getStatusInfo().getStatusCode(),
                            response.getStatusInfo().getReasonPhrase()));
                    this.logger.logError(LOGFROM, String.format("Failure status (id=%d msg=%s)",
                            response.getStatusInfo().getStatusCode(),
                            response.getStatusInfo().getReasonPhrase()));
                }
            }
            if (!errors.isEmpty()) {
                throw new AiServicesException(errors.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(";")));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new AiServicesException(e.getMessage());
        }
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

    public class AiServicesException extends Exception {
        public AiServicesException(String message) {
            super(message);
        }
    }
}
