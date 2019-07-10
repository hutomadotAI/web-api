package com.hutoma.api.logic;

import com.hutoma.api.ServerMonitor;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.connectors.aiservices.ServiceStatusConnector;
import com.hutoma.api.containers.ApiServersAvailable;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.ILogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.*;

public class TestServerMonitor {

    ILogger fakeLogger;
    Config fakeConfig;
    ServiceStatusConnector fakeControllerConnector;

    ServerMonitorWrapper serverMonitor;

    public class ServerMonitorWrapper extends ServerMonitor {

        public LanguageStatus up = null;
        public LanguageStatus changed = null;
        public SupportedLanguage down = null;

        public ServerMonitorWrapper(ILogger logger, Config config,
                                    ServiceStatusConnector controllerConnector, JsonSerializer jsonSerializer) {
            super(logger, config, controllerConnector, jsonSerializer);
        }

        public void clear() {
            up = null;
            changed = null;
            down = null;
        }

        @Override
        public void languageStatusUp(LanguageStatus languageStatus) {
            this.up = languageStatus;
        }

        @Override
        public void languageStatusDown(SupportedLanguage language) {
            this.down = language;
        }

        @Override
        public void languageStatusChanged(LanguageStatus languageStatus) {
            this.changed = languageStatus;
        }

        @Override
        public void monitorServer() {
            super.monitorServer();
        }
    }

    @Before
    public void setup() {
        this.fakeLogger = mock(ILogger.class);
        this.fakeConfig = mock(Config.class);
        this.fakeControllerConnector = mock(ServiceStatusConnector.class);
        this.respondWithNothing();
        this.serverMonitor = new ServerMonitorWrapper(fakeLogger, fakeConfig,
                fakeControllerConnector, null);
    }

    void respondWithNothing() {
        when(this.fakeControllerConnector.getServiceIdentities(any()))
                .thenReturn(new ApiServersAvailable(Collections.emptyList()));
    }

    void respondWith(ServiceIdentity serviceIdentity) {
        when(this.fakeControllerConnector.getServiceIdentities(any()))
                .thenReturn(new ApiServersAvailable(Collections.singletonList(serviceIdentity)));
    }

    void respondWith(Collection<ServiceIdentity> serviceIdentityList) {
        when(this.fakeControllerConnector.getServiceIdentities(any()))
                .thenReturn(new ApiServersAvailable(serviceIdentityList));
    }

    @Test
    public void testNoServers() {
        this.serverMonitor.monitorServer();
        Assert.assertNull(this.serverMonitor.changed);
        Assert.assertNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }

    @Test
    public void testOnline() {
        this.respondWithNothing();
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();

        this.respondWith(new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();

        Assert.assertNull(this.serverMonitor.changed);
        Assert.assertNotNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }

    @Test
    public void testOffline() {
        this.respondWith(new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();

        this.respondWithNothing();
        this.serverMonitor.monitorServer();

        Assert.assertNull(this.serverMonitor.changed);
        Assert.assertNull(this.serverMonitor.up);
        Assert.assertNotNull(this.serverMonitor.down);
    }

    @Test
    public void testChanged() {
        this.respondWith(new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();

        this.respondWith(Arrays.asList(
                new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V"),
                new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "2.0")));
        this.serverMonitor.monitorServer();

        Assert.assertNotNull(this.serverMonitor.changed);
        Assert.assertNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }

    @Test
    public void testAimlCantChat() {
        this.respondWith(new ServiceIdentity(BackendServerType.AIML, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();
        Assert.assertNull(this.serverMonitor.changed);
        Assert.assertNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }

    @Test
    public void testAimlThenEmb() {
        this.respondWith(new ServiceIdentity(BackendServerType.AIML, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();

        this.respondWith(Arrays.asList(
                new ServiceIdentity(BackendServerType.AIML, SupportedLanguage.EN, "V"),
                new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V")));
        this.serverMonitor.monitorServer();

        Assert.assertNull(this.serverMonitor.changed);
        Assert.assertNotNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }

    @Test
    public void testEmbThenAiml() {
        this.respondWith(new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V"));
        this.serverMonitor.monitorServer();
        this.serverMonitor.clear();

        this.respondWith(Arrays.asList(
                new ServiceIdentity(BackendServerType.AIML, SupportedLanguage.EN, "V"),
                new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, "V")));
        this.serverMonitor.monitorServer();

        Assert.assertNotNull(this.serverMonitor.changed);
        Assert.assertNull(this.serverMonitor.up);
        Assert.assertNull(this.serverMonitor.down);
    }
}
