package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * Tests for the WebHooks connector.
 */
public class TestWebhooks {
    private static final UUID AIID = UUID.fromString("bd2700ff-279b-4bac-ad2f-85a5275ac073");
    private static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    private static final UUID DEVID = UUID.fromString("ef1593e6-503f-481c-a1fd-071a32c69271");
    private static final ChatRequestInfo CHATINFO = new ChatRequestInfo(new AiIdentity(DEVID, AIID), CHATID, "hi", null);
    private JsonSerializer serializer;
    private DatabaseAI fakeDatabase;
    private DatabaseMarketplace fakeDatabaseMarketplace;
    private ILogger fakeLogger;
    private JerseyClient fakeClient;
    private Tools fakeTools;

    private WebHooks webHooks;

    @Before
    public void setup() {
        this.serializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(DatabaseAI.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeClient = mock(JerseyClient.class);
        this.fakeTools = mock(Tools.class);

        this.webHooks = new WebHooksWrapper(this.fakeDatabase, this.fakeDatabaseMarketplace, this.fakeLogger,
                this.serializer, this.fakeClient, this.fakeTools);
    }

    /*
     * executeIntentWebHook returns false if it is provided with an invalid intent.
     */
    @Test
    public void testExecuteWebHook_InvalidIntent() {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        ChatResult chatResult = new ChatResult("Hi");

        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity("{\"text\":\"test\"}").build());

        assertThatExceptionOfType(WebHooks.WebHookInternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(wh, null, chatResult, CHATINFO));
    }

    /*
     * executeIntentWebHook returns null if a webhook has an invalid endpoint.
     */
    @Test
    public void testExecuteWebHook_InvalidEndpoint() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatState(ChatState.getEmpty());

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn("{\"text\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.serverError().build());

        assertThatExceptionOfType(WebHooks.WebHookExternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(wh, mi, chatResult, CHATINFO));
    }

    /*
     * executeIntentWebHook returns a WebHookResponse if a valid webhook is executed.
     */
    @Test
    public void testExecuteWebHook_ValidResponse()
            throws DatabaseException, WebHooks.WebHookException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.fakeTools.generateRandomHexString(anyInt())).thenReturn("deadf00d");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatId(CHATID);
        chatResult.setChatState(ChatState.getEmpty());

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeIntentWebHook(wh, mi, chatResult, CHATINFO);
        verify(spy).getMessageHash(any(), any());
        verify(this.fakeTools, Mockito.never()).generateRandomHexString(anyInt());
        verify(this.fakeDatabase).getBotConfigForWebhookCall(any(), any(), any(), any());
        Assert.assertNotNull(response);
    }

    /*
     * executeIntentWebHook Check that hash function not called if this is not HTTPS
     */
    @Test
    public void testExecuteWebHook_HttpNoHash()
            throws DatabaseException, WebHooks.WebHookException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "http://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatId(CHATID);
        chatResult.setChatState(ChatState.getEmpty());

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeIntentWebHook(wh, mi, chatResult, CHATINFO);
        verify(spy, Mockito.never()).getMessageHash(any(), any());
        Assert.assertNotNull(response);
    }

    /*
     * executeIntentWebHook Check that do generate secret if there already is one
     */
    @Test
    public void testExecuteWebHook_generateSecretifNull()
            throws DatabaseException, WebHooks.WebHookException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn(null);
        when(this.fakeTools.generateRandomHexString(anyInt())).thenReturn("deadf00d");
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatId(CHATID);
        chatResult.setChatState(ChatState.getEmpty());

        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        WebHooks spy = Mockito.spy(this.webHooks);
        doReturn(new WebHookResponse("response")).when(spy).deserializeResponse(any());
        when(getFakeBuilder().post(any())).thenReturn(Response.ok().entity(new WebHookResponse("Success")).build());
        WebHookResponse response = spy.executeIntentWebHook(wh, mi, chatResult, CHATINFO);
        verify(spy).getMessageHash(any(), any());
        verify(this.fakeTools).generateRandomHexString(anyInt());
        Assert.assertNotNull(response);

    }

    /*
     * executeIntentWebHook returns a null if a valid webhook is not found.
     */
    @Test
    public void testExecuteWebHook_NoWebHook() {
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");

        assertThatExceptionOfType(WebHooks.WebHookInternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(null, mi, chatResult, CHATINFO));
    }

    /*
     * executeIntentWebHook returns null if the webhook response fails to deserialize.
     */
    @Test
    public void testExecuteWebHook_ResponseDeserialiseFailed() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatId(CHATID);
        chatResult.setChatState(ChatState.getEmpty());

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(getFakeBuilder().post(any())).thenReturn(Response.ok(new WebHookResponse("Success")).build());
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(this.serializer.deserialize(anyString(), any())).thenReturn(null);

        assertThatExceptionOfType(WebHooks.WebHookExternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(wh, mi, chatResult, CHATINFO));
    }

    /*
     * executeIntentWebHook returns null if a webhook returns an invalid error code.
     */
    @Test
    public void testExecuteWebHook_InvalidErrorCode() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setChatId(CHATID);
        chatResult.setChatState(ChatState.getEmpty());

        when(this.fakeDatabase.getWebhookSecretForBot(any())).thenReturn("123456");
        when(this.serializer.serialize(any())).thenReturn("{\"intentName\":\"test\"}");
        when(getFakeBuilder().post(any())).thenReturn(Response.status(0).entity(new WebHookResponse("Success")).build());

        assertThatExceptionOfType(WebHooks.WebHookExternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(wh, mi, chatResult, CHATINFO));
    }

    /*
     * executeIntentWebHook returns a null if DB config lookup throws, webhook is NOT called.
     */
    @Test
    public void testExecuteWebHook_FailureLookupConfig() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", false);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, CHATID, null);
        ChatResult chatResult = new ChatResult("Hi");
        when(this.fakeDatabase.getBotConfigForWebhookCall(any(), any(), any(), any())).thenThrow(new DatabaseException("BAD CALL"));

        assertThatExceptionOfType(WebHooks.WebHookInternalException.class)
                .isThrownBy(() -> this.webHooks.executeIntentWebHook(wh, mi, chatResult, CHATINFO));

        verify(this.fakeDatabase).getBotConfigForWebhookCall(any(), any(), any(), any());
        // ensure no attempt is made to build the webhook call
        verify(this.fakeClient, never()).target(anyString());
    }

    /*
     * test that the message hashes correctly
     */
    @Test
    public void testHashWebHook_CalculatesOk() throws WebHooks.WebHookInternalException {
        String hashString = this.webHooks.getMessageHash("123456", "ThisIsAMessage".getBytes());

        // Here's what Python calculated this should be using HMAC SHA256
        assert (hashString).equals("13f5ef193847035a837ef7123ffe2efd4748f57f0be74c3897f82064443457f2");
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

    public static class WebHooksWrapper extends WebHooks {

        WebHooksWrapper(final DatabaseAI databaseAi, final DatabaseMarketplace databaseMarketplace,
                        final ILogger logger,
                        final JsonSerializer serializer, final JerseyClient jerseyClient,
                        final Tools tools) {
            super(databaseAi, databaseMarketplace, logger, serializer, jerseyClient, tools);
        }

        @Override
        protected String getEntity(final Response response) {
            return response.getEntity().toString();
        }
    }
}
