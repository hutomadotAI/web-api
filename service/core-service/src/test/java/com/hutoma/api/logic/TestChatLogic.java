package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.IChatHandler;
import com.hutoma.api.memory.ChatStateHandler;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestChatLogic extends TestChatBase {

    /***
     * Valid semantic response.
     */
    @Test
    public void testChat_Valid_Semantic() throws ChatBackendConnector.AiControllerException {
        final ChatResult chatResult = buildChatResult();
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(ImmutableMap.of(chatResult.getAiid(), chatResult));
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(chatResult.getAnswer(), ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid aiml net response.
     */
    @Test
    public void testChat_Valid_Aiml() throws Exception {
        final ChatResult chatResult = buildChatResult();
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(ImmutableMap.of(chatResult.getAiid(), chatResult));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(chatResult.getAnswer(), ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_ErrorSemantic() throws Exception {
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenThrow(ChatBackendConnector.AiControllerException.class);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_ErrorAiml() throws ChatBackendConnector.AiControllerException {
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenThrow(ChatBackendConnector.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Semantic_Trimmed() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, " " + SEMANTICRESULT + "\n",
                0.0d, AIMLRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Aiml_Trimmed() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT,
                0.5d, " " + AIMLRESULT + "\n");
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIMLRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Semantic server does not find AI.
     */
    @Test
    public void testChat_Emb_AiNotFound() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenThrow(ChatBackendConnector.AiNotFoundException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server throws generic exception.
     */
    @Test
    public void testChat_Emb_AiException() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenThrow(ChatBackendConnector.AiControllerException.class);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, aiml throws generic exception.
     */
    @Test
    public void testChat_Aiml_AiException() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT);
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenThrow(ChatBackendConnector.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * History is passed back to the user when semantic server wins
     */
    @Test
    public void testChat_History_Semantic() throws ChatBackendConnector.AiControllerException {
        String historyValue = "History is made now";
        setupFakeChatWithHistory(0.7d, SEMANTICRESULT, historyValue, 0.5d, AIMLRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(historyValue, ((ApiChat) result).getResult().getHistory());
    }

    /***
     * No history is passed back to the user when AIML server wins
     */
    @Test
    public void testChat_History_Aiml_AlwaysEmpty() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
    }

    /*
     * Tests for correct passthrough on bots that provide a valid url.
     * @throws RequestBase.AiControllerException.
     */
    @Test
    public void testChat_botPassthrough() throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException,
            ChatLogic.ChatFailedException {
        String passthroughResponse = "different message.";
        WebHookResponse response = new WebHookResponse(passthroughResponse);

        when(this.fakeChatServices.getAIPassthroughUrl(any(), any())).thenReturn("http://localhost:80");
        when(this.fakeWebHooks.executePassthroughWebhook(any(), any(), any())).thenReturn(response);

        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT);
        ApiResult result = getChat(0.2f);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(passthroughResponse, ((ApiChat) result).getResult().getAnswer());
    }

    /*
     * Tests that passthrough is ignored with an empty string url.
     * @throws RequestBase.AiControllerException.
     */
    @Test
    public void testChat_botPassthroughIgnored() throws ChatBackendConnector.AiControllerException, WebHooks.WebHookException,
            ChatLogic.ChatFailedException {
        String passthroughResponse = "won't see this";
        WebHookResponse response = new WebHookResponse(passthroughResponse);

        when(this.fakeChatServices.getAIPassthroughUrl(any(), any())).thenReturn(null);
        when(this.fakeWebHooks.executePassthroughWebhook(any(), any(), any())).thenReturn(response);
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT);
        ApiResult result = getChat(0.2f);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_botPassthrough_invalidAiidForUser() throws ChatLogic.ChatFailedException {
        when(this.fakeChatServices.getAIPassthroughUrl(any(), any())).thenThrow(new ChatLogic.ChatFailedException(ApiError.getNotFound()));
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testChat_noAiml_embNotConfident() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(TestDataHelper.DEFAULT_CHAT_RESPONSE, result.getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_embNull() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(null);
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(TestDataHelper.DEFAULT_CHAT_RESPONSE, result.getResult().getAnswer());
    }


    @Test
    public void testChat_AimlNotConf_embNotConfident() throws ChatBackendConnector.AiControllerException {
        setupFakeChat(0.0d, "emb", 0.0d, "aiml");
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(TestDataHelper.DEFAULT_CHAT_RESPONSE, result.getResult().getAnswer());
    }

    @Test
    public void testChat_notReadyToChat() throws ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException, NoServerAvailableException {
        setupFakeChat(0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiNotReadyToChat.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_servicesException() throws ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException, NoServerAvailableException {
        setupFakeChat(0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiServicesException.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_genericException() throws ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException, NoServerAvailableException {
        setupFakeChat(0.0d, "", 0.0d, "");
        doThrow(Exception.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_IntentError() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any()))
                .thenThrow(new ChatLogic.IntentException("test"));
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_botAffinity_noBots_embWins() throws ChatBackendConnector.AiControllerException {
        final String response = "emb";
        setupFakeChat(0.2d, response, 0.0d, "");
        ApiChat result = (ApiChat) getChat(0.1f);
        Assert.assertEquals(response, result.getResult().getAnswer());
    }

    @Test
    public void testChat_botAffinity_noBots_stateHasUnknownLockedAiid() throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException {
        final String response = "emb";
        setupFakeChat(0.2d, response, 0.0d, "");
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext()));
        ApiChat result = (ApiChat) getChat(0.1f);
        // Verify we still get the answer from EMB and it doesn't try to get it from the invalid bot
        Assert.assertEquals(response, result.getResult().getAnswer());
    }

    @Test
    public void testChat_botAffinity_bots_noPreviousLock_lockToHighestBot() throws ChatBackendConnector.AiControllerException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.4);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_stillLockedEvenWithOtherHigherConfidence() throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_lowConfidenceSwitchToHigherConfidenceBot() throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);
        validateStateSaved(cr2, cr2Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_allLowConfidence() throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException {
        ChatResult cr1 = new ChatResult("question");
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult("question");
        cr2.setScore(0.3);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);
        ChatResult cr1Aiml = new ChatResult("question");
        cr1Aiml.setScore(0.6);
        ChatResult cr2Aiml = new ChatResult("question");
        cr2Aiml.setScore(0.7);
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(new HashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1Aiml);
            put(cr2Uuid, cr2Aiml);
        }});
        // We now expect to get the AIML one with the highest score
        validateStateSaved(cr2Aiml, cr2Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_emb_aiml_score_order() throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException {
        // BOT1 has higher score in EMB
        ChatResult cr1 = new ChatResult("question");
        cr1.setScore(0.3);
        ChatResult cr2 = new ChatResult("question");
        cr2.setScore(0.2);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);

        // BOT2 has higher score in AIML
        ChatResult cr1Aiml = new ChatResult("question");
        cr1Aiml.setScore(0.6);
        ChatResult cr2Aiml = new ChatResult("question");
        cr2Aiml.setScore(0.7);
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(ImmutableMap.of(cr1Uuid, cr1Aiml, cr2Uuid, cr2Aiml));
        // We now expect to get the AIML one with the highest score
        validateStateSaved(cr2Aiml, cr2Uuid);
    }

    @Test
    public void testChat_linkedBots_allUnderMinP_noAnswer() throws ChatBackendConnector.AiControllerException {
        final double minP1 = 0.7;
        final double minP2 = 0.8;
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(minP1 - 0.1);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(minP2 - 0.1);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> results = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, minP1, cr2Uuid, minP2));
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(results);
        ApiChat result = (ApiChat) getChat(0.0);
        Assert.assertEquals(0.0, result.getResult().getScore(), 0.0001);
        Assert.assertEquals(TestDataHelper.DEFAULT_CHAT_RESPONSE, result.getResult().getAnswer());
    }

    @Test
    public void testChat_handedOver_fromPreviousState() throws ChatBackendConnector.AiControllerException, ChatStateHandler.ChatStateException {
        final ChatState state = ChatState.getEmpty();
        state.setChatTarget(ChatHandoverTarget.Human);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        ApiChat result = (ApiChat) getChat(0.0);
        Assert.assertEquals(ChatHandoverTarget.Human.getStringValue(), result.getResult().getChatTarget());
        Assert.assertEquals(1.0, result.getResult().getScore(), 0.00001);
        Assert.assertNull(result.getResult().getAnswer());
        // We never make any backend requests
        verify(this.fakeChatServices, never()).awaitBackend(BackendServerType.EMB);
        verify(this.fakeChatServices, never()).awaitBackend(BackendServerType.AIML);
        // We don't process any intents
        verify(this.fakeIntentHandler, never()).getIntent(any(), anyString());
    }

    @Test
    public void testChat_handedOver_toHuman() throws ChatStateHandler.ChatStateException {
        final ChatState state = ChatState.getEmpty();
        state.setChatTarget(ChatHandoverTarget.Ai);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        ArgumentCaptor<ChatState> argument = ArgumentCaptor.forClass(ChatState.class);
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeChatStateHandler).saveState(any(), any(), any(), argument.capture());
        Assert.assertEquals(ChatHandoverTarget.Human, argument.getValue().getChatTarget());
    }

    @Test
    public void testChat_handedOver_toAi() throws ChatStateHandler.ChatStateException {
        final ChatState state = ChatState.getEmpty();
        state.setChatTarget(ChatHandoverTarget.Human);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        ArgumentCaptor<ChatState> argument = ArgumentCaptor.forClass(ChatState.class);
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Ai);
        verify(this.fakeChatStateHandler).saveState(any(), any(), any(), argument.capture());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatHandoverTarget.Ai, argument.getValue().getChatTarget());
    }

    @Test
    public void testChat_handedOver_sameTarget() throws ChatStateHandler.ChatStateException {
        final ChatState state = ChatState.getEmpty();
        state.setChatTarget(ChatHandoverTarget.Human);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_handedOver_getCurrentState_chatStateException_dueToUser()
            throws ChatStateHandler.ChatStateException {
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenThrow(ChatStateHandler.ChatStateUserException.class);
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_handedOver_getCurrentState_chatStateException_otherReason()
            throws ChatStateHandler.ChatStateException {
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenThrow(Exception.class);
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_handedOver_saveNewState_chatStateException_dueToUser()
            throws ChatStateHandler.ChatStateException {
        doThrow(ChatStateHandler.ChatStateUserException.class).when(this.fakeChatStateHandler).saveState(any(), any(), any(), any());
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_handedOver_saveNewState_chatStateException_otherReason()
            throws ChatStateHandler.ChatStateException {
        doThrow(Exception.class).when(this.fakeChatStateHandler).saveState(any(), any(), any(), any());
        ApiResult result = this.chatLogic.handOver(AIID, DEVID_UUID, CHATID.toString(), ChatHandoverTarget.Human);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChatWorkflow_noHandlers() {
        when(this.fakeChatWorkflow.getHandlers()).thenReturn(Collections.emptyList());
        ApiResult result = getChat(0.0);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChatWorkflow_noDefaultHandler() {
        IChatHandler other = buildChatHandler("answer1", false);
        when(this.fakeChatWorkflow.getHandlers()).thenReturn(Collections.singletonList(other));
        ApiResult result = getChat(0.0);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChatWorkflow_fallbackNextHandler() {
        final String responseLastHandler = "response2";
        IChatHandler other = buildChatHandler("answer1", false);
        IChatHandler last = buildChatHandler(responseLastHandler, true);
        when(this.fakeChatWorkflow.getHandlers()).thenReturn(Arrays.asList(other, last));
        ApiChat result = (ApiChat) getChat(0.0);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(responseLastHandler, result.getResult().getAnswer());
    }

    @Test
    public void testChatReset() throws ChatStateHandler.ChatStateException {
        ChatState state = ChatState.getEmpty();
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        ApiResult result = this.chatLogic.resetChat(DEVID_UUID, AIID, CHATID.toString());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testChat_setContextVariable()
            throws ChatStateHandler.ChatStateException {
        Map<String, String> variables = new HashMap<String, String>(){{ put("variable1", "value1"); }};
        ApiResult result = this.chatLogic.setContextVariable(AIID, DEVID_UUID, CHATID.toString(), variables);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeChatStateHandler).saveState(any(), any(), any(), any());
    }

    @Test
    public void testChat_setContextVariable_Multiple()
            throws ChatStateHandler.ChatStateException {
        Map<String, String> variables = new HashMap<String, String>(){{
            put("variable1", "value1");
            put("variable2", "value2"); }};
        ApiResult result = this.chatLogic.setContextVariable(AIID, DEVID_UUID, CHATID.toString(), variables);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeChatStateHandler).saveState(any(), any(), any(), any());
    }

    @Test
    public void testChat_setContextVariable_MissingValue()
            throws ChatStateHandler.ChatStateException {
        Map<String, String> variables = new HashMap<String, String>(){{
            put("variable1", "value1");
            put("variable2", ""); }};
        ApiResult result = this.chatLogic.setContextVariable(AIID, DEVID_UUID, CHATID.toString(), variables);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeChatStateHandler, never()).saveState(any(), any(), any(), any());
    }

    @Test
    public void testChat_setContextVariable_EmptyMap()
            throws ChatStateHandler.ChatStateException {
        Map<String, String> variables = new HashMap<String, String>();
        ApiResult result = this.chatLogic.setContextVariable(AIID, DEVID_UUID, CHATID.toString(), variables);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeChatStateHandler, never()).saveState(any(), any(), any(), any());
    }

    @Test
    public void testChat_setContextVariable_NullMap()
            throws ChatStateHandler.ChatStateException {
        Map<String, String> variables = null;
        ApiResult result = this.chatLogic.setContextVariable(AIID, DEVID_UUID, CHATID.toString(), variables);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeChatStateHandler, never()).saveState(any(), any(), any(), any());
    }

    private IChatHandler buildChatHandler(final String response, final boolean complete) {
        return new IChatHandler() {
            @Override
            public ChatResult doWork(final ChatRequestInfo requestInfo, final ChatResult currentResult, final LogMap telemetryMap) {
                ChatResult r = new ChatResult("query");
                r.setAnswer(response);
                return r;
            }

            @Override
            public boolean chatCompleted() {
                return complete;
            }
        };
    }

    private ChatResult buildChatResult() {
        ChatResult chatResult = new ChatResult("question");
        chatResult.setAnswer("my answer");
        chatResult.setScore(0.7);
        chatResult.setAiid(AIID);
        ChatState chatState = ChatState.getEmpty();
        chatState.setAiChatServices(this.fakeChatServices);
        chatResult.setChatState(chatState);
        return chatResult;
    }
}
