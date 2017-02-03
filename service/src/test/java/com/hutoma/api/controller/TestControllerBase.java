package com.hutoma.api.controller;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ServerTracker;

import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 03/02/2017.
 */
public class TestControllerBase {


    FakeTimerTools tools;
    Config fakeConfig;
    ServerTracker fakeServerTracker;
    ControllerUnderTest controller;

    @Test
    public void testRegisterOne() throws Exception {
        this.controller.registerServer(new ServerRegistration("wnet", "url", 1, 1));
        List<String> results = this.controller.getBackendEndpoints();
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("mock", results.get(0));
    }

    @Test
    public void testRegisterFallback() throws Exception {
        List<String> results = this.controller.getBackendEndpoints();
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("fallback", results.get(0));
    }

    @Before
    public void setup() {
        this.tools = new FakeTimerTools();
        this.fakeConfig = mock(Config.class);
        this.controller = new ControllerUnderTest(this.fakeConfig, this.tools,
                mock(ThreadSubPool.class), mock(ServiceLocator.class), mock(ILogger.class));
    }

    public class ControllerUnderTest extends ControllerBase {

        public ControllerUnderTest(final Config config, final Tools tools, final ThreadSubPool threadSubPool, final ServiceLocator serviceLocator, final ILogger logger) {
            super(config, tools, threadSubPool, serviceLocator, logger);
        }

        @Override
        protected ServerTracker createNewServerTracker() {
            TestDataHelper helper = new TestDataHelper();
            ServerTracker mock = mock(ServerTracker.class);
            when(mock.trackServer(any())).thenReturn(
                    helper.getUUIDList().get(0));
            when(mock.getServerUrl()).thenReturn("mock");
            return mock;
        }

        @Override
        public List<String> getBackendEndpoints() {
            return super.getBackendEndpoints();
        }

        @Override
        public List<String> getFallbackBackendEndpoints() {
            return Collections.singletonList("fallback");
        }

    }
}
