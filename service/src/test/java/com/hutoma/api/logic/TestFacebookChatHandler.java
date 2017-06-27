package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.IntegrationRecord;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.UUID;
import javax.inject.Provider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestFacebookChatHandler {

    private static final String PAGEID = "pageid";
    private static final String MESSAGE = "message";
    private static final String SENDER = "sender";
    private static final String PAGETOKEN = "validpagetoken";
    private static final String ANSWER = "answer";
    private FacebookChatHandler chatHandler;
    private Config fakeConfig;
    private JsonSerializer serializer;
    private Database fakeDatabase;
    private ILogger fakeLogger;
    private FacebookConnector fakeConnector;
    private FacebookToken fakeToken;
    private FacebookNode fakeNode;
    private IntegrationRecord fakeIntegrationRecord;
    private Provider<ChatLogic> fakeChatLogicProvider;
    private ChatLogic fakeChatLogic;

    @Before
    public void setup() throws Database.DatabaseException, FacebookConnector.FacebookException {
        this.fakeConfig = mock(Config.class);
        this.serializer = new JsonSerializer();
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeConnector = mock(FacebookConnector.class);

        this.fakeIntegrationRecord = mock(IntegrationRecord.class);
        when(this.fakeDatabase.getIntegrationResource(any(), any())).thenReturn(this.fakeIntegrationRecord);

        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        metadata.setPageToken(PAGETOKEN);
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        when(this.fakeIntegrationRecord.getIntegrationResource()).thenReturn(PAGEID);
        when(this.fakeIntegrationRecord.isActive()).thenReturn(true);
        when(this.fakeIntegrationRecord.getAiid()).thenReturn(TestDataHelper.AIID);

        String apiChat = "{  \"chatId\": \"" + TestDataHelper.ALT_SESSIONID.toString()
                + "\",  \"timestamp\": 1498561280892,  "
                + "\"result\": {    \"score\": 0.3,    \"query\": \"" + MESSAGE + "\",   "
                + " \"answer\": \"" + ANSWER + "\","
                + "\"elapsedTime\": 2.926  },  \"status\": {    \"code\": 200,    \"info\": \"OK\"  }}";
        ApiChat chatResult = (ApiChat) this.serializer.deserialize(apiChat, ApiChat.class);
        this.fakeChatLogicProvider = mock(Provider.class);
        this.fakeChatLogic = mock(ChatLogic.class);
        when(this.fakeChatLogicProvider.get()).thenReturn(this.fakeChatLogic);
        when(this.fakeChatLogic.chat(Matchers.eq(TestDataHelper.AIID), any(), any(), any()))
                .thenReturn(chatResult);

        this.chatHandler = new FacebookChatHandler(this.fakeDatabase, this.fakeLogger,
                this.serializer, this.fakeConnector, this.fakeChatLogicProvider);
        this.chatHandler.initialise(
                new FacebookNotification.Messaging(SENDER, PAGEID, MESSAGE));
    }

    @Test
    public void testChat_BadRecipient() throws Exception {
        when(this.fakeDatabase.getIntegrationResource(any(), any())).thenReturn(null);
        this.chatHandler.call();
        verifyZeroInteractions(this.fakeChatLogic);
        verifyZeroInteractions(this.fakeConnector);
    }

    @Test
    public void testChat_InactiveIntegration() throws Exception {
        when(this.fakeIntegrationRecord.isActive()).thenReturn(false);
        this.chatHandler.call();
        verifyZeroInteractions(this.fakeChatLogic);
        verifyZeroInteractions(this.fakeConnector);
    }

    @Test
    public void testChat_NoPageToken() throws Exception {
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        metadata.setPageToken("");
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        this.chatHandler.call();
        verifyZeroInteractions(this.fakeChatLogic);
        verifyZeroInteractions(this.fakeConnector);
    }

    @Test
    public void testChat_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chat(
                Matchers.eq(TestDataHelper.AIID), any(), Matchers.eq(MESSAGE), anyString());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                Matchers.eq(SENDER), any(), Matchers.eq(PAGETOKEN));
    }

    @Test
    public void testChatID_SameSame() throws Exception {
        UUID uuid1 = this.chatHandler.generateChatId(
                TestDataHelper.AIID, "123");
        UUID uuid2 = this.chatHandler.generateChatId(
                TestDataHelper.AIID, "123");
        Assert.assertEquals(uuid1, uuid2);
    }

    @Test
    public void testChatID_differentAiid() throws Exception {
        UUID uuid1 = this.chatHandler.generateChatId(
                TestDataHelper.AIID, "123");
        UUID uuid2 = this.chatHandler.generateChatId(
                TestDataHelper.ALT_SESSIONID, "123");
        Assert.assertNotEquals(uuid1, uuid2);
    }

    @Test
    public void testChatID_differentFacebookID() throws Exception {
        UUID uuid1 = this.chatHandler.generateChatId(
                TestDataHelper.AIID, "123");
        UUID uuid2 = this.chatHandler.generateChatId(
                TestDataHelper.AIID, "223");
        Assert.assertNotEquals(uuid1, uuid2);
    }
}
