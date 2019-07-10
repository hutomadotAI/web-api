package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import com.hutoma.api.logic.chat.ConditionEvaluator;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Mockito.*;

public class TestChatLogicIntents extends TestChatBase {


    /***
     * Tests an intent is recognized by the API when the backend sends it.
     */
    @Test
    public void testChat_IntentRecognized() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setLabel("label");
        mv.setCurrentValue("value");
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(miList);
        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(TestIntentLogic.getIntent());
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        MemoryIntent ri = r.getIntents().get(0);
        Assert.assertEquals(intentName, ri.getName());
    }

    /***
     * Memory intent is fulfilled.
     */
    @Test
    public void testChat_IntentFulfilled() throws
            ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = intentFulfilledSetup();
        Assert.assertFalse(mi.isFulfilled());
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertTrue(mi.isFulfilled());
        Assert.assertEquals(1, result.getResult().getIntents().size());
        Assert.assertTrue(result.getResult().getIntents().get(0).isFulfilled());
        Assert.assertTrue(result.getResult().getChatState().getCurrentIntents().isEmpty());
    }

    /***
     * Memory intent is fulfilled but some fields are not reported to the user
     */
    @Test
    public void testChat_IntentFulfilled_NoEntityKeysAndPrompts() throws
            ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        intentFulfilledSetup();
        ApiChat result = (ApiChat) getChat(0.5f);
        MemoryVariable mv = result.getResult().getIntents().get(0).getVariables().get(0);
        Assert.assertNull(mv.getEntityKeys());
        Assert.assertNull(mv.getPrompts());
    }

    private MemoryIntent intentFulfilledSetup() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setCurrentValue("a value"); // to fulfill
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName,
                0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));
        return mi;
    }

    /***
     * Memory intent updates prompt when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is the prompt
        Assert.assertEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // And timesPrompted is incremented
        Assert.assertEquals(1, mi.getVariables().get(0).getTimesPrompted());
        Assert.assertFalse(r.getChatState().getCurrentIntents().isEmpty());
    }

    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt_unfullfileldVar_exceededPrompts() throws
            ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(1, null);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // Answer with something unrelated to exhaust the prompts
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // And timesPrompted is not incremented
        Assert.assertEquals(1, mi.getVariables().get(0).getTimesPrompted());
        // we need to clear the intent if we exceeded the number of prompts
        verify(this.fakeIntentHandler, times(1)).clearIntents(any(), any());
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfullfilledVar_fulfillFromUserQuestion()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), "value"));
        }};
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(entities);
        Assert.assertFalse(mi.isFulfilled());
        ApiChat result = (ApiChat) getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(mi.isFulfilled());
        Assert.assertTrue(result.getResult().getChatState().getCurrentIntents().isEmpty());
    }

    /***
     * Memory intent is fulfilled based on variable value included in last question
     */
    @Test
    public void testChat_IntentPrompt_unfulfilledVar_variableWithNoPrompt()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, null);
        mi.getVariables().get(0).setPrompts(new ArrayList<>());
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(new ArrayList<>());
        Assert.assertFalse(mi.isFulfilled());
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
        Assert.assertFalse(mi.isFulfilled());
    }


    /***
     * Memory intent is fulfilled with one sys.any variable
     */
    @Test
    public void testChat_IntentPrompt_unfulfilled_SysAny()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {

        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("sys.any", null, true,
                Arrays.asList("a", "b"), Collections.singletonList("prompt"), 1, 0,
                true, EntityValueType.SYS, false, "label1", false, 0);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertFalse(mi.isFulfilled());
        result = (ApiChat) getChat(0.5f, "nothing to see here.");
        Assert.assertTrue(mi.getVariables().get(0).getCurrentValue().equals("nothing to see here."));
        Assert.assertTrue(mi.isFulfilled());
        Assert.assertTrue(result.getResult().getChatState().getCurrentIntents().isEmpty());
    }

    /***
     * Can use multiple sys.any variables
     */
    @Test
    public void testChat_IntentPrompt_multiple_SysAny() throws ChatBackendConnector.AiControllerException,
            ChatLogic.IntentException {

        final String intentName = "intent1";
        final String labelSysAny1 = "sysany1";
        final String labelSysAny2 = "sysany2";
        MemoryVariable mv1 = new MemoryVariable("sys.any", null, true,
                null, Collections.singletonList("prompt1"), 3, 0,
                true, EntityValueType.SYS, false, labelSysAny1, false, 0);
        MemoryVariable mv2 = new MemoryVariable("sys.any", null, true,
                null, Collections.singletonList("prompt2"), 3, 0,
                true, EntityValueType.SYS, false, labelSysAny2, false, 1);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Arrays.asList(mv1, mv2));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));

        // First question triggers the intent
        ApiChat result = (ApiChat) getChat(0.5f, "nothing to see here");
        // The response is the 1st sys.any prompt
        Assert.assertEquals(mv1.getPrompts().get(0), result.getResult().getAnswer());
        // Answer with the first answer, this will fulfill the first variable
        final String answerToFirstPrompt = "answer1";
        result = (ApiChat) getChat(0.5f, answerToFirstPrompt);
        // 1st sys.any now contains the question asked
        Assert.assertEquals(answerToFirstPrompt, result.getResult().getIntents().get(0).getVariablesMap().get(labelSysAny1).getCurrentValue());
        // The response should now be the prompt for the 2nd sys.any
        Assert.assertEquals(mv2.getPrompts().get(0), result.getResult().getAnswer());
        // Intent is not fulfilled
        Assert.assertFalse(result.getResult().getIntents().get(0).isFulfilled());

        // Send the answer to the second prompt
        final String answerToSecondPrompt = "answer2";
        result = (ApiChat) getChat(0.5f, answerToSecondPrompt);
        // 2nd sys.any now contains the question asked
        Assert.assertEquals(answerToSecondPrompt, result.getResult().getIntents().get(0).getVariablesMap().get(labelSysAny2).getCurrentValue());

        // Intent is fulfilled
        Assert.assertEquals(intent.getResponses().get(0), result.getResult().getAnswer());
        Assert.assertTrue(result.getResult().getIntents().get(0).isFulfilled());
    }


    /***
     * Memory intent does not prompt after numPromps>=MaxPrompts when intent is recognized but doesn't match any entity value.
     */
    @Test
    public void testChat_IntentPrompt_NoPromptWhenZero()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(1, "currentValue");
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        // The answer is NOT the prompt
        Assert.assertNotEquals(MEMORY_VARIABLE_PROMPT, r.getAnswer());
        // And timesPrompted is decremented
        Assert.assertEquals(0, mi.getVariables().get(0).getTimesPrompted());
    }


    /***
     * Memory intent is not fulfilled when persistent variable has not been set.
     */
    @Test
    public void testChat_multiLineIntent_notFulfilledWithNonPersistedValue()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMultiEntityMemoryIntentForPrompt(3, "prompt");

        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }

        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(TestIntentLogic.getIntent());

        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());

        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any()))
                .thenReturn(Collections.singletonList(mi));

        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(entities);
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        // Is not fulfilled.
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());
    }

    /***
     * Memory intent is fulfilled after it's prompted once
     */
    @Test
    public void testChat_multiLineIntent_fulfilled()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        MemoryIntent mi = getMemoryIntentForPrompt(3, "prompt");
        final double DOUBLE_EPSILON = 1e-15;
        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }
        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify score, and that it is in chat context
        Assert.assertEquals(EMB_CHAT_SCORE, r.getScore(), DOUBLE_EPSILON);
        Assert.assertEquals(EMB_CHAT_SCORE, r.getChatState().getChatContext().getIntentScore(), DOUBLE_EPSILON);

        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());
        // Verify answer is the prompt for the first variable
        Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        Assert.assertEquals(1, r.getChatState().getCurrentIntents().size());
        Assert.assertEquals(mi, r.getChatState().getCurrentIntents().get(0));

        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(any(), any())).thenReturn(entities);
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        // Is fulfilled
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
        // Intent has the entity with currentValue set to what we've defined
        Assert.assertEquals(varValue, r.getIntents().get(0).getVariables().get(0).getCurrentValue());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        // Score is as before
        Assert.assertEquals(EMB_CHAT_SCORE, r.getScore(), DOUBLE_EPSILON);
        Assert.assertTrue(r.getChatState().getCurrentIntents().isEmpty());
    }

    /***
     * Memory intent is not fulfilled after exhausting all prompts
     */
    @Test
    public void testChat_multiLineIntent_promptsExhausted()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final int maxPrompts = 3;
        MemoryIntent mi = getMemoryIntentForPrompt(maxPrompts, "prompt");
        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }

        // Exhaust prompts
        for (int n = 1; n <= maxPrompts; n++) {
            ApiResult result = getChat(0.5f, "nothing to see here.");
            ChatResult r = ((ApiChat) result).getResult();
            // Verify answer is intent prompt and intent is not fulfilled
            Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
            Assert.assertFalse(r.getIntents().get(0).isFulfilled());
            Assert.assertEquals(n, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
            Assert.assertEquals(maxPrompts, r.getIntents().get(0).getVariables().get(0).getTimesToPrompt());
        }

        // Next answer should exit intent handling and go through normal chat processing
        final String embAnswer = "emb answer";
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(null);
        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setScore(0.9f);
        chatResult.setAnswer(embAnswer);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(getChatResultMap(AIID, chatResult));
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertNull(r.getIntents());
        Assert.assertEquals(embAnswer, r.getAnswer());
    }

    /**
     * Tests an intent with no variables doesn't trigger calls to the entity recognizer
     */
    @Test
    public void testChat_intent_noVariables_entityRecognizerNotCalled()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intentA";
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.emptyList());
        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(TestIntentLogic.getIntent());
        setupFakeChat(0.9d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any(), any())).thenReturn(mi);
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeRecognizer, never()).retrieveEntities(any(), any());
        Assert.assertTrue(r.getIntents().get(0).getVariables().isEmpty());
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
    }

    /***
     * Memory intent is not fulfilled after exhausting all prompts nad it gets reset
     */
    @Test
    public void testChat_multiVariable_promptsExhausted_intentReset()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException, ChatStateHandler.ChatStateException {
        final int maxPrompts = 1;
        MemoryVariable mv1 = new MemoryVariable(
                "var1",
                null,
                true,
                Collections.singletonList("trigger"),
                Collections.singletonList("prompt"),
                maxPrompts,
                0,
                false,
                EntityValueType.LIST,
                false,
                "label1",
                false,
                0);
        MemoryVariable mv2 = new MemoryVariable(
                "var2",
                null,
                true,
                Collections.singletonList("trigger"),
                Collections.singletonList("prompt"),
                maxPrompts,
                0,
                false,
                EntityValueType.LIST,
                false,
                "label2",
                false,
                1);
        MemoryIntent mi = new MemoryIntent("intent", AIID, UUID.randomUUID(), Arrays.asList(mv1, mv2), false);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any(), any())).thenReturn(mi);
        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(TestIntentLogic.getIntent());
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + "intent1", 0.0d, AIMLRESULT);

        ApiResult result = null;
        // Exhaust prompts
        for (int n = 1; n <= maxPrompts; n++) {
            result = getChat(0.5f, "nothing to see here.");
            ChatResult r = ((ApiChat) result).getResult();
            MemoryIntent intent = r.getIntents().get(0);
            // Verify answer is intent prompt and intent is not fulfilled
            Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
            Assert.assertFalse(intent.isFulfilled());
            Assert.assertEquals(n, intent.getVariables().get(0).getTimesPrompted());
            Assert.assertEquals(maxPrompts, intent.getVariables().get(0).getTimesToPrompt());
        }
        // Answer to the last prompt with something unrelated, so that the intent is cleared
        result = getChat(0.5f, "nothing to see here.");
        verify(this.fakeIntentHandler, times(1)).clearIntents(any(), any());
    }

    /***
     * Test that we prompt correctly when using multiple variables from the same entity type.
     * If there are variables with the same entity type, then it forces prompting these ones first to disambiguate.
     */
    @Test
    public void testChat_intent_sameEntity_multipleVars_promptsForDisambiguationFirst()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String sameEntityName = "sameEntityName";
        MemoryVariable mv1 = new MemoryVariable("entity1", null, true, Collections.singletonList("1"),
                Collections.singletonList("prompt1"), 2, 0, false, EntityValueType.LIST, false, "label1", false, 0);
        MemoryVariable mv2 = new MemoryVariable(sameEntityName, null, true, Arrays.asList("a", "b"),
                Collections.singletonList("prompt2"), 2, 0, false, EntityValueType.LIST, false, "label2", false, 1);
        MemoryVariable mv3 = new MemoryVariable(sameEntityName, null, true, Collections.singletonList("c"),
                Collections.singletonList("prompt3"), 1, 0, false, EntityValueType.LIST, false, "label3", false, 2);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Arrays.asList(mv1, mv2, mv3));


        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(mi));

        // Issue the first chat request
        ChatResult result = ((ApiChat) getChat(0.5f)).getResult();
        Assert.assertEquals(mv2.getPrompts().get(0), result.getAnswer());
        List<MemoryVariable> varsRequested = result.getIntents().get(0).getVariables().stream()
                .filter(MemoryVariable::isRequested)
                .collect(Collectors.toList());
        // We expect the first variable that has shared entity type to be prompted
        Assert.assertEquals(mv2.getPrompts().get(0), result.getAnswer());
        Assert.assertEquals(1, varsRequested.size());
        Assert.assertEquals(mv2.getLabel(), varsRequested.get(0).getLabel());

        // Fulfill this variable
        when(this.fakeRecognizer.retrieveEntities(any(), any()))
                .thenReturn(Collections.singletonList(new Pair<>(mv2.getName(), mv2.getEntityKeys().get(0))));

        // Issue the second chat request
        result = ((ApiChat) getChat(0.5f)).getResult();
        varsRequested = result.getIntents().get(0).getVariables().stream()
                .filter(MemoryVariable::isRequested)
                .collect(Collectors.toList());
        // We now expect the second variable that has shared entity type to be prompted
        Assert.assertEquals(mv3.getPrompts().get(0), result.getAnswer());
        Assert.assertEquals(1, varsRequested.size());
        Assert.assertEquals(mv3.getLabel(), varsRequested.get(0).getLabel());
        // Previous requested variables retain value
        Assert.assertEquals(mv2.getEntityKeys().get(0), mv2.getCurrentValue());

        // Fulfill this variable
        when(this.fakeRecognizer.retrieveEntities(any(), any()))
                .thenReturn(Collections.singletonList(new Pair<>(mv3.getName(), mv3.getEntityKeys().get(0))));

        // Issue the third chat request
        result = ((ApiChat) getChat(0.5f)).getResult();
        varsRequested = result.getIntents().get(0).getVariables().stream()
                .filter(MemoryVariable::isRequested)
                .collect(Collectors.toList());
        // Only now we expect the entity with only one entry
        Assert.assertEquals(mv1.getPrompts().get(0), result.getAnswer());
        Assert.assertEquals(1, varsRequested.size());
        Assert.assertEquals(mv1.getLabel(), varsRequested.get(0).getLabel());
        // Previous requested variables retain value
        Assert.assertEquals(mv2.getEntityKeys().get(0), mv2.getCurrentValue());
        Assert.assertEquals(mv3.getEntityKeys().get(0), mv3.getCurrentValue());
    }

    @Test
    public void testChat_intent_contextIn_appliedWhenIntentRecognized()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String contextInVarName = "varIn";
        final String contextInVarValue = "valueIn";
        final String intentName = "intent";
        setupContextVariablesChatTest(intentName, contextInVarName, contextInVarValue, null, null);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        ChatContext ctx = r.getChatState().getChatContext();
        Assert.assertEquals(contextInVarValue, ctx.getValue(contextInVarName));
    }

    @Test
    public void testChat_intent_contextOut_appliedWhenIntentRecognized()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String contextOutVarName = "varOut";
        final String contextOutVarValue = "valueIOut";
        final String intentName = "intent";
        setupContextVariablesChatTest(intentName, null, null, contextOutVarName, contextOutVarValue);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        ChatContext ctx = r.getChatState().getChatContext();
        Assert.assertEquals(contextOutVarValue, ctx.getValue(contextOutVarName));
    }

    @Test
    public void testChat_intent_contextOutAndOut_appliedWhenIntentRecognized()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String contextInVarName = "varIn";
        final String contextInVarValue = "valueIn";
        final String contextOutVarName = "varOut";
        final String contextOutVarValue = "valueOut";
        final String intentName = "intent";
        setupContextVariablesChatTest(intentName, contextInVarName, contextInVarValue, contextOutVarName, contextOutVarValue);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        ChatContext ctx = r.getChatState().getChatContext();
        Assert.assertEquals(contextInVarValue, ctx.getValue(contextInVarName));
        // intent is fulfilled so context_out should be included
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
        Assert.assertEquals(contextOutVarValue, ctx.getValue(contextOutVarName));
    }

    @Test
    public void testChat_intent_conditions_true()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent";
        IntentVariableCondition condition = new IntentVariableCondition("a", IntentConditionOperator.SET, null);
        ApiIntent intent = setupContextVariablesChatTest(intentName, null, null, null, null);
        intent.setConditionsIn(Collections.singletonList(condition));
        when(this.fakeConditionEvaluator.evaluate(any())).thenReturn(new ConditionEvaluator.Results(
                Collections.singletonList(new ConditionEvaluator.Result(condition, ConditionEvaluator.ResultType.PASSED))));
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(intentName, r.getIntents().get(0).getName());
    }

    @Test
    public void testChat_intent_conditions_false()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        testChat_intent_conditions_false_message(null);
    }

    @Test
    public void testChat_intent_conditions_false_returnsMessage()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        testChat_intent_conditions_false_message("fallthrough message");
    }

    @Test
    public void testChat_intent_contextOut_notAppliedWhenIntentNotFulfilled()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent";
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setContextOut(ImmutableMap.of("aa", "bb"));
        MemoryVariable mv = new MemoryVariable("entity1", null, true, Collections.singletonList("1"),
                Collections.singletonList("prompt1"), 2, 0, false, EntityValueType.LIST, false, "label1", false, 0);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(miList);
        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(intent);
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        ChatContext ctx = r.getChatState().getChatContext();
        // intent is NOT fulfilled so context_out should NOT be included
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());
        Assert.assertFalse(ctx.isSet(MemoryIntentHandler.getPrefixedVariableName(intentName, "aa")));
    }

    @Test
    public void testChat_intent_variableLifetime_defaults() throws ChatStateHandler.ChatStateException {
        final String varName = "var";
        ChatContext ctx = new ChatContext();
        ctx.setValue(varName, "value", ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
        ChatState state = new ChatState(DateTime.now(), null, null, null, 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), ctx);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        Assert.assertEquals(-1, ctx.getVariable(varName).getLifespanTurns());
        getChat(0.5f);
        Assert.assertNotNull(ctx.getValue(varName));
        Assert.assertEquals(-1, ctx.getVariable(varName).getLifespanTurns());
    }

    @Test
    public void testChat_intent_variableLifetime_decrements_and_getsCleared() throws ChatStateHandler.ChatStateException {
        final String varName = "var";
        ChatContext ctx = new ChatContext();
        // Set the lifetime of the variable to 3 turns
        ctx.setValue(varName, "value", 3);
        ChatState state = new ChatState(DateTime.now(), null, null, null, 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), ctx);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        // First turn
        getChat(0.5f);
        // lifetime should now be next 2 turns
        Assert.assertEquals(2, ctx.getVariable(varName).getLifespanTurns());
        // Second turn
        getChat(0.5f);
        // lifetime should now be next 1 turn
        Assert.assertEquals(1, ctx.getVariable(varName).getLifespanTurns());
        // Third turn
        getChat(0.5f);
        // Variable should be now cleared on the next turn
        Assert.assertEquals(0, ctx.getVariable(varName).getLifespanTurns());
        // Fourth turn
        getChat(0.5f);
        // Validate variable has been cleared
        Assert.assertNull(ctx.getVariable(varName));
    }

    @Test
    public void testChat_intent_conditionOut_chainIntent() throws ChatLogic.IntentException, DatabaseException {
        ChatResult r = testIntentConditionOut("intent1", "intent2", ConditionEvaluator.ResultType.PASSED);
        Assert.assertEquals("intent2", r.getIntents().get(0).getName());
    }

    @Test
    public void testChat_intent_conditionOut_conditionNotMet_noChaining() throws ChatLogic.IntentException, DatabaseException {
        ChatResult r = testIntentConditionOut("intent1", "intent2", ConditionEvaluator.ResultType.FAILED);
        Assert.assertEquals("intent1", r.getIntents().get(0).getName());
    }

    @Test
    public void testChat_intent_conditionOut_loopDetection_sameIntent() throws ChatLogic.IntentException, DatabaseException {
        ChatResult r = testIntentConditionOut("intent1", "intent1", ConditionEvaluator.ResultType.PASSED);
        Assert.assertEquals("intent1", r.getIntents().get(0).getName());
        // we don't call the same intent again
        verify(this.fakeIntentHandler, never()).buildMemoryIntentFromIntentName(any(), any(), anyString(), any());
    }

    @Test
    public void testChat_intent_conditionOut_loopDetection_deeper() throws ChatLogic.IntentException, DatabaseException {

        final int numIntents = 5;

        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setLabel("label");
        mv.setCurrentValue("value");
        IntentVariableCondition condition = new IntentVariableCondition("var", IntentConditionOperator.SET, "");

        List<ApiIntent> intents = new ArrayList<>();
        List<MemoryIntent> memoryIntents = new ArrayList<>();
        for (int i = 0; i < numIntents; i++) {
            ApiIntent intent = new ApiIntent("intent" + i, "", "");
            intent.addResponse("response" + i);
            String triggerIntent = "intent" + (i == numIntents - 1 ? 0 : i + 1);
            IntentOutConditional intentOutConditional = new IntentOutConditional(triggerIntent, Collections.singletonList(condition));
            List<IntentOutConditional> intentOutConditionals = Collections.singletonList(intentOutConditional);
            intent.setIntentOutConditionals(intentOutConditionals);
            intents.add(intent);

            memoryIntents.add(new MemoryIntent(intent.getIntentName(), AIID, CHATID, Collections.singletonList(mv)));

            when(this.fakeIntentHandler.getIntent(AIID, intents.get(i).getIntentName())).thenReturn(intents.get(i));
            when(this.fakeIntentHandler.buildMemoryIntentFromIntentName(DEVID_UUID, AIID, intents.get(i).getIntentName(), CHATID))
                    .thenReturn(memoryIntents.get(i));
        }

        when(this.fakeConditionEvaluator.evaluate(any())).thenReturn(new ConditionEvaluator.Results(
                Collections.singletonList(new ConditionEvaluator.Result(condition, ConditionEvaluator.ResultType.PASSED))));

        OngoingStubbing stub = when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any()));
        for (int i = 0; i < numIntents; i++) {
            stub = stub.thenReturn(Collections.singletonList(memoryIntents.get(i)));
        }

        ApiResult result = getChat(0.5f);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        // Check recursion message - last one points back to first one
        Assert.assertEquals("Recursion detected for intent intent0", result.getStatus().getInfo());
        verify(this.fakeIntentHandler, times(numIntents + 1)).getCurrentIntentsStateForChat(any());
    }

    @Test
    public void testChat_triggerIntent_Success() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(new ApiIntent("intentName", null, null));
        when(this.fakeDatabaseEntitiesIntents.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        ApiResult result = this.chatLogic.triggerIntent(AIID, DEVID_UUID, CHATID.toString(), "intentName");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testChat_triggerIntent_IncorrectName() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(null);
        when(this.fakeDatabaseEntitiesIntents.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        ApiResult result = this.chatLogic.triggerIntent(AIID, DEVID_UUID, CHATID.toString(), "intentName");
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testChat_triggerIntent_IncorrectDevId() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(new ApiIntent("intentName", null, null));
        when(this.fakeDatabaseEntitiesIntents.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        ApiResult result = this.chatLogic.triggerIntent(AIID, DEVID_UUID, CHATID.toString(), "intentName");
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testChat_triggerIntent_promptsUser() throws DatabaseException, ChatStateHandler.ChatStateException {
        ApiIntent intent = new ApiIntent("intentName", null, null);
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeDatabaseEntitiesIntents.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        MemoryVariable mv = new MemoryVariable("entity1", null, true, Collections.singletonList("1"),
                Collections.singletonList("prompt1"), 2, 0, false, EntityValueType.LIST, false, "label1", false, 1);
        List<MemoryIntent> intentList = new ArrayList<>();
        intentList.add(new MemoryIntent("intentName", AIID, CHATID, Collections.singletonList(mv)));
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(intentList);
        ApiResult result = this.chatLogic.triggerIntent(AIID, DEVID_UUID, CHATID.toString(), "intentName");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());

        ApiResult response = this.chatLogic.chat(AIID, DEVID_UUID, "what?", CHATID.toString(), null);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus().getCode());
    }

    @Test
    public void testChat_IntentPrompt_multiple_reverseOrder() throws ChatBackendConnector.AiControllerException,
            ChatLogic.IntentException {

        final String intentName = "intent1";
        final String firstentity = "firstentity";
        final String secondentity = "secondentity";
        MemoryVariable firstVariable = new MemoryVariable("sys.any", null, true,
                null, Collections.singletonList("prompt1"), 3, 0,
                true, EntityValueType.SYS, false, firstentity, false, 1);
        MemoryVariable secondVariable = new MemoryVariable("sys.any", null, true,
                null, Collections.singletonList("prompt2"), 3, 0,
                true, EntityValueType.SYS, false, secondentity, false, 0);
        MemoryIntent memoryIntent = new MemoryIntent(intentName, AIID, CHATID, Arrays.asList(firstVariable, secondVariable));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(memoryIntent);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(Collections.singletonList(memoryIntent));

        ApiChat result = (ApiChat) getChat(0.5f, "start intent");

        // Check that the second entity has been prompted first, per the index order.
        Assert.assertEquals(secondVariable.getPrompts().get(0), result.getResult().getAnswer());
        final String answerToFirstPrompt = "answer2";
        result = (ApiChat) getChat(0.5f, answerToFirstPrompt);
        Assert.assertEquals(answerToFirstPrompt, result.getResult().getIntents().get(0).getVariablesMap().get(secondentity).getCurrentValue());
        Assert.assertEquals(firstVariable.getPrompts().get(0), result.getResult().getAnswer());

        // Intent is not fulfilled
        Assert.assertFalse(result.getResult().getIntents().get(0).isFulfilled());

        // Check that the first entity has been prompted second.
        final String answerToSecondPrompt = "answer1";
        result = (ApiChat) getChat(0.5f, answerToSecondPrompt);
        Assert.assertEquals(answerToSecondPrompt, result.getResult().getIntents().get(0).getVariablesMap().get(firstentity).getCurrentValue());

        Assert.assertEquals(intent.getResponses().get(0), result.getResult().getAnswer());
        Assert.assertTrue(result.getResult().getIntents().get(0).isFulfilled());
    }

    private ChatResult testIntentConditionOut(final String intent1Name, final String intent2Name, final ConditionEvaluator.ResultType evaluationResult)
            throws ChatLogic.IntentException, DatabaseException {
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        mv.setLabel("label");
        mv.setCurrentValue("value");

        ApiIntent intent2 = new ApiIntent(intent2Name, "", "");
        intent2.addResponse("response2");
        MemoryIntent memoryIntent2 = new MemoryIntent(intent2.getIntentName(), AIID, CHATID, Collections.singletonList(mv));

        ApiIntent intent1 = new ApiIntent(intent1Name, "", "");
        intent1.addResponse("response1");
        IntentVariableCondition condition = new IntentVariableCondition("var", IntentConditionOperator.SET, "");
        IntentOutConditional intentOutConditional = new IntentOutConditional("intent2", Collections.singletonList(condition));
        List<IntentOutConditional> intentOutConditionals = Collections.singletonList(intentOutConditional);
        intent1.setIntentOutConditionals(intentOutConditionals);

        MemoryIntent memoryIntent1 = new MemoryIntent(intent1.getIntentName(), AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(memoryIntent1);

        when(this.fakeIntentHandler.getIntent(AIID, intent1.getIntentName())).thenReturn(intent1);
        when(this.fakeIntentHandler.getIntent(AIID, intent2.getIntentName())).thenReturn(intent2);
        when(this.fakeIntentHandler.buildMemoryIntentFromIntentName(DEVID_UUID, AIID, intent2.getIntentName(), CHATID)).thenReturn(memoryIntent2);
        when(this.fakeConditionEvaluator.evaluate(any())).thenReturn(new ConditionEvaluator.Results(
                Collections.singletonList(new ConditionEvaluator.Result(condition, evaluationResult))));
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(memoryIntent1);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(miList).thenReturn(Collections.singletonList(memoryIntent2));

        ApiResult result = getChat(0.5f);
        return ((ApiChat) result).getResult();
    }

    private ApiIntent setupContextVariablesChatTest(final String intentName,
                                                    final String contextInVarName, final String contextInVarValue,
                                                    final String contextOutVarName, final String contextOutVarValue)
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        ApiIntent intent = TestIntentLogic.getIntent();
        if (contextInVarName != null) {
            intent.setContextIn(ImmutableMap.of(contextInVarName, contextInVarValue));
        }
        if (contextOutVarName != null) {
            intent.setContextOut(ImmutableMap.of(contextOutVarName, contextOutVarValue));
        }
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.emptyList());
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString(), any())).thenReturn(mi);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(miList);
        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(intent);
        return intent;
    }

    private void testChat_intent_conditions_false_message(final String expectedFallthroughMessage)
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent";
        IntentVariableCondition condition = new IntentVariableCondition("a", IntentConditionOperator.SET, null);
        ApiIntent intent = setupContextVariablesChatTest(intentName, null, null, null, null);
        intent.setConditionsIn(Collections.singletonList(condition));
        intent.setConditionsFallthroughMessage(expectedFallthroughMessage);
        when(this.fakeConditionEvaluator.evaluate(any())).thenReturn(new ConditionEvaluator.Results(
                Collections.singletonList(new ConditionEvaluator.Result(condition, ConditionEvaluator.ResultType.FAILED))));
        ApiResult result = getChat(0.5f);
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertNull(r.getIntents());
        Assert.assertEquals(expectedFallthroughMessage == null ? ChatDefaultHandler.COMPLETELY_LOST_RESULT : expectedFallthroughMessage,
                r.getAnswer());
    }
}
