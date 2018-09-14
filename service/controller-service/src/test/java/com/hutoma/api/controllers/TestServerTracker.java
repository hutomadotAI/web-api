package com.hutoma.api.controllers;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.ThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 03/02/2017.
 */
public class TestServerTracker {

    FakeTimerTools tools;
    ControllerConfig fakeConfig;
    ServerTrackerUnderTest testClass;

    @Before
    public void setup() {
        this.tools = new FakeTimerTools();
        this.fakeConfig = mock(ControllerConfig.class);
        when(this.fakeConfig.getServerHeartbeatFailureCutOffMs()).thenReturn(5000L);
        when(this.fakeConfig.getServerHeartbeatEveryMs()).thenReturn(2000L);
        when(this.fakeConfig.getServerHeartbeatMinimumGapMs()).thenReturn(500L);
        this.testClass = new ServerTrackerUnderTest(this.fakeConfig, this.tools,
                mock(JerseyClient.class), mock(JsonSerializer.class), mock(ILogger.class),
                mock(ThreadSubPool.class));
        this.testClass.trackServer(new ServerRegistration(BackendServerType.EMB, "url", 1, 1,
                SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION));
    }

    @Test
    public void testWhenHeartbeatNeverWorks() {
        this.testClass.setTimeAfterWhichHeartbeatFails(0);
        this.testClass.call();
        Assert.assertTrue(this.tools.getElapsedTime() == this.fakeConfig.getServerHeartbeatMinimumGapMs());
    }

    @Test
    public void testHeartbeatWorksTillError() {
        this.testClass.setTimeAfterWhichHeartbeatFails(this.tools.getTimestamp() + 20000L);
        this.testClass.call();
        Assert.assertTrue(this.tools.getElapsedTime() >= 20000L);
    }

    @Test
    public void testHeartbeatWorksTillTerminated() {
        this.testClass.setTimeAfterWhichHeartbeatFails(Long.MAX_VALUE);
        this.tools.setBehaviour((x) -> {
            if (x.getElapsedTime() > 20000L) {
                this.testClass.endServerSession();
            }
        });
        this.testClass.call();
        Assert.assertTrue(this.tools.getElapsedTime() >= 20000L);
    }

    public class ServerTrackerUnderTest extends ServerTracker {

        private long timeAfterWhichHeartbeatFails = 0;

        public ServerTrackerUnderTest(final ControllerConfig config, final Tools tools, final JerseyClient jerseyClient,
                                      final JsonSerializer jsonSerializer, final ILogger logger,
                                      final ThreadSubPool threadSubPool) {
            super(config, tools, jerseyClient, jsonSerializer, logger, threadSubPool);
        }

        public void setTimeAfterWhichHeartbeatFails(final long timeAfterWhichHeartbeatFails) {
            this.timeAfterWhichHeartbeatFails = timeAfterWhichHeartbeatFails;
        }

        @Override
        protected boolean beatHeart() {
            return (TestServerTracker.this.tools.getTimestamp() <= this.timeAfterWhichHeartbeatFails);
        }
    }


}
