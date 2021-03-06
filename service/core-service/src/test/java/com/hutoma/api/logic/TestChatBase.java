package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.chat.*;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Mockito.*;


public class TestChatBase {

    static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    static final String SEMANTICRESULT = "semanticresult";
    static final String AIMLRESULT = "aimlresult";
    static final String MEMORY_VARIABLE_PROMPT = "prompt1";
    static final WebHook VALID_WEBHOOK = new WebHook(AIID, "intent", "endpoint", true);
    static final double EMB_CHAT_SCORE = 0.9d;
    private static final UUID AIML_BOT_AIID = UUID.fromString("bd2700ff-279b-4bac-ad2f-85a5275ac073");
    private static final String QUESTION = "question";

    AIChatServices fakeChatServices;
    IEntityRecognizer fakeRecognizer;
    IMemoryIntentHandler fakeIntentHandler;
    WebHooks fakeWebHooks;
    DatabaseAI fakeDatabaseAi;
    DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    ChatStateHandler fakeChatStateHandler;
    ChatLogic chatLogic;
    protected Config fakeConfig;
    private AiStrings fakeAiStrings;
    ChatWorkflow fakeChatWorkflow;
    ChatHandoverHandler fakeHandoverHandler;
    ChatPassthroughHandler fakePassthroughHandler;
    ChatIntentHandler fakeChatIntenthHandler;
    ChatRequestTrigger fakeRequestBETrigger;
    ChatAimlHandler fakeAimlHandler;
    ChatEmbHandler fakeEmbHandler;
    ChatDefaultHandler fakeDefaultHandler;
    IntentProcessor intentProcessor;
    ConditionEvaluator fakeConditionEvaluator;
    ContextVariableExtractor fakeContextVariableExtractor;
    FeatureToggler fakeFeatureToggler;
    FacebookChatHandler fakeFacebookChathandler;
    WebhookHandler fakeWebhookHandler;
    LanguageLogic fakeLanguageLogic;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeChatServices = mock(AIChatServices.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeChatStateHandler = mock(ChatStateHandler.class);
        this.fakeConfig = mock(Config.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.fakeAiStrings = mock(AiStrings.class);
        this.fakeChatWorkflow = mock(ChatWorkflow.class);
        this.fakeConditionEvaluator = mock(ConditionEvaluator.class);
        this.fakeContextVariableExtractor = mock(ContextVariableExtractor.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);
        this.fakeWebhookHandler = mock(WebhookHandler.class);
        this.intentProcessor = new IntentProcessor(this.fakeRecognizer, this.fakeIntentHandler, this.fakeWebHooks,
                this.fakeConditionEvaluator, this.fakeContextVariableExtractor, this.fakeWebhookHandler,
                mock(ILogger.class), this.fakeConfig, this.fakeFeatureToggler);

        this.fakePassthroughHandler = new ChatPassthroughHandler(this.fakeChatServices, this.fakeWebHooks, mock(Tools.class),
                mock(ChatLogger.class), mock(ILogger.class), this.fakeFeatureToggler);
        this.fakeHandoverHandler = new ChatHandoverHandler(mock(Tools.class));
        this.fakeChatIntenthHandler = new ChatIntentHandler(this.fakeIntentHandler, this.intentProcessor);
        this.fakeRequestBETrigger = new ChatRequestTrigger(this.fakeChatServices);
        this.fakeEmbHandler = new ChatEmbHandler(this.fakeIntentHandler, this.intentProcessor, this.fakeContextVariableExtractor, mock(ILogger.class));
        this.fakeAimlHandler = new ChatAimlHandler(mock(ILogger.class));
        this.fakeDefaultHandler = new ChatDefaultHandler(this.fakeAiStrings, mock(ILogger.class));
        this.fakeFacebookChathandler = mock(FacebookChatHandler.class);
        this.fakeLanguageLogic = mock(LanguageLogic.class);

        when(fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
        when(this.fakeLanguageLogic.getAvailableLanguage(any(String.class), any(), any())).thenReturn(Optional.of(SupportedLanguage.EN));
        when(this.fakeLanguageLogic.getAvailableLanguage(any(Locale.class), any(), any())).thenReturn(Optional.of(SupportedLanguage.EN));

        // Set feature toggles to Control
        TestDataHelper.setFeatureToggleToControl(this.fakeFeatureToggler);

        when(this.fakeChatWorkflow.getHandlers()).thenReturn(
                Arrays.asList(this.fakeHandoverHandler, this.fakePassthroughHandler, this.fakeChatIntenthHandler,
                        this.fakeRequestBETrigger, this.fakeEmbHandler,
                        this.fakeAimlHandler, this.fakeDefaultHandler));

        this.chatLogic = new ChatLogic(this.fakeChatServices, this.fakeChatStateHandler, this.fakeDatabaseEntitiesIntents, mock(Tools.class),
                mock(ILogger.class), mock(ChatLogger.class), this.fakeChatWorkflow, this.fakeFeatureToggler, this.fakeLanguageLogic);

        ChatState emptyState = ChatState.getEmpty();
        emptyState.setAi(getSampleAI());
        try {
            when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(emptyState);
            when(this.fakeAiStrings.getDefaultChatResponses(any(), any())).thenReturn(Collections.singletonList(TestDataHelper.DEFAULT_CHAT_RESPONSE));
            when(this.fakeAiStrings.getRandomDefaultChatResponse(any(), any())).thenReturn(TestDataHelper.DEFAULT_CHAT_RESPONSE);
        } catch (AiStrings.AiStringsException | ChatStateHandler.ChatStateException ex) {
            ex.printStackTrace();
        }
    }

    void validateStateSaved(final ChatResult returnedResult, final UUID usedAiid) {
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(returnedResult.getScore(), result.getResult().getScore(), 0.0001);
        ArgumentCaptor<ChatState> argumentCaptor = ArgumentCaptor.forClass(ChatState.class);
        try {
            verify(this.fakeChatStateHandler).saveState(any(), any(), any(), argumentCaptor.capture());
        } catch (ChatStateHandler.ChatStateException ex) {
            ex.printStackTrace();
        }
        // And that the contains the lockedAiid value for the aiid with the highest score
        Assert.assertEquals(usedAiid, argumentCaptor.getValue().getLockedAiid());

    }

    ChatResult getFacebookChat(double minP)
            throws NoServerAvailableException, ChatBaseException,
            ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException {
        return this.getFacebookChat(minP, QUESTION);
    }

    ChatResult getFacebookChat(double minP, String question)
            throws NoServerAvailableException, ChatBaseException,
            ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException {
        mapMinP(minP);
        return this.chatLogic.chatFacebook(AIID, DEVID_UUID, question, CHATID.toString(), "facebookuser", "pageToken");
    }

    private void mapMinP(final double minP) {
        // We need to check if the tests are already mocking the chat services' individual
        // confidence threshold (min_p)
        if (this.fakeChatServices.getMinPMap() != null) {
            Map<UUID, Double> map = new HashMap<>(this.fakeChatServices.getMinPMap());
            map.put(AIID, minP);
            when(this.fakeChatServices.getMinPMap()).thenReturn(map);
        } else {
            when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(AIID, minP));
        }
    }

    ApiResult getChat(double minP) {
        return this.getChat(minP, QUESTION);
    }

    ApiResult getChat(double minP, String question) {
        // We need to check if the tests are already mocking the chat services' individual
        // confidence threshold (min_p)
        mapMinP(minP);
        return this.chatLogic.chat(AIID, DEVID_UUID, question, CHATID.toString(), null);
    }

    /***
     * Sets up fake responses from the AiChatServices layer
     * @param embConfidence
     * @param embResponse
     * @param aimlConfidence
     * @param aimlResponse
     * @throws ServerConnector.AiServicesException
     */
    void setupFakeChat(double embConfidence, String embResponse,
                       double aimlConfidence, String aimlResponse) throws
            ChatBackendConnector.AiControllerException {
        setupFakeChatWithHistory(embConfidence, embResponse, "", aimlConfidence, aimlResponse);
    }

    /***
     * Sets up fake responses from the AiChatServices layer
     * @param embConfidence
     * @param embResponse
     * @param embHistory
     * @param aimlConfidence
     * @param aimlResponse
     * @throws ServerConnector.AiServicesException
     */
    void setupFakeChatWithHistory(double embConfidence, String embResponse, String embHistory,
                                  double aimlConfidence, String aimlResponse) throws
            ChatBackendConnector.AiControllerException {

        ChatResult chatResult = new ChatResult("Hi");
        chatResult.setScore(embConfidence);
        chatResult.setAnswer(embResponse);
        chatResult.setHistory(embHistory);
        when(this.fakeChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(getChatResultMap(AIID, chatResult));

        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID));
        when(this.fakeChatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(
                new AiMinP(DEVID_UUID, AIML_BOT_AIID, 1.0)));
        ChatResult aimlResult = new ChatResult("Hi2");
        aimlResult.setScore(aimlConfidence);
        aimlResult.setAnswer(aimlResponse);
        when(this.fakeChatServices.awaitBackend(BackendServerType.AIML)).thenReturn(getChatResultMap(AIML_BOT_AIID, aimlResult));
    }

