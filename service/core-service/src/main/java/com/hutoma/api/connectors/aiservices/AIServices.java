package com.hutoma.api.connectors.aiservices;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.apache.commons.io.Charsets;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class AIServices extends ServerConnector {

    private static final String LOGFROM = "aiservices";
    private final AiServicesQueue queueServices;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final DatabaseAI databaseAi;
    private final WnetServicesConnector wnetServicesConnector;
    private final RnnServicesConnector rnnServicesConnector;

    @Inject
    public AIServices(final DatabaseAI databaseAi,
                      final DatabaseEntitiesIntents databaseEntitiesIntents, final ILogger logger,
                      final JsonSerializer serializer,
                      final Tools tools, final JerseyClient jerseyClient,
                      final TrackedThreadSubPool threadSubPool,
                      final AiServicesQueue queueServices,
                      final WnetServicesConnector wnetServicesConnector,
                      final RnnServicesConnector rnnServicesConnector) {
        super(logger, serializer, tools, jerseyClient, threadSubPool);
        this.queueServices = queueServices;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.databaseAi = databaseAi;
        this.wnetServicesConnector = wnetServicesConnector;
        this.rnnServicesConnector = rnnServicesConnector;
    }

    /***
     * Queue a command to start training
     * @param status
     * @param devId
     * @param aiid
     * @throws AiServicesException
     */
    public void startTraining(BackendStatus status, final UUID devId, final UUID aiid) throws AiServicesException {
        try {
            this.queueServices.userActionStartTraining(status, BackendServerType.WNET, devId, aiid);
            this.wnetServicesConnector.kickQueueProcessor();
            this.queueServices.userActionStartTraining(status, BackendServerType.RNN, devId, aiid);
            this.rnnServicesConnector.kickQueueProcessor();
        } catch (DatabaseException e) {
            AiServicesException.throwWithSuppressed("failed to start training", e);
        }
    }

    /***
     * Stop training (direct, not queued)
     * @param backendStatus
     * @param devId
     * @param aiid
     * @throws AiServicesException
     */
    public void stopTraining(final BackendStatus backendStatus, final UUID devId, final UUID aiid)
            throws AiServicesException {
        try {
            this.queueServices.userActionStopTraining(backendStatus, BackendServerType.WNET, this.wnetServicesConnector,
                    devId, aiid);
            this.queueServices.userActionStopTraining(backendStatus, BackendServerType.RNN, this.rnnServicesConnector,
                    devId, aiid);
        } catch (DatabaseException e) {
            AiServicesException.throwWithSuppressed("failed to stop training", e);
        }
    }

    /***
     * Delet an AI (stop training now and queue the delete for later)
     * @param backendStatus
     * @param devId
     * @param aiid
     * @throws AiServicesException
     */
    public void deleteAI(final BackendStatus backendStatus, final UUID devId, final UUID aiid)
            throws AiServicesException {
        try {
            this.queueServices.userActionDelete(backendStatus, BackendServerType.WNET, this.wnetServicesConnector,
                    devId, aiid);
            this.wnetServicesConnector.kickQueueProcessor();
            this.queueServices.userActionDelete(backendStatus, BackendServerType.RNN, this.rnnServicesConnector,
                    devId, aiid);
            this.rnnServicesConnector.kickQueueProcessor();
        } catch (DatabaseException e) {
            AiServicesException.throwWithSuppressed("failed to delete ai", e);
        }
    }

    public void deleteDev(final UUID devId) throws AiServicesException {
        this.logger.logDebug(LOGFROM, "Issuing \"delete DEV\" command to backends for dev " + devId);
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        for (String endpoint : this.getEndpointsForAllServerTypes()) {
            callables.put(endpoint, () -> new InvocationResult(
                    null,
                    this.jerseyClient
                            .target(endpoint).path(devId.toString())
                            .request()
                            .delete(), endpoint, 0));
        }
        executeAndWait(callables);
    }

    private List<String> getEndpointsForAllServerTypes() {
        List<String> list = new ArrayList<>();
        Optional<ServerTrackerInfo> info = this.wnetServicesConnector.getVerifiedEndpointMap().values().stream()
                .findFirst();
        info.ifPresent(serverTrackerInfo -> list.add(serverTrackerInfo.getServerUrl()));
        info = this.rnnServicesConnector.getVerifiedEndpointMap().values().stream()
                .findFirst();
        info.ifPresent(serverTrackerInfo -> list.add(serverTrackerInfo.getServerUrl()));

        return list;
    }

    /***
     * Upload training materials for an AI
     * @param backendStatus
     * @param devId
     * @param aiid
     * @param trainingMaterials
     * @throws AiServicesException
     */
    public void uploadTraining(final BackendStatus backendStatus, final UUID devId, final UUID aiid,
                               final String trainingMaterials)
            throws AiServicesException {

        // for each type of server, send a stop command (if needed),
        // set the status and the queue state
        try {
            this.queueServices.userActionUpload(backendStatus, BackendServerType.WNET, this.wnetServicesConnector,
                    devId, aiid);
            this.queueServices.userActionUpload(backendStatus, BackendServerType.RNN, this.rnnServicesConnector,
                    devId, aiid);
        } catch (DatabaseException e) {
            AiServicesException.throwWithSuppressed("failed to upload training materials", e);
        }

        // upload the file to each backend server (one of each kind)
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        this.logger.logDebug(LOGFROM, "Issuing \"upload training\" command to backends for AI " + aiid.toString());
        for (String endpoint : this.getListOfPrimaryEndpoints(aiid)) {

            // HACK! HACK!- temporarily pro-process the training file for RNN so that
            // it doesn't contain any intent-generated lines
            // Bug:2300
            String trainingToSend = trainingMaterials;
            try {
                if (endpoint.equals(this.rnnServicesConnector.getBackendEndpoint(
                        aiid, RequestFor.Training).getServerUrl())) {
                    trainingToSend = removeIntentExpressions(trainingMaterials);
                }
            } catch (NoServerAvailableException ex) {
                // Shouldn't happen as this would first fail on getListOfPrimaryEndpoints()
            }

            this.logger.logDebug(LOGFROM, "Sending training data to: " + endpoint);
            FormDataContentDisposition dispo = FormDataContentDisposition
                    .name("filename")
                    .fileName("training.txt")
                    .size(trainingToSend.getBytes(Charsets.UTF_8).length)
                    .build();
            FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, trainingToSend);
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

    public String getTrainingMaterialsCommon(final UUID devid, final UUID aiid, final JsonSerializer jsonSerializer)
            throws DatabaseException {
        final String EOL = "\n";
        final String devidString = devid.toString();
        StringBuilder sb = new StringBuilder();
        ApiAi ai = this.databaseAi.getAI(devid, aiid, jsonSerializer);
        if (ai == null) {
            this.logger.logUserTraceEvent(LOGFROM, "GetTrainingMaterialsCommon - AI not found", devidString,
                    LogMap.map("AIID", aiid));
            return null;
        }
        String userTrainingFile = this.databaseAi.getAiTrainingFile(aiid);
        if (userTrainingFile != null && !userTrainingFile.isEmpty()) {
            sb.append(userTrainingFile);
        }
        for (String intentName : this.databaseEntitiesIntents.getIntents(devid, aiid)) {
            ApiIntent intent = this.databaseEntitiesIntents.getIntent(aiid, intentName);
            for (String userSays : intent.getUserSays()) {
                if (sb.length() > 0) {
                    sb.append(EOL);
                }
                sb.append(userSays).append(EOL);
                sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentName).append(EOL);
            }
        }
        return sb.toString();
    }

    /**
     * HACK! HACK!
     * Removes the intent expressions from a given training text
     * @param text the training text
     * @return the text with the intent expressions stripped out
     * <p>
     * Note - public so it can be unit tested
     * Bug:2300
     */
    public String removeIntentExpressions(final String text) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(text);
        String eol = System.getProperty("line.separator");
        while (scanner.hasNextLine()) {
            String line1 = scanner.nextLine();
            if (line1.isEmpty()) {
                sb.append(eol);
                continue;
            }
            if (scanner.hasNextLine()) {
                String line2 = scanner.nextLine();
                if (!line2.startsWith(MemoryIntentHandler.META_INTENT_TAG)) {
                    sb.append(line1).append(eol).append(line2).append(eol);
                }
            } else {
                sb.append(line1).append(eol);
            }
        }
        scanner.close();
        return sb.toString().trim();
    }

    /***
     * Stop the training if the AI was likely to have been training
     * @param devId
     * @param aiid
     * @throws DatabaseException
     */
    public void stopTrainingIfNeeded(final UUID devId, final UUID aiid)
            throws DatabaseException {
        try {
            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.serializer);
            if (ai != null) {
                stopTraining(ai.getBackendStatus(), devId, aiid);
            }
        } catch (ServerConnector.AiServicesException ex) {
            this.logger.logWarning(LOGFROM, "Could not stop training for ai " + aiid);
        }
    }

    /***
     * Get a list containing the primary endpoints (i.e. one of each backend type)
     * @param aiid
     * @return
     * @throws AiServicesException
     */
    private List<String> getListOfPrimaryEndpoints(final UUID aiid) throws AiServicesException {
        try {
            return Arrays.asList(
                    this.wnetServicesConnector.getBackendEndpoint(aiid, RequestFor.Training).getServerUrl(),
                    this.rnnServicesConnector.getBackendEndpoint(aiid, RequestFor.Training).getServerUrl());
        } catch (NoServerAvailableException noServer) {
            AiServicesException.throwWithSuppressed(noServer.getMessage(), noServer);
        }
        return new ArrayList<>(); // this will never happen because the throwsWithSuppressed always throws
    }

    public static class AiInfo {
        @SerializedName("ai_id")
        private final String aiid;
        @SerializedName("dev_id")
        private final String devId;

        public AiInfo(final UUID devId, final UUID aiid) {
            this.aiid = aiid.toString();
            this.devId = devId.toString();
        }

        public String getAiid() {
            return this.aiid;
        }

        public String getDevId() {
            return this.devId;
        }
    }
}