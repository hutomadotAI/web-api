package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    public void testNeuralNet_NeedToStartServer_Success() throws Database.DatabaseException, NeuralNet.NeuralNetException {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(false);
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
        Assert.assertEquals(RESULT, result);
    }

    @Test
    public void testNeuralNet_NeedToStartServer_DBFail() throws Database.DatabaseException, NeuralNet.NeuralNetException {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        try {
            String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
            Assert.fail("exception expected");
        } catch (Exception e) {
        }
        Assert.assertEquals(0, fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_NeedToStartServer_MessageFail() throws Exception {
        when(fakeDatabase.isNeuralNetworkServerActive(anyString(), anyString())).thenReturn(false);
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(fakeMessageQueue).pushMessageStartRNN(anyString(), anyString());
        when(fakeDatabase.insertNeuralNetworkQuestion(anyString(), anyString(), anyString(), anyString())).thenReturn(QID);
        when(fakeDatabase.getAnswer(QID)).thenReturn(RESULT);
        try {
            String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
            Assert.fail("exception expected");
        } catch (Exception e) {
        }
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
        when(fakeDatabase.getAnswer(QID)).thenThrow(new Database.DatabaseException(new Exception("test")));
        try {
            String result = neuralNet.getAnswer(DEVID, AIID, UID, "question");
            Assert.fail("exception expected");
        } catch (NeuralNet.NeuralNetException nne) {
            // this is supposed to throw
        }
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

