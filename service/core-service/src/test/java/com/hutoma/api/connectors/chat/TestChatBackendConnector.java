package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.aiservices.WnetServicesConnector;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestChatBackendConnector {

    private DatabaseAI fakeDatabaseAi;
    private DatabaseUser fakeDatabaseUser;
    private Config fakeConfig;
    private ChatBackendConnector connector;
    private JerseyClient fakeJerseyClient;
    private Tools fakeTools;
    private TrackedThreadSubPool fakeTrackedThreadPool;
    private WnetServicesConnector fakeWnetServicesConnector;
    private JerseyInvocation.Builder fakeJerseyBuilder;
    private JerseyWebTarget fakeTarget;

    @Before
    public void setup() {
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseUser = mock(DatabaseUser.class);
        this.fakeConfig = mock(Config.class);
        this.fakeJerseyClient = mock(JerseyClient.class);
        this.fakeJerseyBuilder = TestDataHelper.mockJerseyClient(this.fakeJerseyClient);
        this.fakeTarget = this.fakeJerseyClient.target("");
        this.fakeTools = new FakeTimerTools();
        this.fakeTrackedThreadPool = mock(TrackedThreadSubPool.class);
        this.fakeWnetServicesConnector = mock(WnetServicesConnector.class);

        this.connector = new ChatWnetConnector(this.fakeJerseyClient, this.fakeTools, this.fakeConfig,
                this.fakeTrackedThreadPool,
                mock(ILogger.class), mock(JsonSerializer.class), this.fakeWnetServicesConnector);

    }

    @Test
    public void testParameterTemplates() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        this.connector.createCallable("endpoint", TestDataHelper.DEVID_UUID, TestDataHelper.AIID,
                paramMap, "hash");
        verify(this.fakeTarget, times(1)).resolveTemplates(any());
    }

    @Test
    public void testParameterTemplates_Nulls() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        paramMap.put("key2", null);
        this.connector.createCallable("endpoint", TestDataHelper.DEVID_UUID, TestDataHelper.AIID,
                paramMap, "hash");
        verify(this.fakeTarget, times(1)).resolveTemplates(any());
    }

    @Test
    public void testParameterTemplates_NullHash() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        this.connector.createCallable("endpoint", TestDataHelper.DEVID_UUID, TestDataHelper.AIID,
                paramMap, null);
        verify(this.fakeTarget, times(1)).resolveTemplates(any());
    }
}
