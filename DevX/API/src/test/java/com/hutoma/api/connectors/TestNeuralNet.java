package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatRequestStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

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
    NeuralNet neuralNet;
    private String DEVID = "devid";
    private UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    private long QID = 42;
    private String RESULT = "result";

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getNeuralNetworkTimeout()).thenReturn(4L);
        this.fakeDatabase = mock(Database.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        this.neuralNet = new NeuralNet(this.fakeDatabase, this.fakeMessageQueue, this.fakeLogger, this.fakeConfig, this.fakeTools);
    }

    @Test
    public void testNeuralNet_HappyPath() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
        String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
        Assert.assertEquals(this.RESULT, result);
    }

    @Test
    public void testNeuralNet_NeedToStartServer_Success() throws Database.DatabaseException, NeuralNet.NeuralNetException {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(false);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
        String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
        Assert.assertEquals(this.RESULT, result);
    }

    @Test
    public void testNeuralNet_NeedToStartServer_DBFail() throws Database.DatabaseException, NeuralNet.NeuralNetException {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("exception expected");
        } catch (Exception e) {
        }
        Assert.assertEquals(0, this.fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_NeedToStartServer_MessageFail() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(false);
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(this.fakeMessageQueue).pushMessageStartRNN(anyString(), any());
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("exception expected");
        } catch (Exception e) {
        }
        Assert.assertEquals(0, this.fakeTools.getTimestamp());
    }

    @Test
    public void testNeuralNet_Insert_DBFail() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Failed());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("exception expected");
        } catch (NeuralNet.NeuralNetException nne) {
            // this is supposed to throw
        }
        Assert.assertTrue(this.fakeTools.getTimestamp() <= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_Insert_AIStatusRejected() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Rejected());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn(this.RESULT);
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("exception expected");
        } catch (NeuralNet.NeuralNetRejectedAiStatusException rejected) {
            // this is supposed to throw
        }
        Assert.assertTrue(this.fakeTools.getTimestamp() <= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_CheckResult_DBFail() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenThrow(new Database.DatabaseException(new Exception("test")));
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("exception expected");
        } catch (NeuralNet.NeuralNetException nne) {
            // this is supposed to throw
        }
        Assert.assertTrue(this.fakeTools.getTimestamp() <= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_Timeout_Fail() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn("");
        try {
            this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
            String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
            Assert.fail("should have timed out");
        } catch (NeuralNet.NeuralNetNotRespondingException nnnre) {
            // this is supposed to throw
        }
        Assert.assertTrue(this.fakeTools.getTimestamp() >= NeuralNet.POLLEVERY);
    }

    @Test
    public void testNeuralNet_TakesTime_Success() throws Exception {
        when(this.fakeDatabase.isNeuralNetworkServerActive(anyString(), any())).thenReturn(true);
        when(this.fakeDatabase.insertNeuralNetworkQuestion(anyString(), any(), any(), anyString())).thenReturn(getChatRequestStatus_Valid());
        when(this.fakeDatabase.getAnswer(anyLong())).thenReturn("").thenReturn("").thenReturn(this.RESULT);
        this.neuralNet.startAnswerRequest(this.DEVID, this.AIID, this.CHATID, "question");
        String result = this.neuralNet.getAnswerResult(this.DEVID, this.AIID);
        Assert.assertEquals(this.RESULT, result);
        Assert.assertTrue(this.fakeTools.getTimestamp() >= (2 * NeuralNet.POLLEVERY));
    }

    private ChatRequestStatus getChatRequestStatus_Valid() {
        return new ChatRequestStatus(this.QID, TrainingStatus.COMPLETED, false);
    }

    private ChatRequestStatus getChatRequestStatus_Failed() {
        return new ChatRequestStatus(-1, TrainingStatus.COMPLETED, false);
    }

    private ChatRequestStatus getChatRequestStatus_Rejected() {
        return new ChatRequestStatus(-11, TrainingStatus.CANCELLED, true);
    }

}

