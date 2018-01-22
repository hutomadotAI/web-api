package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.connectors.InvocationResult;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.UUID;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class TestChatBackendRequester {

    private ChatBackendRequester requester;
    private FakeTimerTools fakeTimer;
    private Config fakeConfig;

    @Before
    public void setup() throws Exception {
        this.fakeTimer = new FakeTimerTools();
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getBackendCombinedRequestTimeoutMs()).thenReturn(1000L);
        this.requester = new ChatBackendRequester(
                fakeTimer, mock(JerseyClient.class), fakeConfig);
        this.requester = spy(requester);
    }

    @Test
    public void testChatCall_OK() throws Exception {
        InvocationResult result = makeResult();
        doReturn(result).when(requester).callBackend(anyLong());
        Assert.assertEquals(result, requester.call());
    }

    @Test(expected = ProcessingException.class)
    public void testChatCall_Exception() throws Exception {
        doThrow(new ProcessingException("test")).when(requester).callBackend(anyLong());
        requester.call();
    }

    @Test
    public void testChatCall_NullReturn() throws Exception {
        doReturn(null).when(requester).callBackend(anyLong());
        requester.call();
    }

    @Test
    public void testChatCall_BadRequest() throws Exception {
        InvocationResult result = make400();
        doReturn(result).when(requester).callBackend(anyLong());
        Assert.assertEquals(result, requester.call());
    }

    @Test
    public void testChatCall_RetryOK() throws Exception {
        InvocationResult result = makeResult();
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            if (this.fakeTimer.getTimestamp() > 100) {
                return result;
            }
            return make503();
        }).when(requester).callBackend(anyLong());
        Assert.assertEquals(result, requester.call());
    }

    @Test
    public void testChatCall_RetryKeepFailing() throws Exception {
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            return make503();
        }).when(requester).callBackend(anyLong());
        Assert.assertEquals(503, requester.call().getResponse().getStatus());
    }

    @Test
    public void testChatCall_RetryFailBadRequest() throws Exception {
        InvocationResult result = make400();
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            if (this.fakeTimer.getTimestamp() > 50) {
                return result;
            }
            return make503();
        }).when(requester).callBackend(anyLong());
        Assert.assertEquals(400, requester.call().getResponse().getStatus());
    }
    private InvocationResult makeResult() {
        return new InvocationResult(
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"),
                null, "endpoint", 1000);
    }

    private InvocationResult make503() {
        Response response503 = mock(Response.class);
        when(response503.getStatus()).thenReturn(HttpURLConnection.HTTP_UNAVAILABLE);
        return new InvocationResult(
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"),
                response503, "endpoint", 100);
    }

    private InvocationResult make400() {
        Response response400 = mock(Response.class);
        when(response400.getStatus()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        return new InvocationResult(
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"),
                response400, "endpoint", 100);
    }
}
