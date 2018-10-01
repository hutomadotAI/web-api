package com.hutoma.api.controllers;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.thread.ITrackedThreadSubPool;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestAIQueueServices {
    private static final UUID DEVID = UUID.fromString("1a5c55e7-6492-4d08-8dfd-d167ac9f3330");
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final DevPlan DEVPLAN = new DevPlan(10, 1000, 5000, 120);

    private static final ServiceIdentity SERVICE_IDENTITY = new ServiceIdentity(
            BackendServerType.EMB, SupportedLanguage.EN, "default");
    private static final AiIdentity AI_IDENTITY = new AiIdentity(DEVID, AIID);
    private static final java.lang.String SERVERID = "serverid";
    private static final java.lang.String SERVERURL = "serverurl";

    AIQueueServices aiQueueServices;

    DatabaseAiStatusUpdates fakeDatabase;
    IConnectConfig fakeConfig;
    AiServiceStatusLogger fakeLogger;
    private Tools fakeTools;
    JsonSerializer jsonSerializer;
    JerseyClient fakeJerseyClient;
    private JerseyInvocation.Builder fakeJerseyBuilder;
    private ITrackedThreadSubPool fakeTrackedThreadSubPool;

    @Before
    public void setUp() {
        this.fakeConfig = mock(ControllerConfig.class);

        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeLogger = mock(AiServiceStatusLogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeJerseyClient = mock(JerseyClient.class);
        this.fakeJerseyBuilder = TestDataHelper.mockJerseyClient(this.fakeJerseyClient);
        this.jsonSerializer = new JsonSerializer();


        this.fakeTrackedThreadSubPool = mock(ITrackedThreadSubPool.class);

        this.aiQueueServices = new AIQueueServices(this.fakeDatabase, this.fakeLogger,
                fakeConfig, jsonSerializer, fakeTools, fakeJerseyClient, fakeTrackedThreadSubPool);
    }

    @Test
    public void testStartTraining_ok() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        Response resp = new ApiResult().setSuccessStatus("ok").getResponse(this.jsonSerializer).build();
        doReturn(CompletableFuture.completedFuture(
                new InvocationResult(resp, "endpoint", 100,
                        100, 1, AIID)))
                .when(this.fakeTrackedThreadSubPool).submit(any(Callable.class));
        this.aiQueueServices.startTrainingDirect(SERVICE_IDENTITY, AI_IDENTITY, SERVERURL, SERVERID);
    }

    @Test
    public void testStartTraining_null_dev_plan() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(null);
        Response resp = new ApiResult().setSuccessStatus("ok").getResponse(this.jsonSerializer).build();
        doReturn(CompletableFuture.completedFuture(
                new InvocationResult(resp, "endpoint", 100,
                        100, 1, AIID)))
                .when(this.fakeTrackedThreadSubPool).submit(any(Callable.class));
        ServerConnector.AiServicesException exception = assertThrows(ServerConnector.AiServicesException.class, () -> {
            this.aiQueueServices.startTrainingDirect(SERVICE_IDENTITY, AI_IDENTITY, SERVERURL, SERVERID);
        });
        assertEquals(true, exception.getPermanentError());
    }

    @Test
    public void testStartTraining_error_dev_plan() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenThrow(new DatabaseException("DB error"));
        Response resp = new ApiResult().setSuccessStatus("ok").getResponse(this.jsonSerializer).build();
        doReturn(CompletableFuture.completedFuture(
                new InvocationResult(resp, "endpoint", 100,
                        100, 1, AIID)))
                .when(this.fakeTrackedThreadSubPool).submit(any(Callable.class));
        ServerConnector.AiServicesException exception = assertThrows(ServerConnector.AiServicesException.class, () -> {
            this.aiQueueServices.startTrainingDirect(SERVICE_IDENTITY, AI_IDENTITY, SERVERURL, SERVERID);
        });
        assertEquals(false, exception.getPermanentError());
    }
}
