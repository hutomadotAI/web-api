package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.ThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 08/02/2017.
 */
public class TestControllerSvm {

    private ControllerConfig config;
    private Tools tools;
    private ControllerSvm test;
    private ThreadSubPool fakeThreadSubPool;

    @Before
    public void setUp() {
        this.tools = mock(Tools.class);
        this.config = mock(ControllerConfig.class);
        this.fakeThreadSubPool = mock(ThreadSubPool.class);

        when(this.tools.createNewRandomUUID()).thenReturn(UUID.randomUUID());

        this.test = new ControllerSvm(TestControllerSvm.this.config, this.fakeThreadSubPool, null,
                mock(AiServiceStatusLogger.class), mock(QueueProcessor.class)) {
            @Override
            protected ServerTracker createNewServerTracker() {
                return new FakeServerTracker(TestControllerSvm.this.config, TestControllerSvm.this.tools, this.logger);
            }
        };
    }

    @Test(expected = NoServerAvailableException.class)
    public void testControllerSvm_noServer() throws NoServerAvailableException {
        this.test.getServerForChat(UUID.randomUUID(), Collections.emptySet());
    }

    @Test
    public void testControllerSvm_oneServer() throws NoServerAvailableException {
        UUID ai1 = UUID.randomUUID();
        UUID sid = registerServer(ai1);
        Assert.assertEquals(sid, this.test.getServerForChat(ai1, Collections.emptySet()).getSessionID());
    }

    @Test
    public void testControllerSvm_twoSVMServers() throws NoServerAvailableException {
        UUID ai1 = UUID.randomUUID();
        UUID ai2 = UUID.randomUUID();
        UUID sid1 = registerServer(ai1);
        UUID sid2 = registerServer(ai2);
        Assert.assertEquals(sid1, this.test.getServerForChat(ai1, Collections.emptySet()).getSessionID());
        Assert.assertEquals(sid2, this.test.getServerForChat(ai2, Collections.emptySet()).getSessionID());
    }

    private UUID registerServer(UUID aiid) {
        ServerRegistration sr = new ServerRegistration(
                BackendServerType.SVM, "url1", 1, 1);
        sr.addAI(aiid, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        return this.test.registerServer(sr);
    }

    public class FakeServerTracker extends ServerTracker {

        FakeServerTracker(final ControllerConfig config, final Tools tools, final ILogger logger) {
            super(config, tools, mock(JerseyClient.class), null, logger, null);
            testSetEndpointVerified(true);
            this.runFlag.set(true);
        }

        void testSetEndpointVerified(boolean flag) {
            this.endpointVerified.set(flag);
        }

        @Override
        public UUID getSessionID() {
            return this.serverSessionID;
        }

    }
}
