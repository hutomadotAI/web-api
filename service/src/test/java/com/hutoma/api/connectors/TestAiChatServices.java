package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestBotHelper;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.common.TrackedThreadSubPool;
import com.hutoma.api.containers.sub.AiMinP;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.RequestAiml;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.controllers.RequestRnn;
import com.hutoma.api.controllers.RequestWnet;
import com.hutoma.api.controllers.ServerMetadata;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 02/02/17.
 */
public class TestAiChatServices {

    private static final UUID CHATID = UUID.randomUUID();
    private static final UUID AIML_BOT_AIID = UUID.randomUUID();
    private Database fakeDatabase;
    private RequestAiml fakeRequestAiml;
    private RequestRnn fakeRequestRnn;
    private RequestWnet fakeRequestWnet;
    private Config fakeConfig;
    private AIChatServices chatServices;
    private TrackedThreadSubPool threadSubPool;


    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.fakeConfig = mock(Config.class);
        this.fakeRequestWnet = mock(RequestWnet.class);
        this.fakeRequestAiml = mock(RequestAiml.class);
        this.fakeRequestRnn = mock(RequestRnn.class);
        this.threadSubPool = mock(TrackedThreadSubPool.class);
        this.chatServices = new AIChatServices(
                this.fakeDatabase, mock(ILogger.class), mock(JsonSerializer.class),
                mock(Tools.class), this.fakeConfig, mock(JerseyClient.class),
                this.threadSubPool,
                this.fakeRequestWnet, this.fakeRequestRnn, this.fakeRequestAiml);
    }

    @Test
    public void startChatRequests_noLinkedBots_aiIsTrained() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.chatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE, TrainingStatus.AI_TRAINING_COMPLETE));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestRnn).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml, never()).issueChatRequests(any(), any(), any());
    }

    @Test(expected = AIChatServices.AiNotReadyToChat.class)
    public void startChatRequests_noLinkedBots_aiIsNotTrained() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.chatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED, TrainingStatus.AI_UNDEFINED));
        this.issueStartChatRequests();
    }

    @Test
    public void startChatRequests_aiIsNotTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(SAMPLEBOT.getAiid()));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED, TrainingStatus.AI_UNDEFINED));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet, never()).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestRnn, never()).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any(), any());
    }

    @Test
    public void startChatRequests_aiIsTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(TestBotHelper.getBot(DEVID_UUID, AIML_BOT_AIID, BOTID)));
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE, TrainingStatus.AI_TRAINING_COMPLETE));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestRnn).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any(), any());
    }

    @Test
    public void canChatWithAi() throws Database.DatabaseException {
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE, TrainingStatus.AI_TRAINING_COMPLETE));
        Assert.assertTrue(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
        Assert.assertTrue(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.RNN));
    }

    @Test
    public void canChatWithAi_CantChat() throws Database.DatabaseException {
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED, TrainingStatus.AI_UNDEFINED));
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.RNN));
    }

    @Test
    public void canChatWithAi_RnnQueued() throws Database.DatabaseException {
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE, TrainingStatus.AI_TRAINING_QUEUED));
        Assert.assertTrue(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.RNN));
    }

    @Test
    public void canChatWithAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenThrow(Database.DatabaseException.class);
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.RNN));
    }

    @Test
    public void testChatServices_noLinkedBots_minPMap_containsSelf() throws Database.DatabaseException,
            ServerConnector.AiServicesException, RequestBase.AiControllerException, ServerMetadata.NoServerAvailable {
        ChatState chatState = ChatState.getEmpty();
        BackendStatus beStatus = new BackendStatus();
        beStatus.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        beStatus.setEngineStatus(BackendServerType.RNN, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(beStatus);
        // No linked bots
        when(this.fakeDatabase.getAisLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
        // MinP map should contain the value for the main ai
        Assert.assertTrue(this.chatServices.getMinPMap().containsKey(AIID));
        Assert.assertEquals(1, this.chatServices.getMinPMap().keySet().size());
    }

    @Test
    public void testChatServices_withLinkedBots_minPMap_containsMinPFromAllLinkedBots() throws Database.DatabaseException,
            ServerConnector.AiServicesException, RequestBase.AiControllerException, ServerMetadata.NoServerAvailable {
        final double minP = 0.4;
        ChatState chatState = ChatState.getEmpty();
        chatState.setConfidenceThreshold(minP);
        BackendStatus beStatus = new BackendStatus();
        beStatus.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        beStatus.setEngineStatus(BackendServerType.RNN, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        when(this.fakeDatabase.getAIStatusReadOnly(any(), any())).thenReturn(beStatus);

        AiMinP bot1 = new AiMinP(DEVID_UUID, UUID.randomUUID(), 0.5);
        AiMinP bot2 = new AiMinP(DEVID_UUID, UUID.randomUUID(), 0.7);
        List<AiMinP> linkedBots = Arrays.asList(bot1, bot2);
        when(this.fakeDatabase.getAisLinkedToAi(any(), any())).thenReturn(linkedBots);
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
        // MinP map should contain the value for the main ai
        Assert.assertEquals(3, this.chatServices.getMinPMap().keySet().size());
        Assert.assertEquals(bot1.getMinP(), this.chatServices.getMinPMap().get(bot1.getAiid()), 0.00001);
        Assert.assertEquals(bot2.getMinP(), this.chatServices.getMinPMap().get(bot2.getAiid()), 0.00001);
        Assert.assertEquals(minP, this.chatServices.getMinPMap().get(AIID), 0.00001);
    }

    private void issueStartChatRequests() throws ServerConnector.AiServicesException, RequestBase.AiControllerException, ServerMetadata.NoServerAvailable {
        ChatState chatState = ChatState.getEmpty();
        chatState.setHistory("history");
        chatState.setTopic("topic");
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
    }
}
