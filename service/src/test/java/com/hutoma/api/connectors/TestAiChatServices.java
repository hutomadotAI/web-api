package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestBotHelper;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
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

import java.util.Collections;
import java.util.UUID;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private ThreadSubPool threadSubPool;


    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.fakeConfig = mock(Config.class);
        this.fakeRequestWnet = mock(RequestWnet.class);
        this.fakeRequestAiml = mock(RequestAiml.class);
        this.fakeRequestRnn = mock(RequestRnn.class);
        this.threadSubPool = mock(ThreadSubPool.class);
        this.chatServices = new AIChatServices(
                this.fakeDatabase, mock(ILogger.class), mock(JsonSerializer.class),
                mock(Tools.class), this.fakeConfig, mock(JerseyClient.class),
                this.threadSubPool,
                this.fakeRequestWnet, this.fakeRequestRnn, this.fakeRequestAiml);
    }

    @Test
    public void startChatRequests_noLinkedBots_aiIsTrained() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.chatServices.getLinkedBotsAiids(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, true));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any());
        verify(this.fakeRequestRnn).issueChatRequests(any(), any());
        verify(this.fakeRequestAiml, never()).issueChatRequests(any(), any());
    }

    @Test(expected = AIChatServices.AiNotReadyToChat.class)
    public void startChatRequests_noLinkedBots_aiIsNotTrained() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.chatServices.getLinkedBotsAiids(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, true));
        this.issueStartChatRequests();
    }

    @Test
    public void startChatRequests_aiIsNotTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(SAMPLEBOT.getAiid().toString()));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, true));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet, never()).issueChatRequests(any(), any());
        verify(this.fakeRequestRnn, never()).issueChatRequests(any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any());
    }

    @Test
    public void startChatRequests_aiIsTrained_onlyAimSingleBot() throws ServerConnector.AiServicesException,
            RequestBase.AiControllerException, Database.DatabaseException, ServerMetadata.NoServerAvailable {
        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID.toString()));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(TestBotHelper.getBot(DEVID, AIML_BOT_AIID, BOTID)));
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, true));
        this.issueStartChatRequests();
        verify(this.fakeRequestWnet).issueChatRequests(any(), any());
        verify(this.fakeRequestRnn).issueChatRequests(any(), any());
        verify(this.fakeRequestAiml).issueChatRequests(any(), any());
    }

    @Test
    public void canChatWithAi() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        Assert.assertTrue(this.chatServices.canChatWithAi(DEVID, AIID));
    }

    @Test
    public void canChatWithAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenThrow(Database.DatabaseException.class);
        Assert.assertFalse(this.chatServices.canChatWithAi(DEVID, AIID));
    }

    private void issueStartChatRequests() throws ServerConnector.AiServicesException, RequestBase.AiControllerException, ServerMetadata.NoServerAvailable {
        this.chatServices.startChatRequests(DEVID, AIID, CHATID, "question", "history", "topic");
    }
}
