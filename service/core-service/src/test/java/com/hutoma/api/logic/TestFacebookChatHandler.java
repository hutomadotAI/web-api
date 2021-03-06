package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseIntegrations;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookMessageNode;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.facebook.FacebookResponseSegment;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.chat.ChatBaseException;
import com.hutoma.api.validation.ParameterValidationException;
import com.hutoma.api.validation.QueryFilter;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.util.*;

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
    private static final String OPTIN = "send-to-messenger";
    String fbOptin = "{\n" +
            "   \"object\":\"page\",\n" +
            "   \"entry\":[\n" +
            "      {\n" +
            "         \"id\":\"1731355550428969\",\n" +
            "         \"time\":1458692752478,\n" +
            "         \"messaging\":[\n" +
            "            {\n" +
            "               \"sender\":{\n" +
            "                  \"id\":\"" + SENDER + "\"\n" +
            "               },\n" +
            "               \"recipient\":{\n" +
            "                  \"id\":\"page_id\"\n" +
            "               },\n" +
            "               \"timestamp\":1234567890,\n" +
            "               \"optin\":{\n" +
            "                  \"ref\":\"" + OPTIN + "\"\n" +
            "               }\n" +
            "            }\n" +
            "         ]\n" +
            "      }\n" +
            "   ]\n" +
            "}";
    private FacebookChatHandler chatHandler;
    private Config fakeConfig;
    private JsonSerializer serializer;
    private DatabaseIntegrations fakeDatabaseIntegrations;
    private ILogger fakeLogger;
    private FacebookConnector fakeConnector;
    private IntegrationRecord fakeIntegrationRecord;
    private Provider<ChatLogic> fakeChatLogicProvider;
    private ChatLogic fakeChatLogic;
    private Provider<QueryFilter> fakeQueryFilterProvider;
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
    public void setup() throws DatabaseException, ChatBaseException, ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, NoServerAvailableException {
        this.fakeConfig = mock(Config.class);
        this.serializer = new JsonSerializer();
        this.fakeDatabaseIntegrations = mock(DatabaseIntegrations.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeConnector = mock(FacebookConnector.class);

        this.fakeIntegrationRecord = mock(IntegrationRecord.class);
        when(this.fakeDatabaseIntegrations.getIntegrationResource(any(), any())).thenReturn(this.fakeIntegrationRecord);

        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata().connect(
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
        when(this.fakeChatLogic.chatFacebook(eq(TestDataHelper.AIID), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> this.chatResult);

        this.fakeQueryFilterProvider = mock(Provider.class);
        when(this.fakeQueryFilterProvider.get()).thenReturn(
                new QueryFilter(this.fakeLogger, mock(Tools.class), this.serializer));

        this.chatHandler = new FacebookChatHandler(this.fakeDatabaseIntegrations, this.fakeLogger,
                this.serializer, this.fakeConnector, this.fakeChatLogicProvider, this.fakeQueryFilterProvider,
                mock(Tools.class));

        this.chatHandler.initialise(
                new FacebookNotification.Messaging(SENDER, PAGEID, MESSAGE));
    }

    @Test
    public void testChat_BadRecipient() throws Exception {
        when(this.fakeDatabaseIntegrations.getIntegrationResource(any(), any())).thenReturn(null);
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
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata().connect(
                "access", "username", DateTime.now().plusHours(1));
        metadata.setPageToken("");
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        this.chatHandler.call();
        verifyZeroInteractions(this.fakeChatLogic);
        verifyZeroInteractions(this.fakeConnector);
    }

    @Test
    public void testChat_OK_NotTruncated() throws Exception {
        String answer = TestDataHelper.stringOfLength(640);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(answer, getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_Truncated() throws Exception {
        String answer = TestDataHelper.stringOfLength(1000);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, null);
        Assert.assertEquals(640, getOutput(chatOutput.get(0)).getText().length());
    }

    @Test
    public void testChat_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chatFacebook(
                eq(TestDataHelper.AIID), any(), eq(MESSAGE), anyString(), any(), any());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                eq(SENDER), eq(PAGETOKEN),
                any());
    }

    @Test
    public void testChat_ValidationError() throws Exception {

        QueryFilter fakeQueryFilter = mock(QueryFilter.class);
        when(fakeQueryFilter.validateChatQuestion(anyString()))
                .thenThrow(new ParameterValidationException("test", "test"));
        when(this.fakeQueryFilterProvider.get()).thenReturn(fakeQueryFilter);
        verifyRequestIgnored();
        // verify database status updated
        verify(this.fakeDatabaseIntegrations, times(1))
                .updateIntegrationStatus(any(), any(), any(), anyBoolean());
    }

    @Test
    public void testChat_Validation_QEmpty() throws Exception {
        this.chatHandler.initialise(
                new FacebookNotification.Messaging(SENDER, PAGEID, ""));
        verifyRequestIgnored();
    }

    @Test
    public void testChat_Validation_QEmptyContent() throws Exception {
        this.chatHandler.initialise(
                new FacebookNotification.Messaging(SENDER, PAGEID, "     "));
        verifyRequestIgnored();
    }

    @Test
    public void testChat_Validation_TooLong() throws Exception {
        this.chatHandler.initialise(
                new FacebookNotification.Messaging(SENDER, PAGEID, TestDataHelper.stringOfLength(1024 + 1)));
        verifyRequestIgnored();
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
                eq(TestDataHelper.AIID), any(), eq(POSTBACK), anyString(), any(), any());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                eq(SENDER), eq(PAGETOKEN), any());
    }

    @Test
    public void testChat_UnknownMessagingType() throws Exception {
        FacebookNotification notification = (FacebookNotification)
                this.serializer.deserialize(this.fbUnknown, FacebookNotification.class);
        this.chatHandler.initialise(notification.getEntryList().get(0).getMessaging().get(0));
        this.chatHandler.call();
        verify(this.fakeChatLogic, never()).chatFacebook(
                eq(TestDataHelper.AIID), any(), any(), anyString(), any(), any());
        verify(this.fakeConnector, never()).sendFacebookMessage(
                eq(SENDER), eq(PAGETOKEN), any());
    }

    @Test
    public void testChat_OptIn() throws Exception {
        FacebookNotification notification = (FacebookNotification)
                this.serializer.deserialize(this.fbOptin, FacebookNotification.class);
        this.chatHandler.initialise(notification.getEntryList().get(0).getMessaging().get(0));
        this.chatHandler.call();
        verify(this.fakeChatLogic, times(1)).chatFacebook(
                eq(TestDataHelper.AIID), any(), eq(OPTIN), anyString(), any(), any());
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                eq(SENDER), eq(PAGETOKEN), any());
    }

    @Test
    public void testSenderActions_OK() throws Exception {
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, never()).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_off));
    }

    @Test
    // chat error still sends a message back to facebook so doesn't need typing_off
    public void testSenderActions_ChatError() throws Exception {
        when(this.fakeChatLogic.chatFacebook(eq(TestDataHelper.AIID), any(), any(), any(), any(), any()))
                .thenThrow(new ChatLogic.ChatFailedException(ApiError.getInternalServerError()));
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, never()).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_off));
    }

    @Test
    public void test_ChatError_NotReady() throws Exception {
        when(this.fakeChatLogic.chatFacebook(eq(TestDataHelper.AIID), any(), any(), any(), any(), any()))
                .thenThrow(new AIChatServices.AiNotReadyToChat("dummy exception"));
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                anyString(), anyString(), any());
    }

    @Test
    public void test_ChatError_ChatFailed() throws Exception {
        when(this.fakeChatLogic.chatFacebook(eq(TestDataHelper.AIID), any(), any(), any(), any(), any()))
                .thenThrow(new ChatLogic.ChatFailedException(ApiError.getInternalServerError()));
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookMessage(
                anyString(), anyString(), any());
    }

    @Test
    public void testSenderActions_SendError() throws Exception {
        doThrow(new FacebookException("test"))
                .when(this.fakeConnector).sendFacebookMessage(any(), any(), any());
        this.chatHandler.call();
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_on));
        verify(this.fakeConnector, times(1)).sendFacebookSenderAction(anyString(),
                anyString(), eq(FacebookConnector.SendMessage.SenderAction.typing_off));
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

    @Test
    public void testChat_OK_multipleResponseNodes() throws Exception {
        String answer = ANSWER;
        String s = "{" +
                "\"text\": \"" + answer + "\"," +
                "\"facebook_multi\":[" +
                "       {\"quick_replies\":[{" +
                "            \"content_type\":\"text\"," +
                "            \"title\":\"Search\"," +
                "            \"payload\":\"PAYLOAD\"," +
                "            \"image_url\":\"image_url\"" +
                "            }]" +
                "       }," +
                "       {\"attachment\":" +
                "           {\"type\": \"template\", \"payload\":" +
                "               {\"template_type\": \"generic\", \"elements\":[" +
                "                   {\"title\": \"title\", \"image_url\": \"image_url\", \"subtitle\": \"subtitle\", \"webview_height_ratio\": \"tall\"," +
                "                   \"buttons\": [{\"type\": \"postback\", \"title\": \"button\", \"payload\": \"payload\"}]" +
                "                   }" +
                "               ]}" +
                "           }" +
                "       }" +
                "   ]" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertTrue(getOutput(chatOutput.get(1)).hasAttachment());
        // Text for first node is set since it's a quick reply
        Assert.assertEquals(ANSWER, getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_multipleResponseNodes_singleNodeIgnored() throws Exception {
        String answer = ANSWER;
        String s = "{" +
                "\"text\": \"" + answer + "\"," +
                "\"facebook\": {\"quick_replies\":[{" +
                "            \"content_type\":\"text\"," +
                "            \"title\":\"Search\"," +
                "            \"payload\":\"PAYLOAD\"," +
                "            \"image_url\":\"image_url\"" +
                "            }]" +
                "       }," +
                "\"facebook_multi\":[" +
                "       {\"attachment\":" +
                "           {\"type\": \"template\", \"payload\":" +
                "               {\"template_type\": \"generic\", \"elements\":[" +
                "                   {\"title\": \"title\", \"image_url\": \"image_url\", \"subtitle\": \"subtitle\", \"webview_height_ratio\": \"tall\"," +
                "                   \"buttons\": [{\"type\": \"postback\", \"title\": \"button\", \"payload\": \"payload\"}]" +
                "                   }" +
                "               ]}" +
                "           }" +
                "       }" +
                "   ]" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasAttachment());
        // Response text for single node is not set (although a quick reply) since the whole node should be ignored
        Assert.assertNull(getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_multipleResponseNodes_multipleTextNodes() throws Exception {
        String answer = ANSWER;
        String s = "{" +
                "\"text\": \"" + answer + "\"," +
                "\"facebook_multi\":[" +
                "       {\"text\":\"text1\"}," +
                "       {\"text\":\"text2\"}" +
                "   ]" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertFalse(getOutput(chatOutput.get(0)).hasQuickReplies());
        Assert.assertTrue(getOutput(chatOutput.get(0)).hasText());
        Assert.assertTrue(getOutput(chatOutput.get(1)).hasText());
        Assert.assertEquals("text1", getOutput(chatOutput.get(0)).getText());
        Assert.assertEquals("text2", getOutput(chatOutput.get(1)).getText());
    }

    @Test
    public void testChat_OK_multipleResponseNodes_multipleTextNodes_oneNull() throws Exception {
        String answer = ANSWER;
        String s = "{" +
                "\"text\": \"" + answer + "\"," +
                "\"facebook_multi\":[" +
                "       {\"text\":\"text1\"}," + // Note the ',', this will cause the array to have 2 nodes, last null
                "   ]" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertEquals(1, chatOutput.size());
        Assert.assertEquals("text1", getOutput(chatOutput.get(0)).getText());
    }

    @Test
    public void testChat_OK_multipleResponseNodes_emptyText_isIgnored() throws Exception {
        String answer = ANSWER;
        String s = "{" +
                "\"text\": \"" + answer + "\"," +
                "\"facebook_multi\":[" +
                "       {\"text\":\"\"}" +
                "   ]" +
                "}";
        WebHookResponse hookResponse = (WebHookResponse) this.serializer.deserialize(s, WebHookResponse.class);
        List<FacebookResponseSegment> chatOutput = makeChatCall(answer, hookResponse);
        Assert.assertTrue(chatOutput.isEmpty());
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

    private void verifyRequestIgnored() throws Exception {
        this.chatHandler.call();
        verify(this.fakeChatLogic, never()).chatFacebook(
                eq(TestDataHelper.AIID), any(), any(), anyString(), any(), any());
        verify(this.fakeConnector, never()).sendFacebookMessage(
                any(), any(), any());
    }

}
