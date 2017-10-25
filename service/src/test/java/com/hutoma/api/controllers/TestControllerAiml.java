package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerBase.RequestFor;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * Created by David MG on 08/02/2017.
 */
public class TestControllerAiml {

    AiServiceStatusLogger logger;
    Config config;
    FakeTimerTools tools;
    ControllerAiml test;
    ThreadSubPool fakeThreadSubPool;

    @Before
    public void setUp() throws Exception {
        this.logger = mock(AiServiceStatusLogger.class);
        this.tools = new FakeTimerTools();
        this.config = mock(Config.class);
        this.fakeThreadSubPool = mock(ThreadSubPool.class);
        this.test = new ControllerAiml(TestControllerAiml.this.config, this.fakeThreadSubPool, null,
                this.logger, mock(QueueProcessor.class)) {
            @Override
            protected ServerTracker createNewServerTracker() {
                return new FakeServerTracker(TestControllerAiml.this.config, TestControllerAiml.this.tools, this.logger);
            }
        };
    }

    @Test
    public void testControllerAiml_noServer() {
        try {
            this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Chat);
        } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
            return;
        }
        Assert.fail("expected an exception");
    }

    @Test
    public void testControllerAiml_oneServer() throws ServerMetadata.NoServerAvailable {
        UUID ai1 = this.tools.createNewRandomUUID();
        UUID sid = registerServer(ai1);
        Assert.assertEquals(sid, this.test.getServerFor(ai1, RequestFor.Chat).getSessionID());
    }

    @Test
    public void testControllerAiml_requestDifferentAiid() throws ServerMetadata.NoServerAvailable {
        UUID ai1 = this.tools.createNewRandomUUID();
        UUID ai2 = this.tools.createNewRandomUUID();
        registerServer(ai1);
        try {
            this.test.getServerFor(ai2, RequestFor.Chat);
        } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
            return;
        }
        Assert.fail("expected an exception");
    }

    @Test
    public void testControllerAiml_twoAIMLServers() throws ServerMetadata.NoServerAvailable {
        UUID ai1 = this.tools.createNewRandomUUID();
        UUID ai2 = this.tools.createNewRandomUUID();
        UUID sid1 = registerServer(ai1);
        UUID sid2 = registerServer(ai2);
        Assert.assertEquals(sid1, this.test.getServerFor(ai1, RequestFor.Chat).getSessionID());
        Assert.assertEquals(sid2, this.test.getServerFor(ai2, RequestFor.Chat).getSessionID());
    }

    private UUID registerServer(UUID aiid) {
        //ServerTracker server = new FakeServerTracker(this.config, this.tools, this.logger);
        ServerRegistration sr = new ServerRegistration(
                BackendServerType.AIML, "url1", 1, 1);
        sr.addAI(aiid, TrainingStatus.AI_TRAINING_COMPLETE, "hash");
        return this.test.registerServer(sr);
    }

    public class FakeServerTracker extends ServerTracker {

        public FakeServerTracker(final Config config, final Tools tools, final ILogger logger) {
            super(config, tools, mock(JerseyClient.class), null, logger, null);
            testSetEndpointVerified(true);
        }

        public void testSetEndpointVerified(boolean flag) {
            this.endpointVerified.set(flag);
        }

        public UUID getSessionID() {
            return this.serverSessionID;
        }

    }
}
