package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.ChatState;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 17/05/17.
 */
public class TestChatStateHandler {

    private Database fakeDatabase;
    private ILogger fakeLogger;
    private ChatStateHandler chatStateHandler;

    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.chatStateHandler = new ChatStateHandler(fakeDatabase, fakeLogger);
    }

    @Test
    public void testChatStateHandler_getState() throws Database.DatabaseException {
        final String topic = "theTopic";
        final DateTime timestamp = DateTime.now();
        ChatState chatState = new ChatState(timestamp, topic, AIID);
        when(this.fakeDatabase.getChatState(anyString(), any())).thenReturn(chatState);
        ChatState result = this.chatStateHandler.getState(DEVID, UUID.randomUUID());
        assertChatStateEquals(chatState, result);
    }

    @Test
    public void testChatStateHandler_getState_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.getChatState(anyString(), any())).thenThrow(Database.DatabaseException.class);
        ChatState result = this.chatStateHandler.getState(DEVID, UUID.randomUUID());
        assertChatStateEquals(ChatState.getEmpty(), result);
        verify(this.fakeLogger).logUserExceptionEvent(anyString(), any(), anyString(), any());
    }

    @Test
    public void testChatStateHandler_saveState() throws Database.DatabaseException {
        final UUID chatId = UUID.randomUUID();
        ChatState chatState = new ChatState(DateTime.now(), "theTopic", AIID);
        this.chatStateHandler.saveState(DEVID, chatId, chatState);
        verify(this.fakeDatabase).saveChatState(DEVID, chatId, chatState);
    }

    @Test
    public void testChatStateHandler_saveState_dbException() throws Database.DatabaseException {
        final UUID chatId = UUID.randomUUID();
        ChatState chatState = new ChatState(DateTime.now(), "theTopic", AIID);
        when(this.fakeDatabase.saveChatState(anyString(), any(), any())).thenThrow(Database.DatabaseException.class);
        this.chatStateHandler.saveState(DEVID, chatId, chatState);
        verify(this.fakeLogger).logUserExceptionEvent(anyString(), any(), anyString(), any());
    }

    private static void assertChatStateEquals(final ChatState expected, final ChatState actual) {
        Assert.assertEquals(expected.getLockedAiid(), actual.getLockedAiid());
        Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.getTopic(), actual.getTopic());
    }
}