    Map<UUID, ChatResult> getChatResultMap(
            final UUID aiid, final ChatResult chatResult) {
        return new HashMap<UUID, ChatResult>() {{
            this.put(aiid, chatResult);
        }};
    }

    MemoryIntent getMemoryIntentForPrompt(int maxPrompts, String currentValue)
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String promptTrigger = "variableValue";
        final String prompt = "prompt1";
        MemoryVariable mv = new MemoryVariable(
                "var",
                currentValue,
                true,
                Arrays.asList(promptTrigger, "b"),
                Collections.singletonList(prompt),
                maxPrompts,
                0,
                false,
                EntityValueType.LIST,
                false,
                "label",
                false,
                0);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        when(this.fakeIntentHandler.getIntent(any(), anyString())).thenReturn(TestIntentLogic.getIntent());
        setupFakeChat(EMB_CHAT_SCORE, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

    MemoryIntent getMultiEntityMemoryIntentForPrompt(
            int maxPrompts, String currentValue) throws
            ChatBackendConnector.AiControllerException, ChatLogic.IntentException {
        final String intentName = "intent1";
        final String promptTrigger = "variableValue";
        final String prompt = "prompt1";
        MemoryVariable mv = new MemoryVariable(
                "var",
                currentValue,
                true,
                Arrays.asList(promptTrigger, "b"),
                Collections.singletonList(prompt),
                maxPrompts,
                0,
                false,
                EntityValueType.LIST,
                false,
                "label1",
                false,
                0);
        final String persistentTrigger = "persistentValue";
        final String persistentPrompt = "persistentPrompt";
        MemoryVariable persistentVariable = new MemoryVariable(
                "persistent_var",
                currentValue,
                true,
                Arrays.asList(persistentTrigger),
                Collections.singletonList(persistentPrompt),
                maxPrompts,
                0,
                false,
                EntityValueType.LIST,
                true,
                "label2",
                false,
                1);
        List<MemoryVariable> variables = new ArrayList<>();
        variables.add(mv);
        variables.add(persistentVariable);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, variables);

        setupFakeChat(EMB_CHAT_SCORE, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any(), any(), any())).thenReturn(mi);
        return mi;
    }

}

