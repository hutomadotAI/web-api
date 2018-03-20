package com.hutoma.api.logic;

import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.MemoryIntentHandler;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestChatLogicIntents extends TestChatBase {


    /***
     * Tests an intent is recognized by the API when the backend sends it.
     */
    @Test
    public void testChat_IntentRecognized() throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("var", Arrays.asList("a", "b"));
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        List<MemoryIntent> miList = Collections.singletonList(mi);
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(miList);
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
        verify(this.fakeIntentHandler).clearIntents(any());
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
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));
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
        // The intent status is updated to storage
        verify(this.fakeIntentHandler).updateStatus(mi);
        // And timesPrompted is incremented
        Assert.assertEquals(1, mi.getVariables().get(0).getTimesPrompted());
        verify(this.fakeIntentHandler, never()).clearIntents(any());
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
        verify(this.fakeIntentHandler, times(1)).clearIntents(any());
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
        ApiResult result = getChat(0.5f, "nothing to see here.");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(mi.isFulfilled());
        verify(this.fakeIntentHandler).clearIntents(any());
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
        verify(this.fakeIntentHandler, never()).updateStatus(mi);
    }


    /***
     * Memory intent is fulfilled with one sys.any variable
     */
    @Test
    public void testChat_IntentPrompt_unfulfilled_SysAny()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {

        final String intentName = "intent1";
        MemoryVariable mv = new MemoryVariable("sys.any", null, true,
                Arrays.asList("a", "b"), Collections.singletonList("prompt"), 1, 0, true, false, "label1");
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertFalse(mi.isFulfilled());
        result = (ApiChat) getChat(0.5f, "nothing to see here.");
        Assert.assertTrue(mi.getVariables().get(0).getCurrentValue().equals("nothing to see here."));
        Assert.assertTrue(mi.isFulfilled());
        verify(this.fakeIntentHandler).clearIntents(any());
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
                null, Collections.singletonList("prompt1"), 3, 0, true, false, labelSysAny1);
        MemoryVariable mv2 = new MemoryVariable("sys.any", null, true,
                null, Collections.singletonList("prompt2"), 3, 0, true, false, labelSysAny2);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Arrays.asList(mv1, mv2));
        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

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
     * Memory intent with multiple entities is fulfilled from persisted value.
     */
    @Test
    public void testChat_multiLineIntent_fulfilledFromPersistence()
            throws ChatBackendConnector.AiControllerException,
            ChatStateHandler.ChatStateException, ChatLogic.IntentException {
        MemoryIntent mi = getMultiEntityMemoryIntentForPrompt(3, "prompt");

        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }

        HashMap<String, String> entityValues = new HashMap<>();
        entityValues.put("persistent_var", "persistentValue");
        ChatState state = new ChatState(DateTime.now(), null, null, null, entityValues, 0.5d, ChatHandoverTarget.Ai);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);

        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());

        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any()))
                .thenReturn(Collections.singletonList(r.getIntents().get(0)));
        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(anyString(), any())).thenReturn(entities);
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        // Is fulfilled
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
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

        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());

        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any()))
                .thenReturn(Collections.singletonList(mi));

        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(anyString(), any())).thenReturn(entities);
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

        // Make sure all variables are clean
        for (MemoryVariable mv : mi.getVariables()) {
            mv.setCurrentValue(null);
        }
        // First question, triggers the intent but without the right entity value
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Verify intent is triggered
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        Assert.assertFalse(r.getIntents().get(0).isFulfilled());
        // Verify answer is the prompt for the first variable
        Assert.assertEquals(mi.getVariables().get(0).getPrompts().get(0), r.getAnswer());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        verify(this.fakeIntentHandler).updateStatus(mi);
        verify(this.fakeIntentHandler, never()).clearIntents(any());

        // Second question, the answer to the prompt with the right entity value
        final String varValue = "_value_";
        List<Pair<String, String>> entities = new ArrayList<Pair<String, String>>() {{
            this.add(new Pair<>(mi.getVariables().get(0).getName(), varValue));
        }};
        when(this.fakeRecognizer.retrieveEntities(anyString(), any())).thenReturn(entities);
        result = getChat(0.5f, "nothing to see here.");
        r = ((ApiChat) result).getResult();
        Assert.assertEquals(1, r.getIntents().size());
        Assert.assertEquals(mi.getName(), r.getIntents().get(0).getName());
        // Is fulfilled
        Assert.assertTrue(r.getIntents().get(0).isFulfilled());
        // Intent has the entity with currentValue set to what we've defined
        Assert.assertEquals(varValue, r.getIntents().get(0).getVariables().get(0).getCurrentValue());
        Assert.assertEquals(1, r.getIntents().get(0).getVariables().get(0).getTimesPrompted());
        // Score is 1.0
        Assert.assertEquals(1.0d, r.getScore(), 0.00000001d);
        verify(this.fakeIntentHandler).updateStatus(mi);
        verify(this.fakeIntentHandler).clearIntents(any());
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
        final String wnetAnswer = "wnet answer";
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(null);
        ChatResult wnetResult = new ChatResult("Hi");
        wnetResult.setScore(0.9f);
        wnetResult.setAnswer(wnetAnswer);
        when(this.fakeChatServices.awaitBackend(BackendServerType.WNET)).thenReturn(getChatResultMap(AIID, wnetResult));
        ApiResult result = getChat(0.5f, "nothing to see here.");
        ChatResult r = ((ApiChat) result).getResult();
        Assert.assertNull(r.getIntents());
        Assert.assertEquals(wnetAnswer, r.getAnswer());
    }

    /**
     * Tests an intent with no variables doesn't trigger calls to the entity recognizer
     */
    @Test
    public void testChat_intent_noVariables_entityRecognizerNotCalled()
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intentA";
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.emptyList());
        setupFakeChat(0.9d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any())).thenReturn(mi);
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
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
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
                false,
                "label1");
        MemoryVariable mv2 = new MemoryVariable(
                "var2",
                null,
                true,
                Collections.singletonList("trigger"),
                Collections.singletonList("prompt"),
                maxPrompts,
                0,
                false,
                false,
                "label2");
        MemoryIntent mi = new MemoryIntent("intent", AIID, UUID.randomUUID(), Arrays.asList(mv1, mv2), false);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any())).thenReturn(mi);
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
        verify(this.fakeIntentHandler).clearIntents(Collections.singletonList(mi));
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
                Collections.singletonList("prompt1"), 2, 0, false, false, "label1");
        MemoryVariable mv2 = new MemoryVariable(sameEntityName, null, true, Arrays.asList("a", "b"),
                Collections.singletonList("prompt2"), 2, 0, false, false, "label2");
        MemoryVariable mv3 = new MemoryVariable(sameEntityName, null, true, Collections.singletonList("c"),
                Collections.singletonList("prompt3"), 1, 0, false, false, "label3");
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Arrays.asList(mv1, mv2, mv3));


        setupFakeChat(0.7d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.0d, AIMLRESULT);
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), anyString())).thenReturn(mi);
        ApiIntent intent = new ApiIntent(intentName, "", "");
        when(this.fakeIntentHandler.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(Collections.singletonList(mi));

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
        when(this.fakeRecognizer.retrieveEntities(anyString(), any()))
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
        when(this.fakeRecognizer.retrieveEntities(anyString(), any()))
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

}
