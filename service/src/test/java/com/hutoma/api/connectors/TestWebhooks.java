package com.hutoma.api.connectors;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
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
import static org.mockito.Mockito.*;

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
    private Tools fakeTools;

    private WebHooks webHooks;

    @Before
    public void setup() throws ServerMetadata.NoServerAvailable {
        this.serializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeClient = mock(JerseyClient.class);
        this.fakeTools = mock(Tools.class);

        this.webHooks = new WebHooks(this.fakeDatabase, this.fakeLogger, this.serializer, this.fakeClient,
                this.fakeTools);
    }

    /*
     * activeWebhookExists returns true if a webhook exists for an intent, and is enabled.
     */
    @Test
    public void testGetWebHook_PassThrough() throws Database.DatabaseException {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);

        WebHook retrieved = this.webHooks.getWebHookForIntent(mi, DEVID);
        Assert.assertEquals(wh, retrieved);
    }


    /*
     * executeWebHook returns false if it is provided with an invalid intent.
     */
    @Test
    public void testExecuteWebHook_InvalidIntent() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        ChatResult chatResult = new ChatResult("Hi");

        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity("{\"text\":\"test\"}").build());
        WebHookResponse response = this.webHooks.executeWebHook(wh, null, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if a webhook has an invalid endpoint.
     */
    @Test
    public void testExecuteWebHook_InvalidEndpoint() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn("{\"text\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.serverError().build());
        WebHookResponse response = this.webHooks.executeWebHook(wh, mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns a WebHookResponse if a valid webhook is executed.
     */
    @Test
    public void testExecuteWebHook_ValidResponse() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.fakeTools.generateRandomHexString(anyInt())).thenReturn("deadf00d");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeWebHook(wh, mi, chatResult, DEVID);
        verify(spy).getMessageHash(any(), any(), any());
        verify(this.fakeTools, Mockito.never()).generateRandomHexString(anyInt());
        Assert.assertNotNull(response);
    }

    /*
     * executeWebHook Check that hash function not called if this is not HTTPS
     */
    @Test
    public void testExecuteWebHook_HttpNoHash() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "http://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeWebHook(wh, mi, chatResult, DEVID);
        verify(spy, Mockito.never()).getMessageHash(any(), any(), any());
        Assert.assertNotNull(response);
    }

    /*
    * executeWebHook Check that do generate secret if there already is one
    */
    @Test
    public void testExecuteWebHook_generateSecretifNull() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn(null);
        when(this.fakeTools.generateRandomHexString(anyInt())).thenReturn("deadf00d");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeWebHook(wh, mi, chatResult, DEVID);
        verify(spy).getMessageHash(any(), any(), any());
        verify(this.fakeTools).generateRandomHexString(anyInt());
        Assert.assertNotNull(response);

    }

    /*
     * executeWebHook returns a null if a valid webhook is not found.
     */
    @Test
    public void testExecuteWebHook_NoWebHook() throws Database.DatabaseException, IOException {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        WebHookResponse response = this.webHooks.executeWebHook(null, mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if the webhook response fails to deserialize.
     */
    @Test
    public void testExecuteWebHook_ResponseDeserialiseFailed() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(getFakeBuilder().post(any())).thenReturn(Response.accepted().entity(new WebHookResponse("Success")).build());
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn("{\"text\":\"test\"}");
        WebHookResponse response = this.webHooks.executeWebHook(wh, mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * executeWebHook returns null if a webhook returns an invalid error code.
     */
    @Test
    public void testExecuteWebHook_InvalidErrorCode() throws Database.DatabaseException, IOException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.accepted().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = this.webHooks.executeWebHook(wh, mi, chatResult, DEVID);
        Assert.assertNull(response);
    }

    /*
     * test that the message hashes correctly
     */
    @Test
    public void testHashWebHook_CalculatesOk() throws Database.DatabaseException, IOException {
        String hashString = this.webHooks.getMessageHash(DEVID.toString(), "123456", "ThisIsAMessage".getBytes());

        // Here's what Python calculated this should be using HMAC SHA256
        assert(hashString).equals("13f5ef193847035a837ef7123ffe2efd4748f57f0be74c3897f82064443457f2");
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
        when(builder.header(any(), any())).thenReturn(builder);
        return builder;
    }
}
