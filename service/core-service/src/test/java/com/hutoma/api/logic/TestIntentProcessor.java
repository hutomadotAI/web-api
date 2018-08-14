package com.hutoma.api.logic;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ConditionEvaluator;
import com.hutoma.api.logic.chat.ContextVariableExtractor;
import com.hutoma.api.logic.chat.IntentProcessor;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.hutoma.api.common.TestDataHelper.getSampleAI;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestIntentProcessor {

    private IntentProcessor intentProcessor;
    private IEntityRecognizer fakeEntityRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;
    private WebHooks fakeWebHooks;
    private ConditionEvaluator fakeConditionalEvaluator;
    private ContextVariableExtractor fakeContextVariableExtractor;

    @Before
    public void setup() {
        this.fakeEntityRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.fakeConditionalEvaluator = mock(ConditionEvaluator.class);
        this.fakeContextVariableExtractor = mock(ContextVariableExtractor.class);
        this.intentProcessor = new IntentProcessor(this.fakeEntityRecognizer,
            this.fakeIntentHandler, this.fakeWebHooks, this.fakeConditionalEvaluator, this.fakeContextVariableExtractor,
            mock(ILogger.class));
    }

    @Test
    public void testIntentProcessor_variableReplacement() throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, chatId,
            "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList("val1"));
        memVar.setCurrentValue(memVar.getEntityKeys().get(0));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
            true);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            ((ChatResult) arg).setAnswer(String.format(responseTemplate, memVar.getCurrentValue()));
            return null;
        }).when(this.fakeContextVariableExtractor).extractContextVariables(any());

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));

        Assert.assertTrue(intentProcessed);
        Assert.assertEquals(String.format(responseTemplate, memVar.getCurrentValue()), chatResult.getAnswer());
    }

    @Test
    public void testIntentProcessor_variableCleared()  throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        final ChatContext context = new ChatContext();
        final String entityValue = "val1";
        context.setValue(label, entityValue);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), context);
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, chatId,
                "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList(entityValue), false, label);
        memVar.setResetOnEntry(true);
        memVar.setIsMandatory(true);
        memVar.setPrompts(Collections.singletonList("prompt 1"));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                true);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            ((ChatResult) arg).setAnswer(String.format(responseTemplate, memVar.getCurrentValue()));
            return null;
        }).when(this.fakeContextVariableExtractor).extractContextVariables(any());

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));
        Assert.assertTrue(intentProcessed);

        Assert.assertEquals(String.format(responseTemplate, "null"), chatResult.getAnswer());
    }

    @Test
    public void testIntentProcessor_variableNotCleared()  throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        final ChatContext context = new ChatContext();
        final String entityValue = "val1";
        context.setValue(label, entityValue);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), context);
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, chatId,
                "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList(entityValue), false, label);
        memVar.setIsMandatory(true);
        memVar.setPrompts(Collections.singletonList("prompt 1"));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                true);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            ((ChatResult) arg).setAnswer(String.format(responseTemplate, memVar.getCurrentValue()));
            return null;
        }).when(this.fakeContextVariableExtractor).extractContextVariables(any());

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));

        Assert.assertTrue(intentProcessed);
        Assert.assertEquals(String.format(responseTemplate, entityValue), chatResult.getAnswer());
    }
}
