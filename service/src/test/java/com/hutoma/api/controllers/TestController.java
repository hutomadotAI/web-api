package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ThreadPool;
import com.hutoma.api.common.TrackedThreadSubPool;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 15/02/2017.
 */
public class TestController {

    private static final String HASH1 = "hash1";
    private static final String HASH2 = "hash2";

    AiServiceStatusLogger logger;
    Config config;
    FakeTimerTools tools;
    ThreadPool threadPool;
    TrackedThreadSubPool subPool;

    ControllerUnderTest controllerUnderTest;

    @Before
    public void setUp() throws Exception {
        this.config = mock(Config.class);
        this.logger = mock(AiServiceStatusLogger.class);
        this.tools = new FakeTimerTools();
        when(this.config.getThreadPoolMaxThreads()).thenReturn(16);
        when(this.config.getThreadPoolIdleTimeMs()).thenReturn(1L);
        this.threadPool = new ThreadPool(this.config);
        this.subPool = new TrackedThreadSubPool(this.threadPool);
        this.controllerUnderTest = new ControllerUnderTest(this.config, this.subPool, this.logger);
    }

    @Test
    public void testHashCode_Store() throws Exception {
        UUID ai1 = this.tools.createNewRandomUUID();
        UUID ai2 = this.tools.createNewRandomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH1);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_None() throws Exception {
        Assert.assertEquals("", this.controllerUnderTest.getHashCodeFor(this.tools.createNewRandomUUID()));
    }

    @Test
    public void testHashCode_Update() throws Exception {
        UUID ai1 = this.tools.createNewRandomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH1);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
        this.controllerUnderTest.setHashCodeFor(ai1, HASH2);
        Assert.assertEquals(HASH2, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_OnRegistration() throws Exception {
        UUID ai1 = this.tools.createNewRandomUUID();
        List<ServerAiEntry> serverAiEntries = Collections.singletonList(
                new ServerAiEntry(ai1, TrainingStatus.AI_TRAINING_COMPLETE, HASH1));
        this.controllerUnderTest.setAllHashCodes(serverAiEntries);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_OnRegistrationUpdate() throws Exception {
        UUID ai1 = this.tools.createNewRandomUUID();
        UUID ai2 = this.tools.createNewRandomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH2);
        this.controllerUnderTest.setHashCodeFor(ai2, HASH2);
        List<ServerAiEntry> serverAiEntries = Collections.singletonList(
                new ServerAiEntry(ai1, TrainingStatus.AI_TRAINING_COMPLETE, HASH1));
        this.controllerUnderTest.setAllHashCodes(serverAiEntries);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
        Assert.assertEquals("", this.controllerUnderTest.getHashCodeFor(ai2));
    }

    public static class ControllerUnderTest extends ControllerBase {

        public ControllerUnderTest(Config config, final TrackedThreadSubPool threadSubPool,
                                   final AiServiceStatusLogger logger) {
            super(config, threadSubPool, null, logger);
        }

        @Override
        public boolean logErrorIfNoTrainingCapacity() {
            return true;
        }

        @Override
        protected ServerTracker createNewServerTracker() {
            return mock(ServerTracker.class);
        }
    }
}
