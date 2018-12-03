package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ConditionEvaluator;
import com.hutoma.api.logic.chat.ContextVariableExtractor;
import com.hutoma.api.logic.chat.IntentProcessor;
import com.hutoma.api.logic.chat.WebhookHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.hutoma.api.common.TestDataHelper.getSampleAI;
import static org.mockito.Mockito.*;

public class TestIntentProcessor {

    private IntentProcessor intentProcessor;
    private IEntityRecognizer fakeEntityRecognizer;
    private IMemoryIntentHandler fakeIntentHandler;
    private WebHooks fakeWebHooks;
    private ConditionEvaluator fakeConditionalEvaluator;
    private ContextVariableExtractor fakeContextVariableExtractor;
    private FeatureToggler fakeFeatureToggler;
    private Config fakeConfig;
    private WebhookHandler fakeWebhookHandler;

    @Before
    public void setup() {
        this.fakeEntityRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.fakeConditionalEvaluator = mock(ConditionEvaluator.class);
        this.fakeContextVariableExtractor = mock(ContextVariableExtractor.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);
        this.fakeConfig = mock(Config.class);
        this.fakeWebhookHandler = mock(WebhookHandler.class);
        this.intentProcessor = new IntentProcessor(this.fakeEntityRecognizer,
                this.fakeIntentHandler, this.fakeWebHooks, this.fakeConditionalEvaluator, this.fakeContextVariableExtractor,
                this.fakeWebhookHandler, mock(ILogger.class), this.fakeConfig, this.fakeFeatureToggler);
    }

