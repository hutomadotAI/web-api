package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.facebook.FacebookRichContentNode;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.WebHookResponse;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Collections;
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
    private static final String WEBHOOK_ANSWER = "webhook answer";
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

    private ChatResult chatResult;

    @Before
    public void setup() throws Database.DatabaseException, FacebookException, ChatLogic.ChatFailedException {
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

        chatResult = new ChatResult(TestDataHelper.ALT_SESSIONID, 0.3,
                MESSAGE, ANSWER, 3.296, null);

        this.fakeChatLogicProvider = mock(Provider.class);
        this.fakeChatLogic = mock(ChatLogic.class);
        when(this.fakeChatLogicProvider.get()).thenReturn(this.fakeChatLogic);
        when(this.fakeChatLogic.chatFacebook(Matchers.eq(TestDataHelper.AIID), any(), any(), any()))
                .thenAnswer(invocation -> chatResult);
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
    public void testChat_OK_NotTruncated() throws Exception {
        String answer = String.join("", Collections.nCopies(640, "A"));
        ChatOutput chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(answer, chatOutput.text);
    }

    @Test
    public void testChat_OK_Truncated() throws Exception {
        String answer = String.join("", Collections.nCopies(1000, "A"));
        ChatOutput chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(640, chatOutput.text.length());
    }

    @Test
    public void testChat_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chatFacebook(
                Matchers.eq(TestDataHelper.AIID), any(), Matchers.eq(MESSAGE), anyString());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                Matchers.eq(SENDER), Matchers.eq(PAGETOKEN),
                any(), any());
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

    @Test
    public void testChat_OK_RichContent() throws Exception {
        String answer = ANSWER;
        WebHookResponse hookResponse = (WebHookResponse) serializer.deserialize(" {\n" +
                "   \"text\": \"" + WEBHOOK_ANSWER + "\",\n" +
                "   \"facebook\": {\n" +
                "     \"type\": \"image\",\n" +
                "     \"payload\": {\n" +
                "       \"url\": \"http://someimage.com/0.jpg\"\n" +
                "     }\n" +
                "   }\n" +
                " }\n" +
                " ", WebHookResponse.class);
        ChatOutput chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertNull(chatOutput.text);
        Assert.assertEquals(FacebookRichContentNode.RichContentType.image,
                chatOutput.richContentNode.getContentType());
    }

    @Test
    public void testChat_RichContentInvalid_SendTextInstead() throws Exception {
        String answer = ANSWER;
        WebHookResponse hookResponse = (WebHookResponse) serializer.deserialize(" {\n" +
                "   \"text\": \"" + WEBHOOK_ANSWER + "\",\n" +
                "   \"facebook\": {\n" +
                "     \"type\": \"badtext\",\n" +
                "     \"payload\": {\n" +
                "       \"url\": \"http://someimage.com/0.jpg\"\n" +
                "     }\n" +
                "   }\n" +
                " }\n" +
                " ", WebHookResponse.class);

        ChatOutput chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertEquals(ANSWER, chatOutput.text);
        Assert.assertNull(chatOutput.richContentNode);
    }

    private ChatOutput makeChatCall(final String answer, final WebHookResponse hookResponse) throws Exception {
        chatResult = new ChatResult(TestDataHelper.ALT_SESSIONID, 0.3,
                MESSAGE, answer, 3.296, hookResponse);
        final ChatOutput chatOutput = new ChatOutput();
        doAnswer(invocation -> {
            chatOutput.text = invocation.getArgument(2);
            chatOutput.richContentNode = invocation.getArgument(3);
            return true;
        }).when(this.fakeConnector).sendFacebookMessage(anyString(), anyString(), any(), any());
        this.chatHandler.call();
        return chatOutput;
    }

    public class ChatOutput {
        public String text;
        public FacebookRichContentNode richContentNode;
    }
}
