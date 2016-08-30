package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import hutoma.api.server.ai.api_root;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 08/08/2016.
 */
public class TestChatLogic {

    //http://mockito.org/
    JsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Tools fakeTools;
    NeuralNet fakeNeuralNet;
    SemanticAnalysis fakeSemanticAnalysis;
    ChatLogic chatLogic;
    Logger fakeLogger;

    private String DEVID = "devid";
    private String AIID = "aiid";
    private String UID = "uid";
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String SEMANTICRESULT = "semanticresult";
    private String NEURALRESULT = "neuralresult";

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        chatLogic = new ChatLogic(fakeConfig, fakeSerializer, fakeSemanticAnalysis, fakeNeuralNet, fakeTools, fakeLogger);
    }

    private ChatResult getSemanticResult() {
        ChatResult res = new ChatResult();
        res.setAnswer(SEMANTICRESULT);
        res.setScore(0.3d);
        return res;
    }

    private ApiResult getValidChat(float min_p) throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(NEURALRESULT);
        return getChat(min_p);
    }

    private ApiResult getChat(float min_p) {
        return chatLogic.chat(fakeContext, AIID, DEVID, "question", UID, "history", false, 0, 0, "topic", "rnn", false, min_p);
    }

    @Test
    public void testChat_Valid_Semantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        ApiResult result = getValidChat(0.2f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat)result).getResult().getAnswer());
    }

    @Test
    public void testChat_Valid_Neural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        ApiResult result = getValidChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(NEURALRESULT, ((ApiChat)result).getResult().getAnswer());
    }

    @Test
    public void testChat_EmptySemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenReturn(res);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testChat_NullSemantic() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenThrow(
                new SemanticAnalysis.SemanticAnalysisException(new Exception("test")));
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(NEURALRESULT);
        ApiResult result = getChat(0.2f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testChat_EmptyNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn("");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat)result).getResult().getAnswer());
    }

    @Test
    public void testChat_NullNeural() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenReturn(getSemanticResult());
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(SEMANTICRESULT, ((ApiChat)result).getResult().getAnswer());
    }

    @Test
    public void testChat_EmptyBoth() throws SemanticAnalysis.SemanticAnalysisException, NeuralNet.NeuralNetNotRespondingException {
        ChatResult res = getSemanticResult();
        res.setAnswer("");
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyFloat(), anyBoolean(), anyInt(), anyInt())).thenReturn(res);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn("");
        ApiResult result = getChat(0.5f);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

}

