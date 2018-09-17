package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ChatState;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

public class TestChatBackendRequester {

    private ChatBackendRequester requester;
    private FakeTimerTools fakeTimer;
    private Config fakeConfig;
    private JsonSerializer fakeSerializer;
    private ControllerConnector fakeControllerConnector;
    private ApiServerEndpointMulti.ServerEndpointResponse fakeEndpointResponse;
    private JerseyWebTarget fakeWebTarget;

    @Before
    public void setup() throws Exception {
        this.fakeTimer = new FakeTimerTools();
        this.fakeSerializer = new JsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeEndpointResponse = mock(ApiServerEndpointMulti.ServerEndpointResponse.class);
        when(fakeEndpointResponse.getHash()).thenReturn("hash");
        this.fakeWebTarget = mock(JerseyWebTarget.class);
        when(this.fakeWebTarget.queryParam(any(), any())).thenReturn(fakeWebTarget);
        when(this.fakeWebTarget.resolveTemplates(any())).thenReturn(fakeWebTarget);

        this.fakeControllerConnector = mock(ControllerConnector.class);
        when(fakeConfig.getBackendCombinedRequestTimeoutMs()).thenReturn(1000L);
        this.requester = new ChatBackendRequester(
                fakeTimer, mock(JerseyClient.class), fakeConfig, fakeSerializer);
        this.requester.initialise(fakeControllerConnector, new AiIdentity(TestDataHelper.DEVID_UUID, TestDataHelper.AIID),
                makeServerEndpoint(), new HashMap<>(), mock(ChatState.class),
                fakeTimer.getTimestamp() + 1000);
        when(fakeControllerConnector.getBackendChatEndpointMulti(any(), any())).thenReturn(makeServerResponse());
        this.requester = spy(requester);
    }

    private Map<UUID,ApiServerEndpointMulti.ServerEndpointResponse> makeServerResponse() {
        HashMap<UUID, ApiServerEndpointMulti.ServerEndpointResponse> serverEndpointResponseHashMap = new HashMap<>();
        ApiServerEndpointMulti.ServerEndpointResponse serverEndpointResponse = makeServerEndpoint();
        serverEndpointResponseHashMap.put(serverEndpointResponse.getAiid(), serverEndpointResponse);
        return serverEndpointResponseHashMap;
    }

    private ApiServerEndpointMulti.ServerEndpointResponse makeServerEndpoint() {
        return new ApiServerEndpointMulti.ServerEndpointResponse(
                TestDataHelper.AIID, "url", "identifier", "hash");
    }

    @Test
    public void testChatCall_OK() throws Exception {
        InvocationResult result = makeResult();
        doReturn(result).when(requester).callBackend(any(), anyLong(), anyInt());
        Assert.assertEquals(result, requester.call());
    }

    @Test(expected = ProcessingException.class)
    public void testChatCall_Exception() throws Exception {
        doThrow(new ProcessingException("test")).when(requester).callBackend(any(), anyLong(), anyInt());
        requester.call();
    }

    @Test
    public void testChatCall_NullReturn() throws Exception {
        doReturn(null).when(requester).callBackend(any(), anyLong(), anyInt());
        requester.call();
    }

    @Test
    public void testChatCall_BadRequest() throws Exception {
        InvocationResult result = make400();
        doReturn(result).when(requester).callBackend(any(), anyLong(), anyInt());
        Assert.assertEquals(result, requester.call());
    }

    @Test
    public void testChatCall_RetryOK() throws Exception {
        InvocationResult result = makeResult();
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            if (this.fakeTimer.getElapsedTime() > 100) {
                return result;
            }
            return make503();
        }).when(requester).callBackend(any(), anyLong(), anyInt());
        Assert.assertEquals(result, requester.call());
    }

    @Test
    public void testChatCall_RetryKeepFailing() throws Exception {
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            return make503();
        }).when(requester).callBackend(any(), anyLong(), anyInt());
        Assert.assertEquals(503, requester.call().getResponse().getStatus());
    }

    @Test
    public void testChatCall_RetryFailBadRequest() throws Exception {
        InvocationResult result = make400();
        doAnswer(x -> {
            this.fakeTimer.threadSleep(10);
            if (this.fakeTimer.getElapsedTime() > 50) {
                return result;
            }
            return make503();
        }).when(requester).callBackend(any(), anyLong(), anyInt());
        Assert.assertEquals(400, requester.call().getResponse().getStatus());
    }

    @Test
    public void testParameterTemplates() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        this.requester.addTargetParameters(fakeEndpointResponse, paramMap, this.fakeWebTarget);
        verify(this.fakeWebTarget, times(1)).resolveTemplates(any());
    }

    @Test
    public void testParameterTemplates_Nulls() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        paramMap.put("key2", null);
        this.requester.addTargetParameters(fakeEndpointResponse, paramMap, this.fakeWebTarget);
        verify(this.fakeWebTarget, times(1)).resolveTemplates(any());
    }

    @Test
    public void testParameterTemplates_NullHash() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("key", "value");
        this.requester.addTargetParameters(fakeEndpointResponse, paramMap, this.fakeWebTarget);
        verify(this.fakeWebTarget, times(1)).resolveTemplates(any());
    }

    private InvocationResult makeResult() {
        return new InvocationResult(null, "endpoint", 1000, 1000, 1,
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"));
    }

    private InvocationResult make503() {
        Response response503 = mock(Response.class);
        when(response503.getStatus()).thenReturn(HttpURLConnection.HTTP_UNAVAILABLE);
        return new InvocationResult(response503, "endpoint", 100, 100, 1,
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"));
    }

    private InvocationResult make400() {
        Response response400 = mock(Response.class);
        when(response400.getStatus()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        return new InvocationResult(response400, "endpoint", 100, 100, 1,
                UUID.fromString("31c4ab35-dfeb-428a-8b09-e123266edeb1"));
    }
}
