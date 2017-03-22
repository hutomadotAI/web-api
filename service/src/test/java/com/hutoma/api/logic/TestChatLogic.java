package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.controllers.ServerMetadata;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 08/08/2016.
 */
public class TestChatLogic {

    private static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    private static final UUID AIML_BOT_AIID = UUID.fromString("bd2700ff-279b-4bac-ad2f-85a5275ac073");
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String SEMANTICRESULT = "semanticresult";
    private static final String ASSISTANTRESULT = "Hello";
    private static final String NEURALRESULT = "neuralresult";
    private static final String AIMLRESULT = "aimlresult";
    private static final String QUESTION = "question";
    private static final String MEMORY_VARIABLE_PROMPT = "prompt1";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";
    private static final String COMPLETELY_LOST_RESULT = "Erm... What?";

    private AIChatServices fakeChatServices;
    private ChatLogic chatLogic;
    private IEntityRecognizer fakeRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;
    private ChatLogger fakeChatTelemetryLogger;
    private WebHooks fakeWebHooks;
    private Database fakeDatabase;
    private ChatStateHandler fakeChatStateHandler;
    private Config fakeConfig;

    @Before
    public void setup() {
        Config fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeChatServices = mock(AIChatServices.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeChatTelemetryLogger = mock(ChatLogger.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeChatStateHandler = mock(ChatStateHandler.class);
        this.fakeConfig = mock(Config.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.chatLogic = new ChatLogic(fakeConfig, mock(JsonSerializer.class), this.fakeChatServices, mock(Tools.class),
                mock(ILogger.class), this.fakeIntentHandler, this.fakeRecognizer, this.fakeChatTelemetryLogger, this.fakeWebHooks,
                this.fakeChatStateHandler);

        when(this.fakeChatStateHandler.getState(anyString(), any())).thenReturn(ChatState.getEmpty());
    }

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
        ChatResult chatResult = new ChatResult();
        chatResult.setAnswer(null);
        Map<UUID, ChatResult> map = new HashMap<UUID, ChatResult>() {{
            this.put(AIID, chatResult);
        }};
        when(this.fakeChatServices.awaitRnn()).thenReturn(map);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIMLRESULT, result.getResult().getAnswer());
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
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getHistory());
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

    /***
     * Reset command is processed and removed when text is at the beginning of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Pre() throws RequestBase.AiControllerException {
        historySemanticReset(SEMANTICRESULT + HISTORY_REST_DIRECTIVE);
    }

    /***
     * Reset command is processed and removed when text is in the middle of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Mid() throws RequestBase.AiControllerException {
        historySemanticReset(SEMANTICRESULT.substring(0, 3) + HISTORY_REST_DIRECTIVE + SEMANTICRESULT.substring(3));
    }

    /***
     * Reset command is processed and removed when text is at the end of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Post() throws RequestBase.AiControllerException {
        historySemanticReset(HISTORY_REST_DIRECTIVE + SEMANTICRESULT);
    }

    /***
     * Semantic server sends reset command. History is cleared but if neuralnet wins the confidence contest then neuralnet response is returned unmodified.
     */
    @Test
    public void testChat_History_Semantic_Reset_NeuralNet_Wins() throws
            RequestBase.AiControllerException {
        setupFakeChat(0.7d, HISTORY_REST_DIRECTIVE + SEMANTICRESULT,
                0.0d, AIMLRESULT,
                0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Semantic server sends reset command. History is cleared but if neuralnet wins the confidence contest then neuralnet response is returned unmodified.
     */
    @Test
    public void testChat_History_NeuralNet_Reset_Ignored() throws
            RequestBase.AiControllerException {
        String neuralResetCommand = NEURALRESULT + HISTORY_REST_DIRECTIVE;
        setupFakeChat(0.7d, HISTORY_REST_DIRECTIVE + SEMANTICRESULT,
                0.0d, AIMLRESULT,
                0.3d, neuralResetCommand);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(neuralResetCommand, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Tests an intent is recognized by the API when the backend sends it.
     */
    @Test
    public void testChat_IntentRecognized() throws RequestBase.AiControllerException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(miList);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        MemoryIntent ri = r.getIntents().get(0);
        Assert.assertEquals(intentName, ri.getName());
    }

    /***
     * Memory intent is fulfilled.
     */
    @Test
    public void testChat_IntentFulfilled() throws RequestBase.AiControllerException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));
        Assert.assertFalse(mi.isFulfilled());
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertTrue(mi.isFulfilled());
        Assert.assertEquals(1, result.getResult().getIntents().size());
        Assert.assertTrue(result.getResult().getIntents().get(0).isFulfilled());
        verify(this.fakeIntentHandler).clearIntents(any());
    }

    /***
     * Memory intent updates prompt when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt() throws RequestBase.AiControllerException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is the prompt
        Assert.assertEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is incremented
        Assert.assertEquals(1, mi.getVariables().get(0).getTimesPrompted());
        verify(this.fakeIntentHandler, never()).clearIntents(any());
    }

    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_exceededPrompts() throws
            RequestBase.AiControllerException {
        MemoryIntent mi = getMemoryIntentForPrompt(0, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is not incremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesPrompted());
        verify(this.fakeIntentHandler, never()).clearIntents(any());
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfullfilledVar_fulfillFromUserQuestion()
            throws RequestBase.AiControllerException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), "value"));
        }};
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(entities);
        Assert.assertFalse(mi.isFulfilled());
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(mi.isFulfilled());
        verify(this.fakeIntentHandler).clearIntents(any());
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfulfilledVar_variableWithNoPrompt()
            throws RequestBase.AiControllerException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        mi.getVariables().get(0).setPrompts(new ArrayList<>());
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(new ArrayList<>());
        Assert.assertFalse(mi.isFulfilled());
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
        Assert.assertFalse(mi.isFulfilled());
        verify(this.fakeIntentHandler, never()).updateStatus(mi);
    }

    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt_NoPromptWhenZero() throws RequestBase.AiControllerException {
        MemoryIntent mi = getMemoryIntentForPrompt(1, "currentValue");
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // And timesPrompted is decremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesPrompted());
    }

    /***
     * Memory intent is fulfilled after it's prompted once
     */
    @Test
    public void testChat_multiLineIntent_fulfilled()
            throws RequestBase.AiControllerException, Database.DatabaseException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, "prompt");

        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }
        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());
        // Verify answer is the prompt for the first variable
        Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        verify(this.fakeIntentHandler).updateStatus(mi);
        verify(this.fakeIntentHandler, never()).clearIntents(any());

        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any()))
                .thenReturn(Collections.singletonList(r.getIntents().get(0)));
        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(anyString(), any())).thenReturn(entities);
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        // Is fulfilled
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
        // Intent has the entity with currentValue set to what we've defined
        Assert.assertEquals(varValue, r.getIntents().get(0).getVariables().get(0).getCurrentValue());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        // Score is 1.0
        Assert.assertEquals(1.0d, r.getScore(), 0.00000001d);
        verify(this.fakeIntentHandler).updateStatus(mi);
        verify(this.fakeIntentHandler).clearIntents(any());
    }

    /***
     * Memory intent is not fulfilled after exhausting all prompts
     */
    @Test
    public void testChat_multiLineIntent_promptsExhausted()
            throws RequestBase.AiControllerException {
        final int maxPrompts = 3;
        MemoryIntent mi = getMemoryIntentForPrompt(maxPrompts, "prompt");
        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }

        // Exhaust prompts
        for (int n = 1; n <= maxPrompts; n++) {
            ApiResult result = getChat(0.5f, "nothing to see here.");
            ChatResult r = ((ApiChat) result).getResult();
            // Verify answer is intent prompt and intent is not fulfilled
            Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
            Assert.assertFalse(r.getIntents().get(0).isFulfilled());
            Assert.assertEquals(n, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
            Assert.assertEquals(maxPrompts, r.getIntents().get(0).getVariables().get(0).getTimesToPrompt());
        }

        // Next answer should exit intent handling and go through normal chat processing
        final String wnetAnswer = "wnet answer";
        when(this.fakeIntentHandler.parseAiResponseForIntent(anyString(), any(), any(), anyString())).thenReturn(null);
        ChatResult wnetResult = new ChatResult();
        wnetResult.setScore(0.9f);
        wnetResult.setAnswer(wnetAnswer);
        when(this.fakeChatServices.awaitWnet()).thenReturn(getChatResultMap(AIID, wnetResult));
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertNull(r.getIntents());
        Assert.assertEquals(wnetAnswer, r.getAnswer());
    }

    /***
     * Tests for a valid semantic response from assistant.
     * @throws ServerConnector.AiServicesException
     */
    @Test
    public void testAssistant_Valid_Semantic() throws ServerConnector.AiServicesException {
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
        Assert.assertEquals(COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_noRnn_wnetNull() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitWnet()).thenReturn(null);
        when(this.fakeChatServices.awaitAiml()).thenReturn(null);
        when(this.fakeChatServices.awaitRnn()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_noAiml_rnnEmpty_wnetNotConfident() throws RequestBase.AiControllerException {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        when(this.fakeChatServices.awaitAiml()).thenReturn(null);
        ApiChat result = (ApiChat) getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(COMPLETELY_LOST_RESULT, result.getResult().getAnswer());
    }

    @Test
    public void testChat_notReadyToChat() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiNotReadyToChat.class)
                .when(this.fakeChatServices).startChatRequests(anyString(), any(), any(), anyString(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testChat_servicesException() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(AIChatServices.AiServicesException.class)
                .when(this.fakeChatServices).startChatRequests(anyString(), any(), any(), anyString(), anyString(), any());
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_genericException() throws RequestBase.AiControllerException, ServerConnector.AiServicesException, ServerMetadata.NoServerAvailable {
        setupFakeChat(0.0d, "", 0.0d, "", 0.0d, "");
        doThrow(Exception.class)
                .when(this.fakeChatServices).startChatRequests(anyString(), any(), any(), anyString(), anyString(), any());
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
        when(this.fakeChatStateHandler.getState(anyString(), any())).thenReturn(new ChatState(DateTime.now(), null, UUID.randomUUID()));
        ApiChat result = (ApiChat) getChat(0.1f);
        // Verify we still get the answer from WNET and it doesn't try to get it from the invalid bot
        Assert.assertEquals(response, result.getResult().getAnswer());
    }

    @Test
    public void testChat_botAffinity_bots_noPreviousLock_lockToHighestBot() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult();
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult();
        cr2.setScore(0.4);
        UUID cr1Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = new HashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1);
            put(UUID.randomUUID(), cr2);
        }};
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_stillLockedEvenWithOtherHigherConfidence() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult();
        cr1.setScore(0.6);
        ChatResult cr2 = new ChatResult();
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = new HashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1);
            put(UUID.randomUUID(), cr2);
        }};
        ChatState initialChatState = new ChatState(DateTime.now(), null, cr1Uuid);
        when(this.fakeChatStateHandler.getState(anyString(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr1, cr1Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_lowConfidenceSwitchToHigherConfidenceBot() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult();
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult();
        cr2.setScore(0.9);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = new LinkedHashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1);
            put(cr2Uuid, cr2);
        }};
        ChatState initialChatState = new ChatState(DateTime.now(), null, cr1Uuid);
        when(this.fakeChatStateHandler.getState(anyString(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        validateStateSaved(cr2, cr2Uuid);
    }

    @Test
    public void testChat_botAffinity_bots_lockedToBot_allLowConfidence() throws RequestBase.AiControllerException {
        ChatResult cr1 = new ChatResult();
        cr1.setScore(0.2);
        ChatResult cr2 = new ChatResult();
        cr2.setScore(0.3);
        UUID cr1Uuid = UUID.randomUUID();
        UUID cr2Uuid = UUID.randomUUID();
        Map<UUID, ChatResult> wnetResults = new LinkedHashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1);
            put(cr2Uuid, cr2);
        }};
        ChatState initialChatState = new ChatState(DateTime.now(), null, cr1Uuid);
        when(this.fakeChatStateHandler.getState(anyString(), any())).thenReturn(initialChatState);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResults);
        ChatResult cr1Aiml = new ChatResult();
        cr1Aiml.setScore(0.6);
        ChatResult cr2Aiml = new ChatResult();
        cr2Aiml.setScore(0.7);
        when(this.fakeChatServices.awaitAiml()).thenReturn(new HashMap<UUID, ChatResult>() {{
            put(cr1Uuid, cr1Aiml);
            put(cr2Uuid, cr2Aiml);
        }});
        // We now expect to get the AIML one with the highest score
        validateStateSaved(cr2Aiml, cr2Uuid);
    }

    /***
     * Test that if an active WebHook exists, it is executed.
     */
    @Test
    public void testChat_webHookTriggered()
            throws RequestBase.AiControllerException, Database.DatabaseException, IOException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);
        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);
        when(this.fakeWebHooks.activeWebhookExists(any(), any())).thenReturn(true);
        when(this.fakeWebHooks.executeWebHook(any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

        Assert.assertFalse(mi.isFulfilled());

        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(webHookResponse, result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    /***
     * Test that if an inactive WebHook exists, it is not executed.
     */
    @Test
    public void testChat_inactiveWebHookIgnored()
            throws RequestBase.AiControllerException, Database.DatabaseException, IOException {
        final String intentName = "intent1";
        final String webHookResponse = "webhook executed";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        WebHookResponse wr = new WebHookResponse(webHookResponse);
        when(this.fakeWebHooks.activeWebhookExists(any(), any())).thenReturn(false);
        when(this.fakeWebHooks.executeWebHook(any(), any(), any())).thenReturn(wr);

        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any(), any())).thenReturn(intent);
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
            throws RequestBase.AiControllerException, Database.DatabaseException, IOException {
        final String intentName = "intent1";

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        when(this.fakeWebHooks.activeWebhookExists(any(), any())).thenReturn(true);
        when(this.fakeWebHooks.executeWebHook(any(), any(), any())).thenReturn(null);

        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

        Assert.assertFalse(mi.isFulfilled());

        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals("response", result.getResult().getAnswer());
        Assert.assertTrue(mi.isFulfilled());
    }

    private void validateStateSaved(final ChatResult returnedResult, final UUID usedAiid) {
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(returnedResult.getScore(), result.getResult().getScore(), 0.0001);
        ArgumentCaptor<ChatState> argumentCaptor = ArgumentCaptor.forClass(ChatState.class);
        verify(this.fakeChatStateHandler).saveState(anyString(), any(), argumentCaptor.capture());
        // And that the contains the lockedAiid value for the aiid with the highest score
        Assert.assertEquals(usedAiid, argumentCaptor.getValue().getLockedAiid());

    }

    private void historySemanticReset(String resetCommand) throws
            RequestBase.AiControllerException {
        setupFakeChat(0.7d, resetCommand, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.3f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    private ApiResult getChat(float min_p) {
        return this.getChat(min_p, QUESTION);
    }

    private ApiResult getChat(float min_p, String question) {
        return this.chatLogic.chat(AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
    }

    private ApiResult getAssistantChat(float min_p) {
        return this.getAssistantChat(min_p, QUESTION);
    }

    private ApiResult getAssistantChat(float min_p, String
            question) {
        return this.chatLogic.assistantChat(AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
    }

    /***
     * Sets up fake responses from the AiChatServices layer
     * @param wnetConfidence
     * @param wnetResponse
     * @param aimlConfidence
     * @param aimlResponse
     * @param rnnConfidence
     * @param rnnResponse
     * @throws ServerConnector.AiServicesException
     */
    private void setupFakeChat(double wnetConfidence, String wnetResponse,
                               double aimlConfidence, String aimlResponse,
                               double rnnConfidence, String rnnResponse) throws
            RequestBase.AiControllerException {

        ChatResult wnetResult = new ChatResult();
        wnetResult.setScore(wnetConfidence);
        wnetResult.setAnswer(wnetResponse);
        when(this.fakeChatServices.awaitWnet()).thenReturn(getChatResultMap(AIID, wnetResult));

        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID));
        when(this.fakeChatServices.getLinkedBotsAiids(anyString(), any())).thenReturn(Collections.singletonList(new Pair<>(DEVID, AIML_BOT_AIID)));
        ChatResult aimlResult = new ChatResult();
        aimlResult.setScore(aimlConfidence);
        aimlResult.setAnswer(aimlResponse);
        when(this.fakeChatServices.awaitAiml()).thenReturn(getChatResultMap(AIML_BOT_AIID, aimlResult));

        ChatResult rnnResult = new ChatResult();
        rnnResult.setScore(rnnConfidence);
        rnnResult.setAnswer(rnnResponse);
        when(this.fakeChatServices.awaitRnn()).thenReturn(getChatResultMap(AIID, rnnResult));
    }

    private Map<UUID, ChatResult> getChatResultMap(
            final UUID aiid, final ChatResult chatResult) {
        return new HashMap<UUID, ChatResult>() {{
            this.put(aiid, chatResult);
        }};
    }

    private MemoryIntent getMemoryIntentForPrompt(
            int maxPrompts, String currentValue) throws
            RequestBase.AiControllerException {
        final String intentName = "intent1";
        final String promptTrigger = "variableValue";
        final String prompt = "prompt1";
        MemoryVariable mv = new MemoryVariable(
                "var",
                currentValue,
                true,
                Arrays.asList(promptTrigger, "b"),
                Collections.singletonList(prompt),
                maxPrompts,
                0);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        setupFakeChat(0.9d, "@meta.intent." + intentName, 0.3d, "", 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(anyString(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

}

