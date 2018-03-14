package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.sub.AiMinP;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    private DatabaseAI fakeDatabaseAi;
    private DatabaseUser fakeDatabaseUser;
    private ChatAimlConnector fakeRequestAiml;
    private ChatWnetConnector fakeRequestWnet;
    private ChatSvmConnector fakeRequestSvm;
    private Config fakeConfig;
    private AIChatServices chatServices;
    private TrackedThreadSubPool threadSubPool;
    private ChatConnectors fakeChatConnectors;


    @Before
    public void setup() {
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseUser = mock(DatabaseUser.class);
        this.fakeConfig = mock(Config.class);
        this.fakeRequestWnet = mock(ChatWnetConnector.class);
        this.fakeRequestAiml = mock(ChatAimlConnector.class);
        this.fakeRequestSvm = mock(ChatSvmConnector.class);
        this.threadSubPool = mock(TrackedThreadSubPool.class);
        this.fakeChatConnectors = new ChatConnectors(this.fakeRequestWnet, this.fakeRequestAiml, this.fakeRequestSvm);

        this.chatServices = new AIChatServices(
                this.fakeDatabaseAi, mock(ILogger.class), fakeConfig, mock(JsonSerializer.class),
                mock(Tools.class), this.fakeConfig, mock(JerseyClient.class),
                this.threadSubPool, this.fakeChatConnectors);
    }

    @Test
    public void startChatRequests_noLinkedBots_aiIsTrained() throws ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, DatabaseException, NoServerAvailableException {
        when(this.chatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml, never()).issueChatRequests(any(), any(), any());
    }

    @Test(expected = AIChatServices.AiNotReadyToChat.class)
    public void startChatRequests_noLinkedBots_aiIsNotTrained() throws ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, DatabaseException, NoServerAvailableException {
        when(this.chatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED));
        this.issueStartChatRequests();
    }

    @Test
    public void startChatRequests_aiIsNotTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, DatabaseException, NoServerAvailableException {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(SAMPLEBOT.getAiid()));
        when(this.fakeDatabaseAi.getAisLinkedToAi(any(), any()))
                .thenReturn(Collections.singletonList(new AiMinP(DEVID_UUID, SAMPLEBOT.getAiid(), 0.5)));
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet, never()).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any(), any());
    }

    @Test
    public void startChatRequests_aiIsTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, DatabaseException, NoServerAvailableException {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID));
        when(this.fakeDatabaseAi.getAisLinkedToAi(any(), any())).thenReturn(
                Collections.singletonList(new AiMinP(DEVID_UUID, AIML_BOT_AIID, 0.5)));
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any(), any());
    }

    @Test
    public void canChatWithAi() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_TRAINING_COMPLETE));
        Assert.assertTrue(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
    }

    @Test
    public void canChatWithAi_CantChat() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(TestDataHelper.getBackendStatus(
                TrainingStatus.AI_UNDEFINED));
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
    }

    @Test
    public void canChatWithAi_DBException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenThrow(DatabaseException.class);
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID_UUID, AIID).contains(BackendServerType.WNET));
    }

    @Test
    public void testChatServices_noLinkedBots_minPMap_containsSelf() throws DatabaseException,
            ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException, NoServerAvailableException {
        ChatState chatState = ChatState.getEmpty();
        BackendStatus beStatus = new BackendStatus();
        beStatus.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(beStatus);
        // No linked bots
        when(this.fakeDatabaseAi.getAisLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
        // MinP map should contain the value for the main ai
        Assert.assertTrue(this.chatServices.getMinPMap().containsKey(AIID));
        Assert.assertEquals(1, this.chatServices.getMinPMap().keySet().size());
    }

    @Test
    public void testChatServices_withLinkedBots_minPMap_containsMinPFromAllLinkedBots() throws DatabaseException,
            ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException, NoServerAvailableException {
        final double minP = 0.4;
        ChatState chatState = ChatState.getEmpty();
        chatState.setConfidenceThreshold(minP);
        BackendStatus beStatus = new BackendStatus();
        beStatus.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE, 0.0, 1.0));
        when(this.fakeDatabaseAi.getAIStatusReadOnly(any(), any())).thenReturn(beStatus);

        AiMinP bot1 = new AiMinP(DEVID_UUID, UUID.randomUUID(), 0.5);
        AiMinP bot2 = new AiMinP(DEVID_UUID, UUID.randomUUID(), 0.7);
        List<AiMinP> linkedBots = Arrays.asList(bot1, bot2);
        when(this.fakeDatabaseAi.getAisLinkedToAi(any(), any())).thenReturn(linkedBots);
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
        // MinP map should contain the value for the main ai
        Assert.assertEquals(3, this.chatServices.getMinPMap().keySet().size());
        Assert.assertEquals(bot1.getMinP(), this.chatServices.getMinPMap().get(bot1.getAiid()), 0.00001);
        Assert.assertEquals(bot2.getMinP(), this.chatServices.getMinPMap().get(bot2.getAiid()), 0.00001);
        Assert.assertEquals(minP, this.chatServices.getMinPMap().get(AIID), 0.00001);
    }

    @Test(expected = ChatLogic.ChatFailedException.class)
    public void classtestChatservices_passthroughUrl_invalidAiidForDev() throws DatabaseException, ChatLogic.ChatFailedException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        this.chatServices.getAIPassthroughUrl(DEVID_UUID, AIID);
    }

    private void issueStartChatRequests() throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException, NoServerAvailableException {
        ChatState chatState = ChatState.getEmpty();
        chatState.setHistory("history");
        chatState.setTopic("topic");
        this.chatServices.startChatRequests(DEVID_UUID, AIID, CHATID, "question", chatState);
    }
}
