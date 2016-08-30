package com.hutoma.api.connectors;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 09/08/2016.
 */
public class TestNeuralNet {

    //http://mockito.org/
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Tools fakeTools;
    NeuralNet fakeNeuralNet;
    SemanticAnalysis fakeSemanticAnalysis;
    Logger fakeLogger;

    private String DEVID = "devid";
    private String AIID = "aiid";
    private String UID = "uid";
    private long QID = 42;
    private String RESULT = "result";

    NeuralNet neuralNet;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getNeuralNetworkTimeout()).thenReturn(4L);
        this.fakeDatabase = mock(Database.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        neuralNet = new NeuralNet(fakeDatabase, fakeMessageQueue, fakeLogger, fakeConfig, fakeTools);
    }

    @Test
    public void testNeuralNet_HappyPath() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(true);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(RESULT, result);
    }

    @Test
    public void testNeuralNet_NeedToStartServer_Success() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(false);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(RESULT, result);
    }

    @Test
    public void testNeuralNet_NeedToStartServer_DBFail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenThrow(new Exception());
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(null, result);
        Assert.assertEquals(0, fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_NeedToStartServer_MessageFail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(false);
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(fakeMessageQueue).pushMessageStartRNN(anyString(), anyString());
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(null, result);
        Assert.assertEquals(0, fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_Insert_DBFail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(true);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(-1L);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(null, result);
        Assert.assertEquals(0, fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_CheckResult_DBFail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(true);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(null);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(null, result);
        Assert.assertTrue(fakeTools.getTimestamp() <= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_Timeout_Fail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(true);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn("");
        try {
            String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
            Assert.fail("should have timed out");
        } catch (NeuralNet.NeuralNetNotRespondingException nnnre) {
            // this is supposed to throw
        }
        Assert.assertTrue(fakeTools.getTimestamp() >= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_TakesTime_Success() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(true);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn("").thenReturn("").thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(RESULT, result);
        Assert.assertTrue(fakeTools.getTimestamp() >= (3 * NeuralNet.POLLEVERY));
    }

}

