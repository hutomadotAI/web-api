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
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(ILogger.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.chatLogic = new ChatLogic(fakeConfig, fakeSerializer, fakeSemanticAnalysis, fakeNeuralNet, fakeTools,
                fakeLogger, fakeIntentHandler, fakeRecognizer);
    }

    @Test
    public void testChat_Valid_Semantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        ApiResult result = getValidChat(0.2f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_Valid_Neural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        ApiResult result = getValidChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_EmptySemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(fakeNeuralNet.getAnswerResult()).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testChat_NullSemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(fakeSemanticAnalysis.getAnswerResult()).thenThrow(
                new SemanticAnalysis.SemanticAnalysisException(new Exception("test")));
        when(fakeNeuralNet.getAnswerResult()).thenReturn(RNN_NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testChat_EmptyNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {

        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn("");

        ApiResult result = getChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_InvalidNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {

        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn("0.1231723SomeText");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_NullNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn(null);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat) result).getResult().getAnswer());
    }

    @Test
    public void testChat_IntentRecognized() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn("@meta.intent." + intentName);
        when(fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(miList);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        MemoryIntent ri = r.getIntents().get(0);
        Assert.assertEquals(intentName, ri.getName());
    }

    @Test
    public void testChat_IntentFulfilled() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn("@meta.intent." + intentName);
        when(fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertTrue(mi.isFulfilled());
    }

    @Test
    public void testChat_IntentPrompt() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
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
    public void testChat_IntentPrompt_NoPromptWhenZero() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
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
    public void testChat_EmptyBoth() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(res);
        when(fakeNeuralNet.getAnswerResult()).thenReturn("");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    private ChatResult getSemanticResult() {
        ChatResult res = new ChatResult();
        res.setAnswer(SEMANTICRESULT);
        res.setScore(0.3d);
        return res;
    }

    private ApiResult getValidChat(float min_p) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn(RNN_NEURALRESULT);
        return getChat(min_p);
    }

    private ApiResult getChat(float min_p) {
        return this.getChat(min_p, QUESTION);
    }

    private ApiResult getChat(float min_p, String question) {
        return this.chatLogic.chat(fakeContext, AIID, DEVID, question, CHATID.toString(), "history", "topic", min_p);
    }

    private MemoryIntent getMemoryIntentForPrompt(int timesPrompted) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetException {
        final String intentName = "intent1";
        final String promptTrigger = "variableValue";
        final String prompt = "prompt1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList(promptTrigger, "b"));
        mv.setPrompts(Collections.singletonList(prompt));
        mv.setIsMandatory(true);
        mv.setTimesPrompted(timesPrompted);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        when(fakeSemanticAnalysis.getAnswerResult()).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswerResult()).thenReturn("@meta.intent." + intentName);
        when(fakeIntentHandler.parseAiResponseForIntent(anyString(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

}

