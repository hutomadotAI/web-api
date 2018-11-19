package com.hutoma.api.logic.chat;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logic.FacebookChatHandler;
import com.hutoma.api.memory.ChatStateHandler;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TestWebhookHandler {

    private static final String WEBHOOK_ENCODING_SECRET = "secret";

    private ChatStateHandler fakeChatStateHandler;
    private FacebookChatHandler fakeFacebookChatHandler;
    private Config fakeConfig;
    private WebhookHandler webhookHandler;

    public TestWebhookHandler() {
        this.fakeChatStateHandler = mock(ChatStateHandler.class);
        this.fakeFacebookChatHandler = mock(FacebookChatHandler.class);
        this.fakeConfig = mock(Config.class);

        this.webhookHandler = new WebhookHandler(this.fakeChatStateHandler, this.fakeFacebookChatHandler, this.fakeConfig, mock(ChatLogger.class));
    }


    @Test
    public void testWebhookHandler_outContext() {
        WebHookResponse whr = new WebHookResponse("output line");

        // Define the initial context state
        ChatContext initialContext = new ChatContext();
        initialContext.setValue("var1", "value1", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        initialContext.setValue("var2", "value2", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        initialContext.setValue("var3", "value3", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);

        ChatContext webhookCtx = new ChatContext();
        webhookCtx.setValue("var1", "newValue", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        webhookCtx.setValue("var4", "value4", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        whr.setChatContext(webhookCtx);

        webhookHandler.updateChatContext(initialContext, webhookCtx);

        // var1 should have new value
        Assert.assertEquals("newValue", initialContext.getValue("var1"));
        // var2 should remain untouched since it was not included in the webhook output
        Assert.assertEquals("value2", initialContext.getValue("var2"));
        // var3 should have remained untouched
        Assert.assertEquals("value3", initialContext.getValue("var3"));
        // var4 should have been added
        Assert.assertEquals("value4", initialContext.getValue("var4"));
    }

    @Test
    public void testWebhookHandler_outContext_deleteVariable() {
        WebHookResponse whr = new WebHookResponse("output line");
        ChatContext webhookCtx = new ChatContext();
        webhookCtx.setValue("var4", "value4", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        whr.setChatContext(webhookCtx);
        // To inject a variable with null value we need to do it straight through JSON (as the webhook would do) since
        // our context api doesn't allow you to do this programmatically (as it deletes the value)
        JsonSerializer serializer = new JsonSerializer();
        String s = serializer.serialize(whr);
        s = s.replace("\"variables\":{", "\"variables\":{\"var3\":{\"value\":null,\"lifespan_turns\":-1},");
        whr = (WebHookResponse) serializer.deserialize(s, WebHookResponse.class);

        // Define the initial context state
        ChatContext initialContext = new ChatContext();
        initialContext.setValue("var1", "value1", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        initialContext.setValue("var3", "value3", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);

        this.webhookHandler.updateChatContext(initialContext, whr.getChatContext());

        // var1 should have maintained the value
        Assert.assertEquals("value1", initialContext.getValue("var1"));
        // var3 should have been deleted
        Assert.assertFalse(initialContext.isSet("var3"));
        // var4 should have been added
        Assert.assertEquals("value4", initialContext.getValue("var4"));
    }

    @Test
    public void testWebhookHandler_outContext_noVariables() {
        // Define the initial context state
        ChatContext initialContext = new ChatContext();
        initialContext.setValue("var1", "value1", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);

        this.webhookHandler.updateChatContext(initialContext, null);

        // var1 should have maintained the value
        Assert.assertEquals("value1", initialContext.getValue("var1"));
    }

    @Test
    public void testWebhookHandler_runWebhookCallback() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        IntegrationData integrationData = new IntegrationData(IntegrationType.FACEBOOK);
        ChatState state = getTestState(getTestWebhookSessionList(token), integrationData);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(state);
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(token);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus().getCode());
        // Webhook session was deleted since there was only for single use
        Assert.assertTrue(state.getWebhookSessions().isEmpty());
        // State was saved
        verify(this.fakeChatStateHandler).saveState(any(), any(), any(), any());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_invalidChatIdHash() throws ChatStateHandler.ChatStateException {
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(null);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", new WebHookResponse("output line"));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_noWebhookSession() throws ChatStateHandler.ChatStateException {
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(null, null));
        ApiResult response = this.webhookHandler.runWebhookCallback("123", new WebHookResponse("output line"));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_invalidToken() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(getTestWebhookSessionList(token), null));
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", new WebHookResponse("output line"));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_invalidTokenSigningKey() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(getTestWebhookSessionList(token), null));
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), "new secret"));
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_noIntegrationData() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(getTestWebhookSessionList(token), null));
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(token);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_integrationNotFacebook() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        IntegrationData integrationData = new IntegrationData(IntegrationType.NONE);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(getTestWebhookSessionList(token), integrationData));
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(token);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_tokenClaimsMismatch() throws ChatStateHandler.ChatStateException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        IntegrationData integrationData = new IntegrationData(IntegrationType.NONE);
        // here we need to force teh existing state to have unexpected values as if done from the webhook side the token string would mismatch the existing one
        ChatState state = getTestState(getTestWebhookSessionList(token), integrationData);
        state.setDevId(UUID.randomUUID());
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(state);
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(token);
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus().getCode());
    }

    @Test
    public void testWebhookHandler_runWebhookCallback_integrationDbException() throws ChatStateHandler.ChatStateException, DatabaseException {
        String token = WebHookPayload.generateWebhookToken(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, UUID.randomUUID(), WEBHOOK_ENCODING_SECRET);
        IntegrationData integrationData = new IntegrationData(IntegrationType.FACEBOOK);
        when(this.fakeChatStateHandler.getState(anyString())).thenReturn(getTestState(getTestWebhookSessionList(token), integrationData));
        when(this.fakeConfig.getWebhookEncodingSecret()).thenReturn(WEBHOOK_ENCODING_SECRET);
        WebHookResponse whr = new WebHookResponse("output line");
        whr.setToken(token);
        doThrow(DatabaseException.class).when(this.fakeFacebookChatHandler).processWebhookCallbackResponse(any(), any(), any(), any());
        ApiResult response = this.webhookHandler.runWebhookCallback("123", whr);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatus().getCode());
    }

    private ChatState getTestState(final List<WebHookSession> webHookSessions, final IntegrationData integrationData) {
        ChatState chatState = ChatState.getEmpty();
        chatState.setDevId(TestDataHelper.DEVID_UUID);
        chatState.setAi(TestDataHelper.getSampleAI());
        if (webHookSessions != null) {
            chatState.setWebhookSessions(webHookSessions);
        }
        if (integrationData != null) {
            chatState.setIntegrationData(integrationData);
        }
        return chatState;
    }

    // We need to create a mutable list since sessions may get deleted at the end of processing
    // (Collections.singletonList() creates an immutable list)
    private List<WebHookSession> getTestWebhookSessionList(final String token) {
        List<WebHookSession> sessions = new ArrayList<>();
        sessions.add(new WebHookSession(token));
        return sessions;
    }
}
