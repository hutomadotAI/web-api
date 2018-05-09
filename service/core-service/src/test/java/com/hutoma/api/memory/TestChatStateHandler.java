package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 17/05/17.
 */
public class TestChatStateHandler {

    private DatabaseAI fakeDatabaseAi;
    private ILogger fakeLogger;
    private ChatStateHandler chatStateHandler;
    private JsonSerializer fakeJsonSerializer;

    private static void assertChatStateEquals(final ChatState expected, final ChatState actual) {
        Assert.assertEquals(expected.getLockedAiid(), actual.getLockedAiid());
        Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.getTopic(), actual.getTopic());
    }

    @Before
    public void setup() {
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeLogger = mock(ILogger.class);
        this.chatStateHandler = new ChatStateHandler(fakeDatabaseAi, fakeLogger, fakeJsonSerializer);
    }

    @Test
    public void testChatStateHandler_getState() throws DatabaseException, ChatStateHandler.ChatStateException {
        ChatState chatState = getTestChatState();
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getChatState(any(), any(), any(), any())).thenReturn(chatState);
        ChatState result = this.chatStateHandler.getState(DEVID_UUID, AIID, UUID.randomUUID());
        assertChatStateEquals(chatState, result);
    }

    @Test
    public void testChatStateHandler_getState_dbException() throws DatabaseException, ChatStateHandler.ChatStateException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getChatState(any(), any(), any(), any())).thenThrow(DatabaseException.class);
        ChatState result = this.chatStateHandler.getState(DEVID_UUID, AIID, UUID.randomUUID());
        assertChatStateEquals(ChatState.getEmpty(), result);
        verify(this.fakeLogger).logUserExceptionEvent(anyString(), any(), anyString(), any());
    }

    @Test(expected = ChatStateHandler.ChatStateUserException.class)
    public void testChatStateHandler_getState_aiidNotOwned() throws DatabaseException, ChatStateHandler.ChatStateException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        this.chatStateHandler.getState(DEVID_UUID, AIID, UUID.randomUUID());
    }

    @Test
    public void testChatStateHandler_saveState() throws DatabaseException, ChatStateHandler.ChatStateException {
        final UUID chatId = UUID.randomUUID();
        ChatState chatState = getTestChatState();
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        this.chatStateHandler.saveState(DEVID_UUID, AIID, chatId, chatState);
        verify(this.fakeDatabaseAi).saveChatState(DEVID_UUID, chatId, chatState, fakeJsonSerializer);
    }

    @Test(expected = ChatStateHandler.ChatStateUserException.class)
    public void testChatStateHandler_saveState_aiidNotOwned() throws DatabaseException, ChatStateHandler.ChatStateException {
        final UUID chatId = UUID.randomUUID();
        ChatState chatState = getTestChatState();
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        this.chatStateHandler.saveState(DEVID_UUID, AIID, chatId, chatState);
    }

    @Test(expected = ChatStateHandler.ChatStateException.class)
    public void testChatStateHandler_saveState_dbException() throws DatabaseException, ChatStateHandler.ChatStateException {
        final UUID chatId = UUID.randomUUID();
        ChatState chatState = getTestChatState();
        when(this.fakeDatabaseAi.saveChatState(any(), any(), any(), any())).thenThrow(DatabaseException.class);
        this.chatStateHandler.saveState(DEVID_UUID, AIID, chatId, chatState);
        verify(this.fakeLogger).logUserExceptionEvent(anyString(), any(), anyString(), any());
    }

    private ChatState getTestChatState() {
        return new ChatState(DateTime.now(), "theTopic", "theHistory", AIID, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
    }
}


