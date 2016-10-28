package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
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
    //http://mockito.org/
    private JsonSerializer fakeSerializer;
    private SecurityContext fakeContext;
    private Database fakeDatabase;
    private MessageQueue fakeMessageQueue;
    private Config fakeConfig;
    private Tools fakeTools;
    private NeuralNet fakeNeuralNet;
    private SemanticAnalysis fakeSemanticAnalysis;
    private ChatLogic chatLogic;
    private ILogger fakeLogger;
    private IEntityRecognizer fakeRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(ILogger.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.chatLogic = new ChatLogic(this.fakeConfig, this.fakeSerializer, this.fakeSemanticAnalysis, this.fakeNeuralNet, this.fakeTools,
                this.fakeLogger, this.fakeIntentHandler, this.fakeRecognizer);
    }

    @Test
    public void testChat_Valid_Semantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_Valid_Neural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ApiResult result = getValidChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_EmptySemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_NullSemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenThrow(
                new SemanticAnalysis.SemanticAnalysisException(new Exception("test")));
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testChat_EmptyNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {

        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("");

        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_InvalidNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {

        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("0.1231723SomeText");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_NullNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn(null);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
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

    @Test
    public void testChat_IntentFulfilled() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("@meta.intent." + intentName);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertTrue(mi.isFulfilled());
    }

    @Test
    public void testChat_IntentPrompt() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        final int timesPrompted = 3;
        MemoryIntent mi = getMemoryIntentForPrompt(timesPrompted);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is the prompt
        Assert.assertEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is decremented
        Assert.assertEquals(timesPrompted - 1, mi.getVariables().get(0).getTimesToPrompt());
    }

    @Test
    public void testChat_IntentPrompt_NoPromptWhenZero() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        MemoryIntent mi = getMemoryIntentForPrompt(0);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // But the expected one
        Assert.assertEquals(SEMANTICRESULT, r.getAnswer());
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is decremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesToPrompt());
    }

    @Test
    public void testChat_EmptyBoth() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("");
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

    private ApiResult getChat(float min_p) {
        return this.getChat(min_p, QUESTION);
    }

    private ApiResult getChat(float min_p, String question) {
        return this.chatLogic.chat(this.fakeContext, AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
    }

    private MemoryIntent getMemoryIntentForPrompt(int timesPrompted) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException, Database.DatabaseException {
        final String intentName = "intent1";
        final String promptTrigger = "variableValue";
        final String prompt = "prompt1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList(promptTrigger, "b"));
        mv.setPrompts(Collections.singletonList(prompt));
        mv.setIsMandatory(true);
        mv.setTimesPrompted(timesPrompted);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(this.fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(this.fakeNeuralNet.getAnswerResult(DEVID, AIID)).thenReturn("@meta.intent." + intentName);
        when(this.fakeIntentHandler.parseAiResponseForIntent(anyString(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

}

