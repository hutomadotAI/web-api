package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookMessageNode;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.facebook.FacebookResponseSegment;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHookResponse;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Provider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestFacebookChatHandler {

    private static final String PAGEID = "pageid";
    private static final String MESSAGE = "message";
    private static final String POSTBACK = "postback";
    private static final String SENDER = "sender";
    private static final String PAGETOKEN = "validpagetoken";
    private static final String ANSWER = "answer";
    private static final String WEBHOOK_ANSWER = "webhook answer";
    private static final String LABEL = "label";
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

    private String fbPostback = "{\"object\":\"page\",\"entry\":[{\"id\":\"1731355550428969\","
            + "\"time\":1498224298036,\"messaging\":[{\"sender\":{\"id\":\"" + SENDER + "\"},"
            + "\"recipient\":{\"id\":\"632106133664550\"},\"timestamp\":1498224297854,\"postback\":"
            + "{\"title\":\"TITLE_FOR_THE_CTA\",\"payload\":\"" + POSTBACK + "\",\"referral\":{\"ref\":"
            + "\"USER_DEFINED_REFERRAL_PARAM\",\"source\":\"SHORTLINK\",\"type\":\"OPEN_THREAD\"}}}]}]}";

    private String fbUnknown = "{\"object\":\"page\",\"entry\":[{\"id\":\"1731355550428969\","
            + "\"time\":1498224298036,\"messaging\":[{\"sender\":{\"id\":\"" + SENDER + "\"},"
            + "\"recipient\":{\"id\":\"632106133664550\"},\"timestamp\":1498224297854}]}]}";

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

        this.chatResult = new ChatResult(TestDataHelper.ALT_SESSIONID, 0.3,
                MESSAGE, ANSWER, 3.296, null);

        this.fakeChatLogicProvider = mock(Provider.class);
        this.fakeChatLogic = mock(ChatLogic.class);
        when(this.fakeChatLogicProvider.get()).thenReturn(this.fakeChatLogic);
        when(this.fakeChatLogic.chatFacebook(Matchers.eq(TestDataHelper.AIID), any(), any(), any(), any()))
                .thenAnswer(invocation -> this.chatResult);
        this.chatHandler = new FacebookChatHandler(this.fakeDatabase, this.fakeLogger,
                this.serializer, this.fakeConnector, this.fakeChatLogicProvider, mock(Tools.class));

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
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(answer, getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_Truncated() throws Exception {
        String answer = String.join("", Collections.nCopies(1000, "A"));
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(640, getOutput(chatOutput.get(0)).getText().length());
    }

    @Test
    public void testChat_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chatFacebook(
                Matchers.eq(TestDataHelper.AIID), any(), Matchers.eq(MESSAGE), anyString(), any());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                Matchers.eq(SENDER), Matchers.eq(PAGETOKEN),
                any());
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
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize("{\n" +
                "   \"text\":\"text field\",\n" +
                "   \"facebook\":{\n" +
                "      \"attachment\":{\n" +
                "         \"type\":\"image\",\n" +
                "         \"payload\":{\n" +
                "            \"url\":\"image_url\"\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}", WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertEquals(FacebookMessageNode.RichContentType.image,
                getOutput(chatOutput.get(0)).getAttachment().getContentType());
    }

    @Test
    public void testChat_OK_RichContent_Deprecated() throws Exception {
        String answer = ANSWER;
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(" {\n" +
                "   \"text\": \"" + WEBHOOK_ANSWER + "\",\n" +
                "   \"facebook\": {\n" +
                "     \"type\": \"image\",\n" +
                "     \"payload\": {\n" +
                "       \"url\": \"http://someimage.com/0.jpg\"\n" +
                "     }\n" +
                "   }\n" +
                " }\n" +
                " ", WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertEquals(FacebookMessageNode.RichContentType.image,
                getOutput(chatOutput.get(0)).getAttachment().getContentType());
    }

    @Test
    public void testChat_OK_QuickReplies_Text() throws Exception {
        String answer = ANSWER;
        String s = "{\n" +
                "   \"text\":\"text field\",\n" +
                "   \"facebook\":{\n" +
                "      \"quick_replies\":[\n" +
                "         {\n" +
                "            \"content_type\":\"text\",\n" +
                "            \"title\":\"Search\",\n" +
                "            \"payload\":\"PAYLOAD\",\n" +
                "            \"image_url\":\"image_url\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"content_type\":\"location\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"content_type\":\"text\",\n" +
                "            \"title\":\"Something Else\",\n" +
                "            \"payload\":\"PAYLOAD\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertNotNull(getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_QuickReplies_Attachment() throws Exception {
        String answer = ANSWER;
        String s = "{\n" +
                "   \"text\":\"text field\",\n" +
                "   \"facebook\":{\n" +
                "      \"quick_replies\":[\n" +
                "         {\n" +
                "            \"content_type\":\"text\",\n" +
                "            \"title\":\"Search\",\n" +
                "            \"payload\":\"PAYLOAD\",\n" +
                "            \"image_url\":\"image_url\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"content_type\":\"location\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"content_type\":\"text\",\n" +
                "            \"title\":\"Something Else\",\n" +
                "            \"payload\":\"PAYLOAD\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"attachment\":{\n" +
                "         \"type\":\"image\",\n" +
                "         \"payload\":{\n" +
                "            \"url\":\"image_url\"\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertNull(getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_Postback() throws Exception {
        FacebookNotification notification = (FacebookNotification)
                this.serializer.deserialize(this.fbPostback, FacebookNotification.class);
        this.chatHandler.initialise(notification.getEntryList().get(0).getMessaging().get(0));
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chatFacebook(
                Matchers.eq(TestDataHelper.AIID), any(), Matchers.eq(POSTBACK), anyString(), any());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                Matchers.eq(SENDER), Matchers.eq(PAGETOKEN), any());
    }

    @Test
    public void testChat_UnknownMessagingType() throws Exception {
        FacebookNotification notification = (FacebookNotification)
                this.serializer.deserialize(this.fbUnknown, FacebookNotification.class);
        this.chatHandler.initialise(notification.getEntryList().get(0).getMessaging().get(0));
        this.chatHandler.call();
        verify(this.fakeChatLogic, never()).chatFacebook(
                Matchers.eq(TestDataHelper.AIID), any(), any(), anyString(), any());
        verify(this.fakeConnector, never()).sendFacebookMessage(
                Matchers.eq(SENDER), Matchers.eq(PAGETOKEN), any());
    }

    @Test
    public void testSenderActions_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, never()).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_off));
    }

    @Test
    // chat error still sends a message back to facebook so doesn't need typing_off
    public void testSenderActions_ChatError() throws Exception {
        when(this.fakeChatLogic.chatFacebook(Matchers.eq(TestDataHelper.AIID), any(), any(), any(), any()))
                .thenThrow(new ChatLogic.ChatFailedException(ApiError.getInternalServerError()));
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, never()).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_off));
    }

    @Test
    public void testSenderActions_SendError() throws Exception {
        doThrow(new FacebookException("test"))
                .when(this.fakeConnector).sendFacebookMessage(any(), any(), any());
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), Matchers.eq(FacebookConnector.SendMessage.SenderAction.typing_off));
    }

    @Test
    public void testChat_IntentExpansion_OK() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, Arrays.asList("A", "B", "C"), false);
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertNotNull(getOutput(chatOutput.get(0)).getText());
        Assert.assertEquals(3,
                getOutput(chatOutput.get(0)).getQuickReplies().size());
    }

    @Test
    public void testChat_IntentExpansion_1Button() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, Arrays.asList("A"), false);
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertEquals(1,
                getOutput(chatOutput.get(0)).getQuickReplies().size());
    }

    @Test
    public void testChat_IntentExpansion_11Buttons() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, Arrays.asList("A", "B", "C", "D", "E", "F",
                        "G", "H", "I", "J", "K"), false);
        Assert.assertEquals(1, chatOutput.size());
        Assert.assertEquals(11,
                getOutput(chatOutput.get(0)).getQuickReplies().size());
    }

    @Test
    public void testChat_IntentExpansion_TooManyButtons() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, Arrays.asList("A", "B", "C", "D", "E", "F",
                        "G", "H", "I", "J", "K", "X"), false);
        Assert.assertEquals(1, chatOutput.size());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasQuickReplies());
    }

    @Test
    public void testChat_IntentExpansion_NoKeys() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, new ArrayList<>(), false);
        Assert.assertEquals(1, chatOutput.size());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasQuickReplies());
    }

    @Test
    public void testChat_IntentExpansion_SystemEntity() throws Exception {
        String answer = ANSWER;
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null, "intentname",
                LABEL, Arrays.asList("A", "B", "C"), true);
        Assert.assertEquals(1, chatOutput.size());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasAttachment());
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasQuickReplies());
    }

    private FacebookMessageNode getOutput(FacebookResponseSegment segment) {
        FacebookConnector.SendMessage sendMessage = new FacebookConnector.SendMessage("recipient");
        segment.populateMessageContent(sendMessage);
        return sendMessage.getMessageNode();
    }

    private List<FacebookResponseSegment> makeChatCall(
            final String answer, final WebHookResponse hookResponse) throws Exception {
        return makeChatCall(answer, hookResponse, null, null, null, false);
    }

    private List<FacebookResponseSegment> makeChatCall(
            final String answer, final WebHookResponse hookResponse,
            String intentName, String intentLabel, List<String> keys, boolean isSystemEntity) throws Exception {

        this.chatResult = new ChatResult(TestDataHelper.ALT_SESSIONID, 0.3,
                MESSAGE, answer, 3.296, hookResponse);
        if (intentName != null) {
            this.chatResult.setIntents(
                    Collections.singletonList(new MemoryIntent(
                            intentName, TestDataHelper.AIID, TestDataHelper.SESSIONID,
                            Collections.singletonList(
                                    new MemoryVariable(intentName, keys, isSystemEntity, intentLabel))
                            , false)));
            this.chatResult.setPromptForIntentVariable(intentLabel);
        }
        List<FacebookResponseSegment> chatOutput = new ArrayList<>();
        doAnswer(invocation -> {
            chatOutput.add(invocation.getArgument(2));
            return true;
        }).when(this.fakeConnector).sendFacebookMessage(anyString(), anyString(), any());
        this.chatHandler.call();
        return chatOutput;
    }
}
