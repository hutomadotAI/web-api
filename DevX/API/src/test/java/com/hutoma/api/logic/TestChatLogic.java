package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
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
    FakeJsonSerializer fakeSerializer;
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

    String semanticResult;
    String neuralResult;

    @Before
    public void setup() {
        semanticResult = "0.3|" + SEMANTICRESULT;
        neuralResult = NEURALRESULT;
        this.fakeSerializer = new FakeJsonSerializer();
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

    private api_root._chat getValidChat(float min_p) {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn(semanticResult);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(neuralResult);
        return getChat(min_p);
    }

    private api_root._chat getChat(float min_p) {
        chatLogic.chat(fakeContext, AIID, DEVID, "question", UID, "history", false, false, min_p);
        return ((api_root._chat) fakeSerializer.getUnserialized());
    }

    @Test
    public void testChat_Valid_Semantic() {
        api_root._chat apiRoot = getValidChat(0.2f);
        Assert.assertEquals(200, apiRoot.status.code);
        Assert.assertEquals(SEMANTICRESULT, apiRoot.result.answer);
    }

    @Test
    public void testChat_Valid_Neural() {
        api_root._chat apiRoot = getValidChat(0.5f);
        Assert.assertEquals(200, apiRoot.status.code);
        Assert.assertEquals(NEURALRESULT, apiRoot.result.answer);
    }

    @Test
    public void testChat_EmptySemantic() {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn("");
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(neuralResult);
        api_root._chat apiRoot = getChat(0.2f);
        Assert.assertEquals(500, apiRoot.status.code);
    }

    @Test
    public void testChat_NullSemantic() {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn(null);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(neuralResult);
        api_root._chat apiRoot = getChat(0.2f);
        Assert.assertEquals(500, apiRoot.status.code);
    }

    @Test
    public void testChat_EmptyNeural() {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn(semanticResult);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn("");
        api_root._chat apiRoot = getChat(0.5f);
        Assert.assertEquals(200, apiRoot.status.code);
        Assert.assertEquals(SEMANTICRESULT, apiRoot.result.answer);
    }

    @Test
    public void testChat_NullNeural() {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn(semanticResult);
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        api_root._chat apiRoot = getChat(0.5f);
        Assert.assertEquals(200, apiRoot.status.code);
        Assert.assertEquals(SEMANTICRESULT, apiRoot.result.answer);
    }

    @Test
    public void testChat_EmptyBoth() {
        when(fakeSemanticAnalysis.getAnswer(anyString(), anyString(), anyString(), anyFloat(), anyBoolean())).thenReturn("");
        when(fakeNeuralNet.getAnswer(anyString(), anyString(), anyString(), anyString())).thenReturn("");
        api_root._chat apiRoot = getChat(0.5f);
        Assert.assertEquals(500, apiRoot.status.code);
    }

}

