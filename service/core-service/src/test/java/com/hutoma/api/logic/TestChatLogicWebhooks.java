package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.facebook.FacebookMessageNode;
import com.hutoma.api.containers.facebook.FacebookRichContentAttachment;
import com.hutoma.api.containers.facebook.FacebookRichContentPayload;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logic.chat.ChatBaseException;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TestChatLogicWebhooks extends TestChatBase {

    private static final String INTENTRESPONSE = "response";

    /***
     * Test that if an active WebHook exists, it is executed.
     */
    @Test
    public void testChat_webHookTriggered()
            throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException, DatabaseException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        intent.setWebHook(new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));

        Assert.assertFalse(mi.isFulfilled());

        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(webHookResponse, result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    /***
     * Test WebHook null response is handled.
     */
    @Test
    public void testChat_webHookNullResponseHandled()
            throws ChatBackendConnector.AiControllerException, DatabaseException, WebHooks.WebHookException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = null;
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any())).thenReturn(wr);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));
        Assert.assertFalse(mi.isFulfilled());
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(INTENTRESPONSE, result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    /***
     * Test that Facebook rich content is passed through
     */
    @Test
    public void testChat_webHookFacebookHandled()
            throws ChatBackendConnector.AiControllerException, DatabaseException, ChatBaseException,
            ServerConnector.AiServicesException, NoServerAvailableException {
        final String intentName = "intent1";

        final String webHookResponse = "webhook executed";
        WebHookResponse wr = getFacebookWebHookResponse(webHookResponse);

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        intent.setWebHook(new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));

        Assert.assertFalse(mi.isFulfilled());

        ChatResult result = getFacebookChat(0.5f);
        Assert.assertEquals(webHookResponse, result.getAnswer());
        Assert.assertNotNull(result.getWebhookResponse());
        Assert.assertNotNull(result.getWebhookResponse().getFacebookNode());
        Assert.assertNotNull(result.getWebhookResponse().getFacebookNode().getAttachment());
        FacebookRichContentPayload payload = result.getWebhookResponse().getFacebookNode().getAttachment().getPayload();
        Assert.assertEquals(webHookResponse, payload.getText());
    }

    private WebHookResponse getFacebookWebHookResponse(final String webHookResponse) {
        WebHookResponse wr = new WebHookResponse(webHookResponse);
        FacebookRichContentPayload richContentPayload = new FacebookRichContentPayload(
                "fakeurl",
                FacebookRichContentPayload.TemplateType.button,
                webHookResponse,
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                "aspect ratio");

        FacebookMessageNode node = new FacebookMessageNode(
                new FacebookRichContentAttachment(
                        FacebookRichContentAttachment.RichContentType.image, richContentPayload));
        wr.setFacebookNode(node);
        return wr;
    }

    /***
     * Test that if an inactive WebHook exists, it is not executed.
     */
    @Test
    public void testChat_inactiveWebHookIgnored()
            throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));

        Assert.assertFalse(mi.isFulfilled());

        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertNotEquals(webHookResponse, result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    /***
     * Test that if an invalid webhook is executed, it is handled.
     */
    @Test
    public void testChat_badWebHookHandled()
            throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException, ChatLogic.IntentException {

        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any()))
                .thenThrow(WebHooks.WebHookExternalException.class);
        ApiChat result = (ApiChat) chatGetWebhook();
        Assert.assertEquals(INTENTRESPONSE, result.getResult().getAnswer());
    }

    @Test
    public void testChat_webHookInternalFail()
            throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException, ChatLogic.IntentException {

        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any()))
                .thenThrow(WebHooks.WebHookInternalException.class);
        Assert.assertTrue(chatGetWebhook() instanceof ApiError);
    }

    @Test
    public void testChhat_webHook_outContext() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException,
            WebHooks.WebHookException, ChatStateHandler.ChatStateException {
        WebHookResponse whr = new WebHookResponse("output line");

        // Define the initial context state
        ChatContext initialContext = new ChatContext();
        initialContext.setValue("var1", "value1", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        initialContext.setValue("var2", "value2", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        initialContext.setValue("var3", "value3", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        ChatState initialState = new ChatState(DateTime.now(), "", "", AIID, new HashMap<>(), 0.5,
                ChatHandoverTarget.Ai, TestDataHelper.getSampleAI(), initialContext);


        ChatContext webhookCtx = new ChatContext();
        webhookCtx.setValue("var1", "newValue", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        webhookCtx.setValue("var4", "value4", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        whr.setChatContext(webhookCtx);

        // To inject a variable with null value we need to do it straight through JSON (as the webhook would do) since
        // our context api doesn't allow you to do this programmatically (as it deletes the value)
        JsonSerializer serializer = new JsonSerializer();
        String s = serializer.serialize(whr);
        s = s.replace("\"variables\":{", "\"variables\":{\"var3\":{\"value\":null,\"lifespan_turns\":-1},");
        whr = (WebHookResponse) serializer.deserialize(s, WebHookResponse.class);

        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialState);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any(), any())).thenReturn(whr);
        ApiChat result = (ApiChat) chatGetWebhook();

        ChatContext resultingContext = result.getResult().getChatState().getChatContext();
        // var1 should have new value
        Assert.assertEquals("newValue", resultingContext.getValue("var1"));
        // var2 should remain untouched since it was not included in the webhook output
        Assert.assertEquals("value2", resultingContext.getValue("var2"));
        // var3 should have been deleted
        Assert.assertFalse(resultingContext.isSet("var3"));
        // var4 should have been added
        Assert.assertEquals("value4", resultingContext.getValue("var4"));
    }

    private ApiResult chatGetWebhook() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        setupFakeChat(0.7d,
                MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        intent.setWebHook(VALID_WEBHOOK);

        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));
        return getChat(0.5f);
    }
}
