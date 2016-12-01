package com.hutoma.api.logic;

import com.hutoma.api.common.ChatTelemetryLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 08/08/2016.
 */
public class TestChatLogic {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String SEMANTICRESULT = "semanticresult";
    private static final String NEURALRESULT = "neuralresult";
    private static final String RNN_NEURALRESULT = "0.9|" + NEURALRESULT;
    private static final String QUESTION = "question";
    private static final String MEMORY_VARIABLE_PROMPT = "prompt1";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";

    private SecurityContext fakeContext;
    private NeuralNet fakeNeuralNet;
    private SemanticAnalysis fakeSemanticAnalysis;
    private ChatLogic chatLogic;
    private IEntityRecognizer fakeRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;

    @Before
    public void setup() {
        Config fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.chatLogic = new ChatLogic(fakeConfig, mock(JsonSerializer.class), this.fakeSemanticAnalysis, this.fakeNeuralNet, new FakeTimerTools(),
                mock(ILogger.class), this.fakeIntentHandler, this.fakeRecognizer, mock(ChatTelemetryLogger.class));
    }

    /***
     * Valid semantic response.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_Valid_Semantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Valid neural net response.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_Valid_Neural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Empty semantic response, even with neural net response, sends error.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_EmptySemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Null semantic response, even with neural net response, sends error.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_NullSemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenThrow(
                new SemanticAnalysis.SemanticAnalysisException(new Exception("test")));
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Empty neural net response, returns semantic response.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_EmptyNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Invalid neural net response, returns semantic response.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_InvalidNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("0.1231723SomeText");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Null neural net response, returns semantic response.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_NullNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(null);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Check that whitespaces are removed from each end
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_Semantic_Trimmed() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getPaddedChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Check that whitespaces are removed from each end
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_Neural_Trimmed() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getPaddedChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training)
     * but the semantic server is confident enough to reply.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus_SemanticOverride() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenThrow(new NeuralNet.NeuralNetRejectedAiStatusException("badstatus"));
        ApiResult result = getChat(0.1f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * The neural network can't be queried because the training status is bad (no training).
     * The semantic server has no confidence so we expect an error 400
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_RejectedNeuralDueToAIStatus() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenThrow(new NeuralNet.NeuralNetRejectedAiStatusException("badstatus"));
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    /***
     * History is passed back to the user when semantic server wins
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Semantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getHistory());
    }

    /***
     * History is passed back to the user when neuralnet server wins
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Neural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getHistory());
    }

    /***
     * Reset command is processed and removed when text is at the beginning of the string
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Semantic_Reset_Pre() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getCustomChat(0.2f, SEMANTICRESULT + HISTORY_REST_DIRECTIVE, NEURALRESULT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Reset command is processed and removed when text is in the middle of the string
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Semantic_Reset_Mid() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getCustomChat(0.2f, SEMANTICRESULT.substring(0, 3) + HISTORY_REST_DIRECTIVE + SEMANTICRESULT.substring(3), NEURALRESULT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Reset command is processed and removed when text is at the end of the string
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Semantic_Reset_Post() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getCustomChat(0.2f, HISTORY_REST_DIRECTIVE + SEMANTICRESULT, NEURALRESULT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Semantic server sends reset command. History is cleared but if neuralnet wins the confidence contest then neuralnet response is returned unmodified.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_Semantic_Reset_NeuralNet_Wins() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getCustomChat(0.5f, HISTORY_REST_DIRECTIVE + SEMANTICRESULT, NEURALRESULT);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("", ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Semantic server sends reset command. History is cleared but if neuralnet wins the confidence contest then neuralnet response is returned unmodified.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_History_NeuralNet_Reset_Ignored() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        String neuralResetCommand = NEURALRESULT + HISTORY_REST_DIRECTIVE;
        ApiResult result = getCustomChat(0.5f, SEMANTICRESULT, neuralResetCommand);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(neuralResetCommand, ((ApiChat) result).getResult().getHistory());
        Assert.assertEquals(neuralResetCommand, ((ApiChat) result).getResult().getAnswer());
    }

    /***
     * Tests an intent is recognized by the API when the backend sends it.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentRecognized() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("@meta.intent." + intentName);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(miList);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        MemoryIntent ri = r.getIntents().get(0);
        Assert.assertEquals(intentName, ri.getName());
    }

    /***
     * Memory intent is fulfilled.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentFulfilled() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("@meta.intent." + intentName);
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
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentPrompt() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
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
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_exceededPrompts() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
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
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_fulfillFromUserQuestion() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
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
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_variableWithNoPrompt() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        mi.getVariables().get(0).setPrompts(new ArrayList<>());
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(new ArrayList<>());
        Assert.assertFalse(mi.isFulfilled());
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertFalse(mi.isFulfilled());
        verify(this.fakeIntentHandler, never()).updateStatus(mi);
    }

    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_IntentPrompt_NoPromptWhenZero() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
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

    /***
     * Both WNET and neeural net return empty responses.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     * @throws Database.DatabaseException
     */
    @Test
    public void testChat_EmptyBoth() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet is not found.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     */
    @Test
    public void testChat_AiNotFound() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        doThrow(new NeuralNet.NeuralNetAiNotFoundException("test")).when(this.fakeNeuralNet).startAnswerRequest(anyString(), any(), any(), any());
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet is not responding.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     */
    @Test
    public void testChat_AiNotResponding() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        doThrow(NeuralNet.NeuralNetNotRespondingException.class).when(this.fakeNeuralNet).startAnswerRequest(anyString(), any(), any(), any());
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_ACCEPTED, result.getStatus().getCode());
    }

    /***
     * Semantic server sends response below required confidence threshold, neuralnet throws generic NeuralNetException exception.
     * @throws SemanticAnalysis.SemanticAnalysisException
     * @throws NeuralNet.NeuralNetException
     */
    @Test
    public void testChat_AiException() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        doThrow(NeuralNet.NeuralNetException.class).when(this.fakeNeuralNet).startAnswerRequest(anyString(), any(), any(), any());
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private ChatResult getSemanticResult() {
        ChatResult res = new ChatResult();
        res.setAnswer(SEMANTICRESULT);
        res.setScore(0.3d);
        return res;
    }

    private ApiResult getValidChat(float min_p) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(RNN_NEURALRESULT);
        return getChat(min_p);
    }

    private ApiResult getPaddedChat(float min_p) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        return getCustomChat(min_p, " " + SEMANTICRESULT + "\n", " " + NEURALRESULT + "\n");
    }

    private ApiResult getCustomChat(float min_p, String semanticResult, String neuralResult) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ChatResult customSemantic = getSemanticResult();
        customSemantic.setAnswer(semanticResult);
        String customNeural = RNN_NEURALRESULT.replace(NEURALRESULT, neuralResult);
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(customSemantic);
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(customNeural);
        return getChat(min_p);
    }

    private ApiResult getChat(float min_p) {
        return this.getChat(min_p, QUESTION);
    }

    private ApiResult getChat(float min_p, String question) {
        return this.chatLogic.chat(this.fakeContext, AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
    }

    private MemoryIntent getMemoryIntentForPrompt(int maxPrompts, String currentValue)
            throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
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
        ChatResult chatResult = new ChatResult();
        chatResult.setAnswer("@meta.intent." + intentName);
        chatResult.setScore(0.9d);
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(chatResult);
        when(this.fakeIntentHandler.parseAiResponseForIntent(anyString(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

}

