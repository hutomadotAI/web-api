package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import com.hutoma.api.logic.chat.ChatEmbHandler;
import com.hutoma.api.logic.chat.ChatHandoverHandler;
import com.hutoma.api.logic.chat.ChatWorkflow;
import com.hutoma.api.memory.ChatStateHandler;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestChatLogicHandover  extends TestChatBase {

    private static final String RANDOM_QUESTION = "Random question";
    private static final String HANDOVER_MESSAGE = "handover";
    private static final String FAKE_RESPONSE = "fake response from AI";
    private static final String DEFAULT_RESPONSE = "Erm...";

    /***
     * Chat is handed over if the bad answer count goes over the threshold.
     */
    @Test
    public void testChat_handover_triggeredIfNumErrorsAboveThreshold() throws ChatStateHandler.ChatStateException {
        ChatLogic chatLogic = getLogicWithState(1, 1);
        ApiChat result = (ApiChat) chatLogic.chat(AIID, DEVID_UUID, RANDOM_QUESTION, CHATID.toString(), null);
        Assert.assertEquals(ChatHandoverTarget.Other.getStringValue(), result.getResult().getChatTarget());
        Assert.assertEquals(HANDOVER_MESSAGE, result.getResult().getAnswer());
    }

    /***
     * Chat is not handed over if the bad answer count is still under the threshold.
     */
    @Test
    public void testChat_handover_notTriggeredIfNumErrorsBelowThreshold() throws ChatStateHandler.ChatStateException {
        ChatLogic chatLogic = getLogicWithState(2, 0);
        ApiChat result = (ApiChat) chatLogic.chat(AIID, DEVID_UUID, RANDOM_QUESTION, CHATID.toString(), null);
        Assert.assertEquals(ChatHandoverTarget.Ai.getStringValue(), result.getResult().getChatTarget());
        Assert.assertEquals(DEFAULT_RESPONSE, result.getResult().getAnswer());
    }

    /***
     * Chat handover is reset if the handover reset time has passed.
     */
    @Test
    public void testChat_handover_resetAfterTimeout() throws ChatStateHandler.ChatStateException {
        ChatLogic chatLogic = getLogicWithState(2, 1,
                new DateTime(DateTimeZone.UTC).plusSeconds(-1), ChatHandoverTarget.Other);
        ApiChat result = (ApiChat) chatLogic.chat(AIID, DEVID_UUID, RANDOM_QUESTION, CHATID.toString(), null);
        Assert.assertEquals(ChatHandoverTarget.Ai.getStringValue(), result.getResult().getChatTarget());
    }

    /***
     * Chat handover is not reset if the handover reset time has not passed.
     */
    @Test
    public void testChat_handover_noResetIfBeforeTimeout() throws ChatStateHandler.ChatStateException {
        ChatHandoverTarget initialTarget = ChatHandoverTarget.Other;
        ChatLogic chatLogic = getLogicWithState(2, 1,
                new DateTime(DateTimeZone.UTC).plusSeconds(10), initialTarget);
        ApiChat result = (ApiChat) chatLogic.chat(AIID, DEVID_UUID, RANDOM_QUESTION, CHATID.toString(), null);
        Assert.assertEquals(initialTarget.getStringValue(), result.getResult().getChatTarget());
        Assert.assertEquals(null, result.getResult().getAnswer());
    }

    /***
     * Chat handover has no effect if the error threshold is not defined (<0).
     */
    @Test
    public void testChat_handover_noHandoverIfErrorThresholdNotDefined() throws ChatStateHandler.ChatStateException {
        ChatLogic chatLogic = getLogicWithState(-1, 1,
                null, ChatHandoverTarget.Ai);
        ApiChat result = (ApiChat) chatLogic.chat(AIID, DEVID_UUID, RANDOM_QUESTION, CHATID.toString(), null);
        Assert.assertEquals(ChatHandoverTarget.Ai.getStringValue(), result.getResult().getChatTarget());
    }



    private ChatLogic getLogicWithState(final int errorThresholdHandover, final int currBadAnswerCount)
            throws ChatStateHandler.ChatStateException {
        return getLogicWithState(errorThresholdHandover, currBadAnswerCount, null,
                ChatHandoverTarget.Ai);
    }

    private ChatLogic getLogicWithState(final int errorThresholdHandover, final int currBadAnswerCount,
                                        final DateTime handoverResetTime, final ChatHandoverTarget initialTarget)
            throws ChatStateHandler.ChatStateException {

        ChatResult chatResult = new ChatResult(RANDOM_QUESTION);
        ChatState initialState = ChatState.getEmpty();
        initialState.setHandoverResetTime(handoverResetTime);
        initialState.setBadAnswersCount(currBadAnswerCount);
        initialState.setAi(getSampleAI());
        initialState.getAi().setErrorThresholdHandover(errorThresholdHandover);
        initialState.getAi().setHandoverMessage(HANDOVER_MESSAGE);
        initialState.setChatTarget(initialTarget);
        chatResult.setChatState(initialState);
        chatResult.setAnswer(FAKE_RESPONSE);

        AiStrings aiStrings = mock(AiStrings.class);
        try {
            when(aiStrings.getRandomDefaultChatResponse(any(), any())).thenReturn(DEFAULT_RESPONSE);
        } catch (AiStrings.AiStringsException ex) {
            Assert.fail(ex.getMessage());
        }
        Tools tools = mock(Tools.class);
        when(tools.getTimestamp()).thenReturn(new DateTime(DateTimeZone.UTC).getMillis());
        ChatDefaultHandler defaultHandler = new ChatDefaultHandler(aiStrings, mock(ILogger.class));
        ChatHandoverHandler handoverHandler = new ChatHandoverHandler(tools);
        ChatWorkflow chatWorkflow = mock(ChatWorkflow.class);
        ChatEmbHandler backendHandler = mock(ChatEmbHandler.class);
        try {
            when(this.fakeChatServices.awaitBackend(BackendServerType.SVM)).thenReturn(
                    new HashMap<UUID, ChatResult>() {{put(UUID.fromString(initialState.getAi().getAiid()), chatResult);}});
        } catch (ChatBackendConnector.AiControllerException ex) {
            Assert.fail(ex.getMessage());
        }
        try {
            when(backendHandler.doWork(any(), any(), any())).thenReturn(chatResult);
        } catch (ServerConnector.AiServicesException | WebHooks.WebHookException | ChatLogic.IntentException
                | ChatBackendConnector.AiControllerException ex) {
            Assert.fail(ex.getMessage());
        }
        when(chatWorkflow.getHandlers()).thenReturn(
                Arrays.asList(handoverHandler, backendHandler, defaultHandler));

        when (this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(initialState);
        return new ChatLogic(this.fakeChatServices, this.fakeChatStateHandler, tools,
                mock(ILogger.class), mock(ChatLogger.class), chatWorkflow);
    }
}
