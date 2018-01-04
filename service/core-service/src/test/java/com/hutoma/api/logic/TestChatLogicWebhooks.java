package com.hutoma.api.logic;

import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.memory.MemoryIntentHandler;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TestChatLogicWebhooks extends TestChatBase {

    private static final String INTENTRESPONSE = "response";

    /***
     * Test that if an active WebHook exists, it is executed.
     */
    @Test
    public void testChat_webHookTriggered()
            throws ChatBackendConnector.AiControllerException, IOException,
                WebHooks.WebHookException, DatabaseException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeDatabaseAi.getWebHook(any(), any())).thenReturn(wh);
        when(this.fakeWebHooks.getWebHookForIntent(any(), any())).thenReturn(VALID_WEBHOOK);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

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
            throws ChatBackendConnector.AiControllerException, DatabaseException,
                IOException, WebHooks.WebHookException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = null;
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeDatabaseAi.getWebHook(any(), any())).thenReturn(wh);
        when(this.fakeWebHooks.getWebHookForIntent(any(), any())).thenReturn(VALID_WEBHOOK);
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any())).thenReturn(wr);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));
        Assert.assertFalse(mi.isFulfilled());
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(INTENTRESPONSE, result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    /***
     * Test that if an inactive WebHook exists, it is not executed.
     */
    @Test
    public void testChat_inactiveWebHookIgnored()
            throws ChatBackendConnector.AiControllerException, DatabaseException,
                IOException, WebHooks.WebHookException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeWebHooks.getWebHookForIntent(any(), any())).thenReturn(new WebHook(AIID, "intent", "endpoint", false));
        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

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
            throws ChatBackendConnector.AiControllerException, DatabaseException, IOException,
            WebHooks.WebHookException, ChatLogic.IntentException {

        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any()))
                .thenThrow(new WebHooks.WebHookExternalException("It went wrong"));
        ApiChat result = (ApiChat) chatGetWebhook();
        Assert.assertEquals(INTENTRESPONSE, result.getResult().getAnswer());
    }

    @Test
    public void testChat_webHookInternalFail()
            throws ChatBackendConnector.AiControllerException, DatabaseException,
                IOException, WebHooks.WebHookException, ChatLogic.IntentException {

        when(this.fakeWebHooks.executeIntentWebHook(any(), any(), any(), any()))
                .thenThrow(new WebHooks.WebHookInternalException("It went wrong internally"));
        Assert.assertTrue(chatGetWebhook() instanceof ApiError);
    }

    private ApiResult chatGetWebhook() throws ChatBackendConnector.AiControllerException,
            DatabaseException, ChatLogic.IntentException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(this.fakeWebHooks.getWebHookForIntent(any(), any())).thenReturn(VALID_WEBHOOK);
        setupFakeChat(0.7d,
                MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList(INTENTRESPONSE));

        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));
        return getChat(0.5f);
    }

}
