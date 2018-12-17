package com.hutoma.api.logic;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatEmbHandler;
import com.hutoma.api.logic.chat.ContextVariableExtractor;
import com.hutoma.api.logic.chat.IntentProcessor;
import com.hutoma.api.memory.IMemoryIntentHandler;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.getSampleAI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestChatEmbHandler {

    private ChatEmbHandler chatEmbHandler;
    private IMemoryIntentHandler fakeIntentHandler;
    private IntentProcessor fakeIntentProcessor;
    private ContextVariableExtractor fakeContextVariableExtractor;
    private AIChatServices fakeChatServices;

    @Before
    public void setup() {
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeIntentProcessor = mock(IntentProcessor.class);
        this.fakeContextVariableExtractor = mock(ContextVariableExtractor.class);
        this.fakeChatServices = mock(AIChatServices.class);
        this.chatEmbHandler = new ChatEmbHandler(this.fakeIntentHandler, this.fakeIntentProcessor,
                this.fakeContextVariableExtractor, mock(ILogger.class));
    }

    @Test
    public void testEmbHandler_variableReplacement() throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException,
            ChatLogic.IntentException, WebHooks.WebHookException {
        final String responseTemplate = "response %s";
        final String variableValue = "myValue";
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, UUID.randomUUID(), "question", null);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        chatState.setAiChatServices(this.fakeChatServices);
        ChatResult chatResult = new ChatResult("variable $var1");
        chatResult.setChatState(chatState);
        chatResult.setScore(0.9);

        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            ((ChatResult) arg).setAnswer(String.format(responseTemplate, variableValue));
            return null;
        }).when(this.fakeContextVariableExtractor).extractContextVariables(any());

        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(new HashMap<UUID, ChatResult>() {{put(AIID, chatResult);}});
        when(this.fakeChatServices.getMinPMap()).thenReturn(new HashMap<UUID, Double>(){{put(AIID, 0.1);}});

        this.chatEmbHandler.doWork(chatInfo, chatResult, LogMap.map("a", "b"));

        Assert.assertEquals(String.format(responseTemplate, variableValue), chatResult.getAnswer());
    }
}
