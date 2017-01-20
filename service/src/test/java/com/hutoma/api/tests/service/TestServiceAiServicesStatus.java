package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.endpoints.AIServicesStatusEndpoint;
import com.hutoma.api.logic.AILogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestServiceAiServicesStatus extends ServiceTestBase {

    private static final String AI_SERVICES_PATH = "/aiservices/" + AIID + "/status";

    @Test
    public void testUpdateStatus() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAIStatus(any(), any())).thenReturn(true);
        final Response response = sendRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateStatus_dbDidNotUpdate() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAIStatus(any(), any())).thenReturn(false);
        final Response response = sendRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testUpdateStatus_invalidStatus() throws Database.DatabaseException {
        String statusJson = getCommonAiStatusJson();
        statusJson = statusJson.replace(TrainingStatus.AI_READY_TO_TRAIN.value(), "NOT_A_REAL_STATUS");
        final Response response = sendRequest(statusJson);
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

        final Response response = sendRequest(statusJson);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    private Response sendRequest(final String statusJson) {
        return target(AI_SERVICES_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(statusJson));
    }

    private String getCommonAiStatusJson() {
        return this.serializeObject(new AiStatus(DEVID.toString(), AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0));
    }

    protected Class<?> getClassUnderTest() {
        return AIServicesStatusEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AILogic.class).to(AILogic.class);
        binder.bind(AIServices.class).to(AIServices.class);
        return binder;
    }
}
