package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.endpoints.AIServicesEndpoint;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.AIServicesLogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Collections;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestServiceAiServices extends ServiceTestBase {

    private static final String AI_SERVICES_SERVER_REG_PATH = "/aiservices/register";
    private static final String AI_SERVICES_SERVER_AFFINITY_PATH = "/aiservices/affinity";
    private static final String AI_SERVICES_STATUS_PATH = "/aiservices/" + AIID + "/status";

    @Test
    public void testUpdateStatus() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAIStatus(any(), any())).thenReturn(true);
        final Response response = sendStatusUpdateRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateStatus_dbDidNotUpdate() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAIStatus(any(), any())).thenReturn(false);
        final Response response = sendStatusUpdateRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testUpdateStatus_invalidStatus() throws Database.DatabaseException {
        String statusJson = getCommonAiStatusJson();
        statusJson = statusJson.replace(TrainingStatus.AI_READY_TO_TRAIN.value(), "NOT_A_REAL_STATUS");
        final Response response = sendStatusUpdateRequest(statusJson);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testUpdateStatus_newStatus() throws Database.DatabaseException {
        String statusJson = getCommonAiStatusJson();
        statusJson = statusJson.replace(TrainingStatus.AI_READY_TO_TRAIN.value(), TrainingStatus.AI_TRAINING_QUEUED.value());
        when(this.fakeDatabase.updateAIStatus(any(), any())).thenReturn(false);
        when(this.fakeDatabase.updateAIStatus(
                argThat(aiStatus -> ((AiStatus) aiStatus).getTrainingStatus() == TrainingStatus.AI_TRAINING_QUEUED),
                any())).thenReturn(true);

        final Response response = sendStatusUpdateRequest(statusJson);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerRegister() {
        ServerRegistration wnet = new ServerRegistration("wnet", "http://test:8000/server", 2, 2);
        wnet.addAI(AIID, TrainingStatus.AI_TRAINING_COMPLETE);
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerRegister_WrongServerType() {
        ServerRegistration wnet = new ServerRegistration("none", "http://test:8000/server", 2, 2);
        wnet.addAI(AIID, TrainingStatus.AI_TRAINING_COMPLETE);
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerAffinity() {
        when(this.fakeControllerWnet.updateAffinity(any(), any())).thenReturn(true);
        ServerAffinity affinity = new ServerAffinity(DEVID, Collections.singletonList(AIID));
        String json = this.serializeObject(affinity);
        final Response response = sendAffinityRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerAffinity_BadSession() {
        when(this.fakeControllerWnet.updateAffinity(any(), any())).thenReturn(false);
        when(this.fakeControllerRnn.updateAffinity(any(), any())).thenReturn(false);
        when(this.fakeControllerAiml.updateAffinity(any(), any())).thenReturn(false);
        ServerAffinity affinity = new ServerAffinity(DEVID, Collections.singletonList(AIID));
        String json = this.serializeObject(affinity);
        final Response response = sendAffinityRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    private Response sendStatusUpdateRequest(final String statusJson) {
        return sendRequest(AI_SERVICES_STATUS_PATH, statusJson);
    }

    private Response sendRegistrationRequest(final String registrationJson) {
        return sendRequest(AI_SERVICES_SERVER_REG_PATH, registrationJson);
    }

    private Response sendAffinityRequest(final String affinityJson) {
        return sendRequest(AI_SERVICES_SERVER_AFFINITY_PATH, affinityJson);
    }

    private Response sendRequest(final String path, final String statusJson) {
        return target(path)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(statusJson));
    }

    private String getCommonAiStatusJson() {
        return this.serializeObject(new AiStatus(DEVID.toString(), AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0));
    }

    protected Class<?> getClassUnderTest() {
        return AIServicesEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AILogic.class).to(AILogic.class);
        binder.bind(AIServices.class).to(AIServices.class);
        binder.bind(AIServicesLogic.class).to(AIServicesLogic.class);
        return binder;
    }
}
