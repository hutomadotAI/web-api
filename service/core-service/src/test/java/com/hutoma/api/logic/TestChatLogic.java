package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.controllers.ServerMetadata;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class TestChatLogic extends TestChatBase {

    /***
     * Valid semantic response.
     */
    @Test
    public void testChat_Valid_Semantic() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid aiml net response.
     */
    @Test
    public void testChat_Valid_Aiml() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIMLRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid neural net response.
     */
    @Test
    public void testChat_Valid_Rnn() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_ErrorSemantic() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_ErrorAiml() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitAiml()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Semantic_Trimmed() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, " " + SEMANTICRESULT + "\n",
                0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Aiml_Trimmed() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT,
                0.5d, " " + AIMLRESULT + "\n",
                0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIMLRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Rnn_Trimmed() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT,
                0.0d, AIMLRESULT,
                0.3d, " " + NEURALRESULT + "\n");
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Semantic server does not find AI.
     */
    @Test
    public void testChat_Wnet_AiNotFound() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(RequestBase.AiNotFoundException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet is not found.
     */
    @Test
    public void testChat_Rnn_AiNotFound() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(RequestBase.AiNotFoundException.class);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server throws generic exception.
     */
    @Test
    public void testChat_Wnet_AiException() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, aiml throws generic exception.
     */
    @Test
    public void testChat_Aiml_AiException() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitAiml()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet throws generic exception.
     */
    @Test
    public void testChat_Rnn_AiException() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training)
     * but the semantic server is confident enough to reply.
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus_SemanticOverride() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training).
     * The semantic server has no confidence so we fallback to AIML
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setAnswer(null);
        Map<UUID, ChatResult> map = new HashMap<UUID, ChatResult>() {{
            this.put(AIID, chatResult);
        }};
        when(this.fakeChatServices.awaitRnn()).thenReturn(map);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    /***
     * The neural network can't be queried because the controller threw an exception.
     */
    @Test
    public void testChat_RejectedNeuralDueToException() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(RequestBase.AiControllerException.class);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * History is passed back to the user when semantic server wins
     */
    @Test
    public void testChat_History_Semantic() throws RequestBase.AiControllerException {
        String historyValue = "History is made now";
        setupFakeChatWithHistory(0.7d, SEMANTICRESULT, historyValue, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(historyValue, ((ApiChat) result).getResult().getHistory());
    }

    /***
     * No history is passed back to the user when AIML server wins
     */
    @Test
    public void testChat_History_Aiml_AlwaysEmpty() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
    }

    /***
     * No history is passed back to the user when RNN server wins
     */
    @Test
    public void testChat_History_Rnn_AlwaysEmpty() throws RequestBase.AiControllerException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
    }

    /*
         * Tests for correct passthrough on bots that provide a valid url.
         * @throws RequestBase.AiControllerException.
         */
    @Test
    public void testChat_botPassthrough() throws RequestBase.AiControllerException, WebHooks.WebHookException {
        String passthroughResponse = "different message.";
        WebHookResponse response = new WebHookResponse(passthroughResponse);

        when(this.fakeChatServices.getAIPassthroughUrl(any(), any())).thenReturn("http://localhost:80");
        when(this.fakeWebHooks.executePassthroughWebhook(any(), any(), any())).thenReturn(response);

        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(passthroughResponse, ((ApiChat) result).getResult().getAnswer());
    }

    /*
        * Tests that passthrough is ignored with an empty string url.
        * @throws RequestBase.AiControllerException.
        */
    @Test
    public void testChat_botPassthroughIgnored() throws RequestBase.AiControllerException, WebHooks.WebHookException {
        String passthroughResponse = "won't see this";
        WebHookResponse response = new WebHookResponse(passthroughResponse);

        when(this.fakeChatServices.getAIPassthroughUrl(any(), any())).thenReturn(null);
        when(this.fakeWebHooks.executePassthroughWebhook(any(), any(), any())).thenReturn(response);
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Tests for a valid semantic response from assistant.
     * @throws ServerConnector.AiServicesException
     */
    @Test
    public void testAssistant_Valid_Semantic() throws ServerConnector.AiServicesException, WebHooks.WebHookException {
        ApiResult result = getAssistantChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ASSISTANTRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_noRnn_wnetNotConfident() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitAiml()).thenReturn(null);
        when(this.fakeChatServices.awaitRnn()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_noRnn_wnetNull() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitWnet()).thenReturn(null);
        when(this.fakeChatServices.awaitAiml()).thenReturn(null);
        when(this.fakeChatServices.awaitRnn()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_rnnEmpty_wnetNotConfident() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitAiml()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_AimlNotConf_rnnNull_wnetNotConfident() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "wnet", 0.0d, "aiml", 0.0d, null);
        when(this.fakeChatServices.awaitRnn()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_notReadyToChat() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiNotReadyToChat.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_servicesException() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiServicesException.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_genericException() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(Exception.class)
                .when(this.fakeChatServices).startChatRequests(any(), any(), any(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_botAffinity_noBots_wnetWins() throws RequestBase.AiControllerException {
        final String response = "wnet";
        setupFakeChat(0.2d, response, 0.0d, "", 0.0d, "");
        ApiChat result = (ApiChat) getChat(0.1f);
        Assert.assertEquals(response, result.getResult().getAnswer());
    }

    @Test
    public void testChat_botAffinity_noBots_stateHasUnknownLockedAiid() throws RequestBase.AiControllerException {
        final String response = "wnet";
        setupFakeChat(0.2d, response, 0.0d, "", 0.0d, "");
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<String, String>(), 0.1d));
        ApiChat result = (ApiChat) getChat(0.1f);
        // Verify we still get the answer from WNET and it doesn't try to get it from the invalid bot
        Assert.assertEquals(response, result.getResult().getAnswer());
    }

    @Test
    public void testChat_botAffinity_bots_noPreviousLock_lockToHighestBot() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.4);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_stillLockedEvenWithOtherHigherConfidence() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_lowConfidenceSwitchToHigherConfidenceBot() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr2, cr2Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_allLowConfidence() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult("question");
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult("question");
        cr2.setScore(0.3);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        ChatResult cr1Aiml = new ChatResult("question");
        cr1Aiml.setScore(0.6);
        ChatResult cr2Aiml = new ChatResult("question");
        cr2Aiml.setScore(0.7);
        when(this.fakeChatServices.awaitAiml()).thenReturn(new HashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1Aiml);
            put(cr2Uuid, cr2Aiml);
        }});
        // We now expect to get the AIML one with the highest score
        validateStateSaved(cr2Aiml, cr2Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_wnet_aiml_score_order() throws RequestBase.AiControllerException {
        // BOT1 has higher score in WNET
        ChatResult cr1 = new ChatResult("question");
        cr1.setScore(0.3);
        ChatResult cr2 = new ChatResult("question");
        cr2.setScore(0.2);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        ChatState initialChatState = new ChatState(DateTime.now(), null, null, cr1Uuid, new HashMap<>(), 0.5d);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, 0.5, cr2Uuid, 0.5));
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);

        // BOT2 has higher score in AIML
        ChatResult cr1Aiml = new ChatResult("question");
        cr1Aiml.setScore(0.6);
        ChatResult cr2Aiml = new ChatResult("question");
        cr2Aiml.setScore(0.7);
        when(this.fakeChatServices.awaitAiml()).thenReturn(ImmutableMap.of(cr1Uuid, cr1Aiml, cr2Uuid, cr2Aiml));
        // We now expect to get the AIML one with the highest score
        validateStateSaved(cr2Aiml, cr2Uuid);
    }

    @Test
    public void testChat_linkedBots_allUnderMinP_noAnswer() throws RequestBase.AiControllerException {
        final double minP1 = 0.7;
        final double minP2 = 0.8;
        ChatResult cr1 = new ChatResult("Hi");
        cr1.setScore(minP1 - 0.1);
        ChatResult cr2 = new ChatResult("Hi2");
        cr2.setScore(minP2 - 0.1);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = ImmutableMap.of(cr1Uuid, cr1, cr2Uuid, cr2);
        when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(cr1Uuid, minP1, cr2Uuid, minP2));
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        ApiChat result = (ApiChat) getChat(0.0);
        Assert.assertEquals(0.0, result.getResult().getScore(), 0.0001);
        Assert.assertEquals(ChatLogic.COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }
}