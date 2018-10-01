package com.hutoma.api.connectors.chat;

import com.google.inject.Provider;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.aiservices.EmbServicesConnector;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.ITrackedThreadSubPool;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class TestChatBackendConnector {

    private DatabaseAI fakeDatabaseAi;
    private DatabaseUser fakeDatabaseUser;
    private Config fakeConfig;
    private ChatEmbConnectorUnderTest connector;
    private JerseyClient fakeJerseyClient;
    private Tools fakeTools;
    private ITrackedThreadSubPool fakeTrackedThreadPool;
    private EmbServicesConnector fakeEmbServicesConnector;
    private JerseyInvocation.Builder fakeJerseyBuilder;
    private JerseyWebTarget fakeTarget;
    private ChatBackendConnector.RequestInProgress fakeRequestInProgress;
    private Future<InvocationResult> fakeFuture;

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
        this.fakeEmbServicesConnector = mock(EmbServicesConnector.class);
        this.fakeRequestInProgress = mock(ChatBackendConnector.RequestInProgress.class);
        this.fakeFuture = mock(Future.class);
        when(fakeRequestInProgress.getFuture()).thenReturn(fakeFuture);

        this.connector = new ChatEmbConnectorUnderTest(this.fakeJerseyClient, this.fakeTools, this.fakeConfig,
                this.fakeTrackedThreadPool,
                mock(ILogger.class), mock(JsonSerializer.class), this.fakeEmbServicesConnector);
    }

    public static class ChatEmbConnectorUnderTest extends ChatEmbConnector {

        ChatEmbConnectorUnderTest(final JerseyClient jerseyClient, final Tools tools, final Config config,
                                  final ITrackedThreadSubPool threadSubPool, final ILogger logger,
                                  final JsonSerializer serializer,
                                  final EmbServicesConnector controllerConnector) {
            super(jerseyClient, tools, config, threadSubPool, logger, serializer, controllerConnector,
                    mock(Provider.class));
        }

        @Override
        public InvocationResult waitForResult(final RequestInProgress requestInProgress, final int timeoutMs)
                throws AiControllerException {
            return super.waitForResult(requestInProgress, timeoutMs);
        }
    }

    @Test
    public void testWaitForResult_Done() throws ChatBackendConnector.AiControllerException,
            ExecutionException, InterruptedException {
        when(fakeFuture.get()).thenReturn(new InvocationResult(
                null, "", 0, 0, 0, TestDataHelper.AIID));
        when(fakeFuture.isDone()).thenReturn(true);
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test
    public void testWaitForResult_NotDone() throws ChatBackendConnector.AiControllerException,
            InterruptedException, ExecutionException, TimeoutException {
        when(fakeFuture.get(anyLong(), any())).thenReturn(new InvocationResult(
                null, "", 0, 0, 0, TestDataHelper.AIID));
        when(fakeFuture.isDone()).thenReturn(false);
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test(expected = ChatBackendConnector.AiControllerTimeoutException.class)
    public void testWaitForResult_Timeout() throws InterruptedException, ExecutionException, TimeoutException,
            ChatBackendConnector.AiControllerException {
        when(fakeFuture.isDone()).thenReturn(false);
        when(fakeFuture.get(anyLong(), any())).thenThrow(new TimeoutException("test"));
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test(expected = ChatBackendConnector.AiControllerTimeoutException.class)
    public void testWaitForResult_ExecutionTimeout() throws InterruptedException, ExecutionException, TimeoutException,
            ChatBackendConnector.AiControllerException {
        when(fakeFuture.isDone()).thenReturn(false);
        when(fakeFuture.get(anyLong(), any())).thenThrow(new ExecutionException(new TimeoutException("test")));
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test(expected = ChatBackendConnector.AiControllerException.class)
    public void testWaitForResult_ExecutionNull() throws InterruptedException, ExecutionException, TimeoutException,
            ChatBackendConnector.AiControllerException {
        when(fakeFuture.isDone()).thenReturn(false);
        when(fakeFuture.get(anyLong(), any())).thenThrow(new ExecutionException(null));
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test(expected = ChatBackendConnector.AiControllerException.class)
    public void testWaitForResult_ExecutionOther() throws InterruptedException, ExecutionException, TimeoutException,
            ChatBackendConnector.AiControllerException {
        when(fakeFuture.isDone()).thenReturn(false);
        when(fakeFuture.get(anyLong(), any())).thenThrow(new ExecutionException(new Exception("other")));
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }

    @Test(expected = ChatBackendConnector.AiControllerException.class)
    public void testWaitForResult_Other() throws InterruptedException, ExecutionException, TimeoutException,
            ChatBackendConnector.AiControllerException {
        when(fakeFuture.isDone()).thenReturn(false);
        when(fakeFuture.get(anyLong(), any())).thenThrow(new InterruptedException("other"));
        this.connector.waitForResult(this.fakeRequestInProgress, 10);
    }
}
