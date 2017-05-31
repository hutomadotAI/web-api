package com.hutoma.api.connectors;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.controllers.ServerMetadata;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the WebHooks connector.
 */
public class TestWebhooks {
    private static final UUID AIID = UUID.fromString("bd2700ff-279b-4bac-ad2f-85a5275ac073");
    private static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    private static final UUID DEVID = UUID.fromString("ef1593e6-503f-481c-a1fd-071a32c69271");

    private JsonSerializer serializer;
    private Database fakeDatabase;
    private ILogger fakeLogger;
    private JerseyClient fakeClient;

    private WebHooks webHooks;

    @Before
    public void setup() throws ServerMetadata.NoServerAvailable {
        this.serializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeClient = mock(JerseyClient.class);

        this.webHooks = new WebHooks(this.fakeDatabase, this.fakeLogger, this.serializer, this.fakeClient);
    }

    /*
     * activeWebhookExists returns true if a webhook exists for an intent, and is enabled.
     */
    @Test
    public void testActiveWebHookExists_Exists() throws Database.DatabaseException {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);

        Assert.assertTrue(this.webHooks.activeWebhookExists(mi, DEVID));
    }

    /*
     * activeWebhookExists returns false if a webhook exists for an intent, and is disabled.
     */
    @Test
    public void testActiveWebHookExists_ExistsInactive() throws Database.DatabaseException {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);

        Assert.assertFalse(this.webHooks.activeWebhookExists(mi, DEVID));
    }

    /*
     * activeWebhookExists returns true if a webhook does not exist for an intent.
     */
    @Test
    public void testActiveWebHookExists_DoesntExist() throws Database.DatabaseException {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);

        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(null);
        Assert.assertFalse(this.webHooks.activeWebhookExists(mi, DEVID));
    }

    /*
     * executeWebHook returns false if it is provided with an invalid intent.
     */
    @Test
    public void testExecuteWebHook_InvalidIntent() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(null);
        ChatResult chatResult = new ChatResult("Hi");

        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity("{\"text\":\"test\"}").build());
        WebHookResponse response = this.webHooks.executeWebHook(null, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if a webhook has an invalid endpoint.
     */
    @Test
    public void testExecuteWebHook_InvalidEndpoint() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn("{\"text\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.serverError().build());
        WebHookResponse response = this.webHooks.executeWebHook(mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns a WebHookResponse if a valid webhook is executed.
     */
    @Test
    public void testExecuteWebHook_ValidResponse() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");

        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeWebHook(mi, chatResult, DEVID);
        Assert.assertNotNull(response);
    }

    /*
     * executeWebHook returns a null if a valid webhook is not found.
     */
    @Test
    public void testExecuteWebHook_NoWebHook() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(null);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        WebHookResponse response = this.webHooks.executeWebHook(mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if the webhook response fails to deserialize.
     */
    @Test
    public void testExecuteWebHook_ResponseDeserialiseFailed() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(getFakeBuilder().post(any())).thenReturn(Response.accepted().entity(new WebHookResponse("Success")).build());
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn("{\"text\":\"test\"}");
        WebHookResponse response = this.webHooks.executeWebHook(mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if a webhook returns an invalid error code.
     */
    @Test
    public void testExecuteWebHook_InvalidErrorCode() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.accepted().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = this.webHooks.executeWebHook(mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * Creates a fake builder for the Jersey Client.
     */
    private JerseyInvocation.Builder getFakeBuilder() {
        JerseyWebTarget jerseyWebTarget = Mockito.mock(JerseyWebTarget.class);
        JerseyInvocation.Builder builder = Mockito.mock(JerseyInvocation.Builder.class);
        when(this.fakeClient.target(any(String.class))).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.path(anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.property(anyString(), any())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.queryParam(anyString(), anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.request()).thenReturn(builder);
        return builder;
    }
}
