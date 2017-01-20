package com.hutoma.api.logic;

import com.hutoma.api.common.ChatTelemetryLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

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
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String SEMANTICRESULT = "semanticresult";
    private static final String NEURALRESULT = "neuralresult";
    private static final String AIMLRESULT = "aimlresult";
    private static final String QUESTION = "question";
    private static final String MEMORY_VARIABLE_PROMPT = "prompt1";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";

    private SecurityContext fakeContext;
    private AIChatServices fakeChatServices;
    private ChatLogic chatLogic;
    private IEntityRecognizer fakeRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;
    private ChatTelemetryLogger fakeChatTelemetryLogger;

    @Before
    public void setup() {
        Config fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeChatServices = mock(AIChatServices.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeChatTelemetryLogger = mock(ChatTelemetryLogger.class);
        this.chatLogic = new ChatLogic(fakeConfig, mock(JsonSerializer.class), this.fakeChatServices, mock(Tools.class),
                mock(ILogger.class), this.fakeIntentHandler, this.fakeRecognizer, this.fakeChatTelemetryLogger);
    }

    /***
     * Valid semantic response.
     */
    @Test
    public void testChat_Valid_Semantic() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid aiml net response.
     */
    @Test
    public void testChat_Valid_Aiml() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIMLRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid neural net response.
     */
    @Test
    public void testChat_Valid_Rnn() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_ErrorSemantic() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_ErrorAiml() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitAiml()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_ErrorRnn() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Check that whitespaces are removed from each end
     */
    @Test
    public void testChat_Semantic_Trimmed() throws ServerConnector.AiServicesException {
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
    public void testChat_Aiml_Trimmed() throws ServerConnector.AiServicesException {
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
    public void testChat_Rnn_Trimmed() throws ServerConnector.AiServicesException {
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
    public void testChat_Wnet_AiNotFound() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(new AIChatServices.AiNotFoundException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet is not found.
     */
    @Test
    public void testChat_Rnn_AiNotFound() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(new AIChatServices.AiNotFoundException("test"));
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server throws generic exception.
     */
    @Test
    public void testChat_Wnet_AiException() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitWnet()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, aiml throws generic exception.
     */
    @Test
    public void testChat_Aiml_AiException() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitAiml()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet throws generic exception.
     */
    @Test
    public void testChat_Rnn_AiException() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(new ServerConnector.AiServicesException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training)
     * but the semantic server is confident enough to reply.
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus_SemanticOverride() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(new AIChatServices.AiRejectedStatusException("test"));
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training).
     * The semantic server has no confidence so we expect an error 400
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        when(this.fakeChatServices.awaitRnn()).thenThrow(new AIChatServices.AiRejectedStatusException("test"));
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    /***
     * History is passed back to the user when semantic server wins
     */
    @Test
    public void testChat_History_Semantic() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getHistory());
    }

    /***
     * No history is passed back to the user when AIML server wins
     */
    @Test
    public void testChat_History_Aiml_AlwaysEmpty() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.5d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
    }

    /***
     * No history is passed back to the user when RNN server wins
     */
    @Test
    public void testChat_History_Rnn_AlwaysEmpty() throws ServerConnector.AiServicesException {
        setupFakeChat(0.7d, SEMANTICRESULT, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
        ApiResult result = getChat(0.9f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
    }

    /***
     * Reset command is processed and removed when text is at the beginning of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Pre() throws ServerConnector.AiServicesException {
        historySemanticReset(SEMANTICRESULT + HISTORY_REST_DIRECTIVE);
    }

    /***
     * Reset command is processed and removed when text is in the middle of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Mid() throws ServerConnector.AiServicesException {
        historySemanticReset(SEMANTICRESULT.substring(0, 3) + HISTORY_REST_DIRECTIVE + SEMANTICRESULT.substring(3));
    }

    /***
     * Reset command is processed and removed when text is at the end of the string
     */
    @Test
    public void testChat_History_Semantic_Reset_Post() throws ServerConnector.AiServicesException {
        historySemanticReset(HISTORY_REST_DIRECTIVE + SEMANTICRESULT);
    }

    /***
     * Semantic server sends reset command. History is cleared but if neuralnet wins the confidence contest then neuralnet response is returned unmodified.
     */
    @Test
    public void testChat_History_Semantic_Reset_NeuralNet_Wins() throws ServerConnector.AiServicesException {
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
    public void testChat_History_NeuralNet_Reset_Ignored() throws ServerConnector.AiServicesException {
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
    public void testChat_IntentRecognized() throws ServerConnector.AiServicesException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, "@meta.intent." + intentName, 0.0d, AIMLRESULT, 0.3d, NEURALRESULT);
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
    public void testChat_IntentFulfilled() throws ServerConnector.AiServicesException {
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
    }

    /***
     * Memory intent updates prompt when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt() throws ServerConnector.AiServicesException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is the prompt
        Assert.assertEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is incremented
        Assert.assertEquals(1, mi.getVariables().get(0).getTimesPrompted());
    }

    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_exceededPrompts() throws ServerConnector.AiServicesException {
        MemoryIntent mi = getMemoryIntentForPrompt(0, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is not incremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesPrompted());
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_fulfillFromUserQuestion() throws ServerConnector.AiServicesException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), "value"));
        }};
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(entities);
        Assert.assertFalse(mi.isFulfilled());
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(mi.isFulfilled());
        verify(this.fakeIntentHandler).updateStatus(mi);
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfulfilledVar_variableWithNoPrompt() throws ServerConnector.AiServicesException {
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
    public void testChat_IntentPrompt_NoPromptWhenZero() throws ServerConnector.AiServicesException {
        MemoryIntent mi = getMemoryIntentForPrompt(1, "currentValue");
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is decremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesPrompted());
    }

    private void historySemanticReset(String resetCommand) throws ServerConnector.AiServicesException {
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
        return this.chatLogic.chat(this.fakeContext, AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
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
                               double rnnConfidence, String rnnResponse) throws ServerConnector.AiServicesException {
        ChatResult wnetResult = new ChatResult();
        wnetResult.setScore(wnetConfidence);
        wnetResult.setAnswer(wnetResponse);
        when(this.fakeChatServices.awaitWnet()).thenReturn(wnetResult);

        ChatResult aimlResult = new ChatResult();
        aimlResult.setScore(aimlConfidence);
        aimlResult.setAnswer(aimlResponse);
        when(this.fakeChatServices.awaitAiml()).thenReturn(aimlResult);

        ChatResult rnnResult = new ChatResult();
        rnnResult.setScore(rnnConfidence);
        rnnResult.setAnswer(rnnResponse);
        when(this.fakeChatServices.awaitRnn()).thenReturn(rnnResult);
    }

    private MemoryIntent getMemoryIntentForPrompt(int maxPrompts, String currentValue) throws ServerConnector.AiServicesException {
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

