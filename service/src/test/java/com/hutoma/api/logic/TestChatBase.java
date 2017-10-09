package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiMinP;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class TestChatBase {

    static final UUID CHATID = UUID.fromString("89da2d5f-3ce5-4749-adc3-1f2ff6073fea");
    static final String SEMANTICRESULT = "semanticresult";
    static final String ASSISTANTRESULT = "Hello";
    static final String NEURALRESULT = "neuralresult";
    static final String AIMLRESULT = "aimlresult";
    static final String MEMORY_VARIABLE_PROMPT = "prompt1";
    static final WebHook VALID_WEBHOOK = new WebHook(AIID, "intent", "endpoint", true);
    private static final UUID AIML_BOT_AIID = UUID.fromString("bd2700ff-279b-4bac-ad2f-85a5275ac073");
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String QUESTION = "question";
    AIChatServices fakeChatServices;
    IEntityRecognizer fakeRecognizer;
    IMemoryIntentHandler fakeIntentHandler;
    WebHooks fakeWebHooks;
    Database fakeDatabase;
    ChatStateHandler fakeChatStateHandler;
    private ChatLogic chatLogic;
    private ChatLogger fakeChatTelemetryLogger;
    private Config fakeConfig;
    private AiStrings fakeAiStrings;

    @Before
    public void setup() {
        Config fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeChatServices = mock(AIChatServices.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeChatTelemetryLogger = mock(ChatLogger.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeChatStateHandler = mock(ChatStateHandler.class);
        this.fakeConfig = mock(Config.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.fakeAiStrings = mock(AiStrings.class);
        this.chatLogic = new ChatLogic(fakeConfig, mock(JsonSerializer.class), this.fakeChatServices, mock(Tools.class),
                mock(ILogger.class), this.fakeIntentHandler, this.fakeRecognizer, this.fakeChatTelemetryLogger, this.fakeWebHooks,
                this.fakeChatStateHandler, this.fakeAiStrings);

        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(ChatState.getEmpty());
        try {
            when(this.fakeAiStrings.getDefaultChatResponses(any(), any())).thenReturn(Collections.singletonList(ChatLogic.COMPLETELY_LOST_RESULT));
            when(this.fakeAiStrings.getRandomDefaultChatResponse(any(), any())).thenReturn(ChatLogic.COMPLETELY_LOST_RESULT);
        } catch (AiStrings.AiStringsException ex) {
            ex.printStackTrace();
        }
    }

    private ApiResult getAssistantChat(float min_p, String
            question) {
        return this.chatLogic.assistantChat(AIID, DEVID_UUID, question, CHATID.toString());
    }

    void validateStateSaved(final ChatResult returnedResult, final UUID usedAiid) {
        ApiChat result = (ApiChat) getChat(0.5f);
        Assert.assertEquals(returnedResult.getScore(), result.getResult().getScore(), 0.0001);
        ArgumentCaptor<ChatState> argumentCaptor = ArgumentCaptor.forClass(ChatState.class);
        verify(this.fakeChatStateHandler).saveState(any(), any(), argumentCaptor.capture());
        // And that the contains the lockedAiid value for the aiid with the highest score
        Assert.assertEquals(usedAiid, argumentCaptor.getValue().getLockedAiid());

    }

    ApiResult getChat(double minP) {
        return this.getChat(minP, QUESTION);
    }

    ApiResult getChat(double minP, String question) {
        // We need to check if the tests are already mocking the chat services' individual
        // confidence threshold (min_p)
        if (this.fakeChatServices.getMinPMap() != null) {
            Map<UUID, Double> map = new HashMap<>(this.fakeChatServices.getMinPMap());
            map.put(AIID, minP);
            when(this.fakeChatServices.getMinPMap()).thenReturn(map);
        } else {
            when(this.fakeChatServices.getMinPMap()).thenReturn(ImmutableMap.of(AIID, minP));
        }

        return this.chatLogic.chat(AIID, DEVID_UUID, question, CHATID.toString(), null);
    }

    ApiResult getAssistantChat(float min_p) {
        return this.getAssistantChat(min_p, QUESTION);
    }

    /***
     * Sets up fake responses from the AiChatServices layer
     * @param wnetConfidence
     * @param wnetResponse
     * @param aimlConfidence
     * @param aimlResponse
     * @param rnnConfidence
     * @param rnnResponse
     * @throws ServerConnector.AiServicesException
     */
    void setupFakeChat(double wnetConfidence, String wnetResponse,
                       double aimlConfidence, String aimlResponse,
                       double rnnConfidence, String rnnResponse) throws
            RequestBase.AiControllerException {
        setupFakeChatWithHistory(wnetConfidence, wnetResponse, "", aimlConfidence, aimlResponse,
                rnnConfidence, rnnResponse);
    }

    /***
     * Sets up fake responses from the AiChatServices layer
     * @param wnetConfidence
     * @param wnetResponse
     * @param wnetHistory
     * @param aimlConfidence
     * @param aimlResponse
     * @param rnnConfidence
     * @param rnnResponse
     * @throws ServerConnector.AiServicesException
     */
    void setupFakeChatWithHistory(double wnetConfidence, String wnetResponse, String wnetHistory,
                                  double aimlConfidence, String aimlResponse,
                                  double rnnConfidence, String rnnResponse) throws
            RequestBase.AiControllerException {

        ChatResult wnetResult = new ChatResult("Hi");
        wnetResult.setScore(wnetConfidence);
        wnetResult.setAnswer(wnetResponse);
        wnetResult.setHistory(wnetHistory);
        when(this.fakeChatServices.awaitWnet()).thenReturn(getChatResultMap(AIID, wnetResult));

        when(this.fakeConfig.getAimlBotAiids()).thenReturn(Collections.singletonList(AIML_BOT_AIID));
        when(this.fakeChatServices.getAIsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(
                new AiMinP(DEVID_UUID, AIML_BOT_AIID, 1.0)));
        ChatResult aimlResult = new ChatResult("Hi2");
        aimlResult.setScore(aimlConfidence);
        aimlResult.setAnswer(aimlResponse);
        when(this.fakeChatServices.awaitAiml()).thenReturn(getChatResultMap(AIML_BOT_AIID, aimlResult));

        ChatResult rnnResult = new ChatResult("Hi3");
        rnnResult.setScore(rnnConfidence);
        rnnResult.setAnswer(rnnResponse);
        when(this.fakeChatServices.awaitRnn()).thenReturn(getChatResultMap(AIID, rnnResult));
    }

    Map<UUID, ChatResult> getChatResultMap(
            final UUID aiid, final ChatResult chatResult) {
        return new HashMap<UUID, ChatResult>() {{
            this.put(aiid, chatResult);
        }};
    }

    MemoryIntent getMemoryIntentForPrompt(int maxPrompts, String currentValue)
            throws RequestBase.AiControllerException {
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
                false,
                "label");
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, Collections.singletonList(mv));

        setupFakeChat(0.9d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "", 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any())).thenReturn(mi);
        return mi;
    }

    MemoryIntent getMultiEntityMemoryIntentForPrompt(
            int maxPrompts, String currentValue) throws
            RequestBase.AiControllerException {
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
                false,
                "label1");
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
                true,
                "label2");
        List<MemoryVariable> variables = new ArrayList<>();
        variables.add(mv);
        variables.add(persistentVariable);
        MemoryIntent mi = new MemoryIntent(intentName, AIID, CHATID, variables);

        setupFakeChat(0.9d, MemoryIntentHandler.META_INTENT_TAG + intentName, 0.3d, "", 0.3d, "");
        when(this.fakeIntentHandler.parseAiResponseForIntent(any(), any(), any())).thenReturn(mi);
        return mi;
    }

}
