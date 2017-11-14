package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.thread.IThreadConfig;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.TrackedThreadSubPool;

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
    ControllerConfig config;
    IThreadConfig fakeThreadConfig;
    ThreadPool threadPool;
    TrackedThreadSubPool subPool;

    ControllerUnderTest controllerUnderTest;

    @Before
    public void setUp() throws Exception {
        this.config = mock(ControllerConfig.class);
        this.logger = mock(AiServiceStatusLogger.class);
        this.fakeThreadConfig = mock(IThreadConfig.class);
        when(this.fakeThreadConfig.getThreadPoolMaxThreads()).thenReturn(16);
        when(this.fakeThreadConfig.getThreadPoolIdleTimeMs()).thenReturn(1L);
        this.threadPool = new ThreadPool(this.fakeThreadConfig);
        this.subPool = new TrackedThreadSubPool(this.threadPool);
        this.controllerUnderTest = new ControllerUnderTest(this.config, this.subPool, this.logger);
    }

    @Test
    public void testHashCode_Store() throws Exception {
        UUID ai1 = UUID.randomUUID();
        UUID ai2 = UUID.randomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH1);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_None() throws Exception {
        Assert.assertEquals("", this.controllerUnderTest.getHashCodeFor(UUID.randomUUID()));
    }

    @Test
    public void testHashCode_Update() throws Exception {
        UUID ai1 = UUID.randomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH1);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
        this.controllerUnderTest.setHashCodeFor(ai1, HASH2);
        Assert.assertEquals(HASH2, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_OnRegistration() throws Exception {
        UUID ai1 = UUID.randomUUID();
        List<ServerAiEntry> serverAiEntries = Collections.singletonList(
                new ServerAiEntry(ai1, TrainingStatus.AI_TRAINING_COMPLETE, HASH1));
        this.controllerUnderTest.setAllHashCodes(serverAiEntries);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
    }

    @Test
    public void testHashCode_OnRegistrationUpdate() throws Exception {
        UUID ai1 = UUID.randomUUID();
        UUID ai2 = UUID.randomUUID();
        this.controllerUnderTest.setHashCodeFor(ai1, HASH2);
        this.controllerUnderTest.setHashCodeFor(ai2, HASH2);
        List<ServerAiEntry> serverAiEntries = Collections.singletonList(
                new ServerAiEntry(ai1, TrainingStatus.AI_TRAINING_COMPLETE, HASH1));
        this.controllerUnderTest.setAllHashCodes(serverAiEntries);
        Assert.assertEquals(HASH1, this.controllerUnderTest.getHashCodeFor(ai1));
        Assert.assertEquals("", this.controllerUnderTest.getHashCodeFor(ai2));
    }

    public static class ControllerUnderTest extends ControllerBase {

        public ControllerUnderTest(ControllerConfig config, final TrackedThreadSubPool threadSubPool,
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