    @Test
    public void testIntentProcessor_variableReplacement() throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
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
    public void testIntentProcessor_followUpOnSuccess() throws WebHooks.WebHookException, ChatLogic.IntentException, DatabaseException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList("val1"));
        memVar.setCurrentValue(memVar.getEntityKeys().get(0));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                true);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));

        List<IntentVariableCondition> conditions = new ArrayList<>();
        IntentVariableCondition condition = new IntentVariableCondition("test", IntentConditionOperator.NOT_SET, null);
        conditions.add(condition);
        IntentOutConditional followupCondition = new IntentOutConditional("followUp", conditions);
        List<IntentOutConditional> outConditionals = new ArrayList<>();
        outConditionals.add(followupCondition);

        intent.setIntentOutConditionals(outConditionals);
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        ConditionEvaluator.Result result = new ConditionEvaluator.Result(null, null);
        result.setResult(ConditionEvaluator.ResultType.PASSED);
        List<ConditionEvaluator.Result> resultsList = new ArrayList<>();
        resultsList.add(result);
        ConditionEvaluator.Results results = new ConditionEvaluator.Results(resultsList);
        when(this.fakeConditionalEvaluator.evaluate(any())).thenReturn(results);
        when(this.fakeIntentHandler.buildMemoryIntentFromIntentName(any(), any(), any(), any())).thenReturn(
                new MemoryIntent("nextIntent", TestDataHelper.AIID, chatId, Collections.emptyList()));

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            ((ChatResult) arg).setAnswer(String.format(responseTemplate, memVar.getCurrentValue()));
            return null;
        }).when(this.fakeContextVariableExtractor).extractContextVariables(any());

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));

        Assert.assertTrue(intentProcessed);
        Assert.assertTrue(!chatResult.getChatState().getCurrentIntents().isEmpty());
        Assert.assertEquals(String.format(responseTemplate, memVar.getCurrentValue()), chatResult.getAnswer());
    }

    @Test
    public void testIntentProcessor_fallbackOnFailure() throws WebHooks.WebHookException, ChatLogic.IntentException, DatabaseException {
        UUID chatId = UUID.randomUUID();
        final String label = "label1";
        final String nextIntentName = "nextIntent";
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList("val1"));
        memVar.setIsMandatory(true);
        memVar.setPrompts(Collections.singletonList("prompt"));
        memVar.setRequested(true);
        //memVar.setCurrentValue(memVar.getEntityKeys().get(0));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                false);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));

        List<IntentVariableCondition> conditions = new ArrayList<>();
        IntentVariableCondition condition = new IntentVariableCondition("test", IntentConditionOperator.NOT_SET, null);
        conditions.add(condition);
        IntentOutConditional followupCondition = new IntentOutConditional("followUp", conditions);
        List<IntentOutConditional> outConditionals = new ArrayList<>();
        outConditionals.add(followupCondition);

        List<Pair<String, String>> entities = new ArrayList<>();
        entities.add(new Pair<String, String>("none", "none"));
        when(this.fakeEntityRecognizer.retrieveEntities(any(), any(), any())).thenReturn(entities);

        intent.setIntentOutConditionals(outConditionals);
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        ConditionEvaluator.Result result = new ConditionEvaluator.Result(null, null);
        result.setResult(ConditionEvaluator.ResultType.PASSED);
        List<ConditionEvaluator.Result> resultsList = new ArrayList<>();
        resultsList.add(result);
        ConditionEvaluator.Results results = new ConditionEvaluator.Results(resultsList);
        when(this.fakeConditionalEvaluator.evaluate(any())).thenReturn(results);
        when(this.fakeIntentHandler.buildMemoryIntentFromIntentName(any(), any(), any(), any())).thenReturn(
                new MemoryIntent(nextIntentName, TestDataHelper.AIID, chatId, Collections.emptyList()));
        when(this.fakeFeatureToggler.getStateForAiid(any(), any(), any())).thenReturn(FeatureToggler.FeatureState.T1);

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));

        Assert.assertTrue(intentProcessed);
        Assert.assertTrue(!chatResult.getChatState().getCurrentIntents().isEmpty());
        Assert.assertEquals(nextIntentName, chatResult.getChatState().getCurrentIntents().get(0).getName());
    }

    @Test
    public void testIntentProcessor_fallbackOnFailureConditionBlocked() throws WebHooks.WebHookException, ChatLogic.IntentException, DatabaseException {
        UUID chatId = UUID.randomUUID();
        final String label = "label1";
        final String nextIntentName = "nextIntent";
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), new ChatContext());
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList("val1"));
        memVar.setIsMandatory(true);
        memVar.setPrompts(Collections.singletonList("prompt"));
        memVar.setRequested(true);
        //memVar.setCurrentValue(memVar.getEntityKeys().get(0));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                true);
        ApiIntent intent = new ApiIntent("name", "", "");
        intent.setResponses(Collections.singletonList(""));

        List<IntentVariableCondition> conditions = new ArrayList<>();
        IntentVariableCondition condition = new IntentVariableCondition("test", IntentConditionOperator.NOT_SET, null);
        conditions.add(condition);
        IntentOutConditional followupCondition = new IntentOutConditional("followUp", conditions);
        List<IntentOutConditional> outConditionals = new ArrayList<>();
        outConditionals.add(followupCondition);

        List<Pair<String, String>> entities = new ArrayList<>();
        entities.add(new Pair<String, String>("label1", "label1"));
        when(this.fakeEntityRecognizer.retrieveEntities(any(), any(), any())).thenReturn(entities);
        when(this.fakeFeatureToggler.getStateForAiid(any(), any(), any())).thenReturn(FeatureToggler.FeatureState.T1);

        intent.setIntentOutConditionals(outConditionals);
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        ConditionEvaluator.Result result = new ConditionEvaluator.Result(null, null);
        result.setResult(ConditionEvaluator.ResultType.FAILED);
        List<ConditionEvaluator.Result> resultsList = new ArrayList<>();
        resultsList.add(result);
        ConditionEvaluator.Results results = new ConditionEvaluator.Results(resultsList);
        when(this.fakeConditionalEvaluator.evaluate(any())).thenReturn(results);
        when(this.fakeIntentHandler.buildMemoryIntentFromIntentName(any(), any(), any(), any())).thenReturn(
                new MemoryIntent(nextIntentName, TestDataHelper.AIID, chatId, Collections.emptyList()));

        ChatResult chatResult = new ChatResult("nothing to see here");
        chatResult.setChatState(chatState);

        boolean intentProcessed = this.intentProcessor.processIntent(chatInfo, TestDataHelper.AIID, memoryIntent, chatResult, LogMap.map("a", "b"));

        Assert.assertTrue(intentProcessed);
        Assert.assertTrue(chatResult.getChatState().getCurrentIntents().isEmpty());
    }

    @Test
    public void testIntentProcessor_variableCleared() throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        final ChatContext context = new ChatContext();
        final String entityValue = "val1";
        context.setValue(label, entityValue, ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), context);
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
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

        Assert.assertFalse(context.isSet(label));

        Assert.assertEquals(String.format(responseTemplate, "null"), chatResult.getAnswer());
    }

    @Test
    public void testIntentProcessor_variableNotCleared() throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        final ChatContext context = new ChatContext();
        final String entityValue = "val1";
        context.setValue(label, entityValue, ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), context);
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
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

    @Test
    public void testIntentProcessor_existingValueNotPromptedOnRequired() throws ChatLogic.IntentException, WebHooks.WebHookException {
        UUID chatId = UUID.randomUUID();
        final String responseTemplate = "response %s";
        final String label = "label1";
        final ChatContext context = new ChatContext();
        final String entityValue = "val1";
        context.setValue(label, entityValue, ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        ChatState chatState = new ChatState(DateTime.now(),
                null, null, UUID.randomUUID(), new HashMap<>(), 0.1d, ChatHandoverTarget.Ai,
                getSampleAI(), context);
        ChatRequestInfo chatInfo = new ChatRequestInfo(TestDataHelper.AI_IDENTITY, chatId, "question", null);
        MemoryVariable memVar = new MemoryVariable(label, Collections.singletonList(entityValue), false, label);
        memVar.setIsMandatory(true);
        memVar.setPrompts(Collections.singletonList("prompt 1"));
        MemoryIntent memoryIntent = new MemoryIntent("intent", TestDataHelper.AIID, chatId, Collections.singletonList(memVar),
                false);
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
