package com.hutoma.api.tests.service;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.endpoints.AIServicesEndpoint;
import com.hutoma.api.logic.AIServicesLogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.Collections;
import javax.inject.Singleton;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestServiceAiServices extends ServiceTestBase {

    private static final String AI_SERVICES_SERVER_REG_PATH = "/aiservices/register";
    private static final String AI_SERVICES_SERVER_AFFINITY_PATH = "/aiservices/affinity";
    private static final String AI_SERVICES_STATUS_PATH = "/aiservices/" + ServiceTestBase.AIID + "/status";

    @Mock
    private ControllerConfig fakeControllerConfig;

    @Test
    public void testUpdateStatus() throws DatabaseException {
        when(this.fakeDatabaseStatusUpdates.getAiQueueStatus(any(), any())).thenReturn(
                new BackendEngineStatus(TrainingStatus.AI_TRAINING_QUEUED, 0.0, 0.0));
        when(this.fakeDatabaseStatusUpdates.updateAIStatus(any())).thenReturn(true);
        final Response response = sendStatusUpdateRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateStatus_dbnotfound() throws DatabaseException {
        when(this.fakeDatabaseStatusUpdates.getAiQueueStatus(any(), any())).thenReturn(null);
        final Response response = sendStatusUpdateRequest(getCommonAiStatusJson());
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testUpdateStatus_invalidStatus() {
        String statusJson = getCommonAiStatusJson();
        statusJson = statusJson.replace(TrainingStatus.AI_READY_TO_TRAIN.value(), "NOT_A_REAL_STATUS");
        final Response response = sendStatusUpdateRequest(statusJson);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testUpdateStatus_newStatus() throws DatabaseException {
        String statusJson = getCommonAiStatusJson();
        statusJson = statusJson.replace(TrainingStatus.AI_READY_TO_TRAIN.value(), TrainingStatus.AI_TRAINING_STOPPED.value());
        when(this.fakeDatabaseStatusUpdates.getAiQueueStatus(any(), any())).thenReturn(
                new BackendEngineStatus(TrainingStatus.AI_TRAINING_QUEUED, 0.0, 0.0));
        when(this.fakeDatabaseStatusUpdates.updateAIStatus(any())).thenReturn(false);
        when(this.fakeDatabaseStatusUpdates.updateAIStatus(
                argThat(aiStatus -> ((AiStatus) aiStatus).getTrainingStatus() == TrainingStatus.AI_TRAINING_STOPPED)
        )).thenReturn(true);

        final Response response = sendStatusUpdateRequest(statusJson);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerRegister() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerRegister_nullAIID() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(null, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerRegister_badAIID() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet).replace(ServiceTestBase.AIID.toString(), "be108d4e-9aed-4876-bxdc1-cfbf9ba1146a");
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerRegister_nullStatus() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, null, "hash");
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerRegister_badStatus() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet).replace(TrainingStatus.AI_TRAINING_COMPLETE.value(), "wrong_string");
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerRegister_WrongServerType() {
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET, "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet).replace("wnet", "other");
        final Response response = sendRegistrationRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testServerAffinity() {
        when(this.fakeControllerWnet.updateAffinity(any(), any())).thenReturn(true);
        ServerAffinity affinity = new ServerAffinity(ServiceTestBase.DEVID, Collections.singletonList(ServiceTestBase.AIID));
        String json = this.serializeObject(affinity);
        final Response response = sendAffinityRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testServerAffinity_BadSession() {
        when(this.fakeControllerWnet.updateAffinity(any(), any())).thenReturn(false);
        when(this.fakeControllerAiml.updateAffinity(any(), any())).thenReturn(false);
        when(this.fakeControllerSvm.updateAffinity(any(), any())).thenReturn(false);
        ServerAffinity affinity = new ServerAffinity(ServiceTestBase.DEVID, Collections.singletonList(ServiceTestBase.AIID));
        String json = this.serializeObject(affinity);
        final Response response = sendAffinityRequest(json);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testStatusUpdate_HashCode_Wnet() throws DatabaseException {
        when(this.fakeDatabaseStatusUpdates.updateAIStatus(any())).thenReturn(true);
        when(this.fakeDatabaseStatusUpdates.getAiQueueStatus(any(), any())).thenReturn(
                new BackendEngineStatus(TrainingStatus.AI_TRAINING_QUEUED, 0.0, 0.0));
        String statusJson = this.serializeObject(new AiStatus(ServiceTestBase.DEVID.toString(), ServiceTestBase.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, BackendServerType.WNET,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID));
        final Response response = sendStatusUpdateRequest(statusJson);
        verify(this.fakeControllerWnet, times(1)).setHashCodeFor(ServiceTestBase.AIID, "hash");
        verify(this.fakeControllerAiml, never()).setHashCodeFor(ServiceTestBase.AIID, "hash");
    }

    @Test
    public void testServerRegister_Hashcodes() {
        when(this.fakeControllerWnet.isPrimaryMaster(any())).thenReturn(true);
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET,
                "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        verify(this.fakeControllerWnet, times(1)).setAllHashCodes(any());
        verify(this.fakeControllerAiml, never()).setAllHashCodes(any());
    }

    @Test
    public void testServerRegister_Hashcodes_IgnoreNonMaster() {
        when(this.fakeControllerWnet.isPrimaryMaster(any())).thenReturn(false);
        ServerRegistration wnet = new ServerRegistration(BackendServerType.WNET,
                "http://test:8000/server", 2, 2);
        wnet.addAI(ServiceTestBase.AIID, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        String json = this.serializeObject(wnet);
        final Response response = sendRegistrationRequest(json);
        verify(this.fakeControllerWnet, never()).setAllHashCodes(any());
        verify(this.fakeControllerAiml, never()).setAllHashCodes(any());
    }

    @Test
    public void testServerRegister_noBody() {
        final Response response = sendRegistrationRequest(null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testStatusUpdate_noBody() {
        final Response response = sendStatusUpdateRequest(null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testUpdateAffinityRequest_noBody() {
        final Response response = sendAffinityRequest(null);
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
                .post(Entity.json(statusJson));
    }

    private String getCommonAiStatusJson() {
        return this.serializeObject(new AiStatus(ServiceTestBase.DEVID.toString(), ServiceTestBase.AIID, TrainingStatus.AI_READY_TO_TRAIN, ServiceTestBase.AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID));
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return AIServicesEndpoint.class;
    }

    @Override
    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeControllerConfig = mock(ControllerConfig.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceAiServices.this.fakeControllerConfig)).to(ControllerConfig.class).in(Singleton.class);

        binder.bind(AIServicesLogic.class).to(AIServicesLogic.class);
        return binder;
    }
}
