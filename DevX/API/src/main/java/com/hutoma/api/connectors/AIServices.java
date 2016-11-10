package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiResult;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
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
import javax.inject.Singleton;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Created by pedrotei on 07/11/16.
 */
@Singleton
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
                    .put(null));
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
                    .size(trainingMaterials.getBytes(Charset.defaultCharset()).length)
                    .build();
            FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, trainingMaterials);
            FormDataMultiPart multipart = (FormDataMultiPart) new FormDataMultiPart()
                    .field("devid", devId)
                    .field("aiid", aiid.toString())
                    .bodyPart(bodyPart);
            callables.add(() -> this.jerseyClient
                    .target(endpoint).path(devId).path(aiid.toString())
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
                if (response.hasEntity()) {
                    response.bufferEntity();
                    ApiResult result = (ApiResult) response.getEntity();
                    //ApiResult result = (ApiResult) this.serializer.deserialize(content, ApiResult.class);
                    if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                        errors.add(result.getStatus().getInfo());
                        this.logger.logError(LOGFROM, String.format("Failure status (id=%d msg=%s) for %s",
                                result.getStatus().getCode(), result.getStatus().getInfo(),
                                this.tools.getCallerMethod()));
                    }
                } else {
                    throw new AiServicesException("response without an entity");
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

    public class AiServicesException extends Exception {
        public AiServicesException(String message) {
            super(message);
        }
    }
}
