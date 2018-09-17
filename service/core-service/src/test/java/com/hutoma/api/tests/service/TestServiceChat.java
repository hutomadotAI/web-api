package com.hutoma.api.tests.service;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.endpoints.ChatEndpoint;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.chat.*;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static com.hutoma.api.common.TestDataHelper.getSampleAI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 02/11/16.
 */
public class TestServiceChat extends ServiceTestBase {
    private static final String CHAT_PATH = "/ai/" + AIID + "/chat";
    private static final String CHAT_HANDOVER = CHAT_PATH + "/target";
    private static final String CHAT_RESET = CHAT_PATH + "/reset";
    private static final String TRIGGER_INTENT = CHAT_PATH + "/triggerIntent";

    @Mock
    private IMemoryIntentHandler fakeMemoryIntentHandler;
    @Mock
    private IEntityRecognizer fakeEntityRecognizer;
    @Mock
    private ChatLogger fakeChatTelemetryLogger;
    @Mock
    private ChatStateHandler fakeChatStateHandler;

    IEntityRecognizer fakeRecognizer;
    private ChatPassthroughHandler fakePassthroughHandler;
    private ChatIntentHandler fakeChatIntenthHandler;
    private ChatRequestTrigger fakeRequestBETrigger;
    private ChatEmbHandler fakeEmbHandler;
    private ChatAimlHandler fakeAimlHandler;
    private ChatDefaultHandler fakeDefaultHandler;
    private IntentProcessor fakeIntentProcessorLogic;
    private ChatWorkflow fakeChatWorkflow;

    @Before
    public void setup() throws ChatStateHandler.ChatStateException {

        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.randomUUID());
        ChatState emptyState = ChatState.getEmpty();
        emptyState.setAi(TestDataHelper.getSampleAI());
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(emptyState);

        when(this.fakeChatWorkflow.getHandlers()).thenReturn(
                Arrays.asList(this.fakePassthroughHandler, this.fakeChatIntenthHandler,
                        this.fakeRequestBETrigger, this.fakeEmbHandler,
                        this.fakeAimlHandler, this.fakeDefaultHandler));
    }

    @Test
    public void testChat() throws ChatBackendConnector.AiControllerException {
        final String answer = "the answer";
        final double score = 0.9;
        ChatResult semanticAnalysisResult = new ChatResult("Hi");
        semanticAnalysisResult.setAnswer(answer);
        semanticAnalysisResult.setScore(score);
        when(this.fakeAiChatServices.getMinPMap()).thenReturn(ImmutableMap.of(AIID, score - 0.1));
        when(this.fakeAiChatServices.awaitBackend(BackendServerType.EMB)).thenReturn(ImmutableMap.of(AIID, semanticAnalysisResult));

        final Response response = buildChatDefaultParams(target(CHAT_PATH)).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertEquals(answer, apiChat.getResult().getAnswer());
        Assert.assertNotNull(apiChat.getChatId());
        Assert.assertNotNull(apiChat.getResult().getElapsedTime());
        Assert.assertNotNull(apiChat.getResult().getScore());
    }

    @Test
    public void testChat_devId_invalid() {
        final Response response = buildChatDefaultParams(target(CHAT_PATH)).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testChat_invalidAiId() {
        final Response response = buildChatDefaultParams(target("/ai/myaiid/chat")).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_clientToken_canChat() {
        UUID aiid = UUID.randomUUID();
        UUID devId = UUID.randomUUID();
        MultivaluedHashMap<String, Object> authHeader = getClientAuthHeaders(devId, aiid);
        // Chat request needs to have client id containing the AIID for the Ai we want to chat with
        final Response response = buildChatDefaultParams(target(String.format("/ai/%s/chat", aiid)))
                .request()
                .headers(authHeader)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testChat_chatIdGenerated() {
        final Response response = target(CHAT_PATH).queryParam("q", "question")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertNotNull(apiChat.getChatId());
    }

    @Test
    public void testChat_chatIdRemembered() {
        UUID chatId = UUID.randomUUID();
        final Response response = target(CHAT_PATH).queryParam("q", "question")
                .queryParam("chatId", chatId.toString())
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertEquals(chatId, apiChat.getChatId());
    }

    @Test
    public void testChat_invalidChatId() {
        final Response response = target(CHAT_PATH).queryParam("q", "question")
                .queryParam("chatId", "invalid")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_chatStateUserException() throws ChatStateHandler.ChatStateException {
        UUID chatId = UUID.randomUUID();
        when(this.fakeChatStateHandler.getState(any(), any(), any()))
                .thenThrow(ChatStateHandler.ChatStateUserException.class);
        final Response response = target(CHAT_PATH).queryParam("q", "question")
                .queryParam("chatId", chatId.toString())
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_chatStateException() throws ChatStateHandler.ChatStateException {
        UUID chatId = UUID.randomUUID();
        when(this.fakeChatStateHandler.getState(any(), any(), any()))
                .thenThrow(ChatStateHandler.ChatStateException.class);
        final Response response = target(CHAT_PATH).queryParam("q", "question")
                .queryParam("chatId", chatId.toString())
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatus());
    }

    @Test
    public void testChat_chatQuestionTooLong() {
        String question = String.join("", Collections.nCopies(1024 + 1, "A"));
        final Response response = target(CHAT_PATH)
                .queryParam("q", question)
                .queryParam("chatId", "")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_response_includesVariablesMap() throws ChatLogic.IntentException, WebHooks.WebHookException,
            ChatStateHandler.ChatStateException {
        UUID chatId = UUID.randomUUID();
        String label1 = "label1";
        String label2 = "label2";
        MemoryVariable mv1 = new MemoryVariable("entity1", null, true, Collections.singletonList("1"),
                Collections.singletonList("prompt1"), 2, 0, false, false, label1, false);
        MemoryVariable mv2 = new MemoryVariable("entity2", null, true, Collections.singletonList("2"),
                Collections.singletonList("prompt2"), 2, 0, false, false, label2, false);
        List<MemoryVariable> vars = Arrays.asList(mv1, mv2);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, chatId, vars, false);
        List<MemoryIntent> intents = Collections.singletonList(mi);
        final ChatResult chatResult = new ChatResult("response");
        chatResult.setIntents(intents);
        final long timestamp = System.currentTimeMillis();
        chatResult.setScore(0.8);

        // Need to set the state to the intents since we're mocking the intent processor's ::processIntent which is
        // responsible for adding the intent to the state
        ChatState state = new ChatState(DateTime.now(), null, null, null, null, 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
        state.setCurrentIntents(intents);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);

        when(this.fakeMemoryIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(intents);
        when(this.fakeIntentProcessorLogic.processIntent(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeTools.getTimestamp()).thenReturn(timestamp);

        final Response response = target(CHAT_PATH)
                .queryParam("q", "blablabla")
                .queryParam("chatId", "")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertEquals(intents.size(), apiChat.getResult().getIntents().size());
        MemoryIntent resultIntent = apiChat.getResult().getIntents().get(0);
        Assert.assertEquals(vars.size(), resultIntent.getVariables().size());
        Assert.assertNotNull(resultIntent.getVariablesMap());
        Assert.assertEquals(vars.size(), resultIntent.getVariablesMap().entrySet().size());
        Assert.assertTrue(resultIntent.getVariablesMap().containsKey(label1));
        Assert.assertTrue(resultIntent.getVariablesMap().containsKey(label2));
        Assert.assertEquals(mv1.getName(), resultIntent.getVariablesMap().get(label1).getName());
        Assert.assertEquals(mv2.getName(), resultIntent.getVariablesMap().get(label2).getName());
        Assert.assertEquals(timestamp, apiChat.getTimestamp());
        Assert.assertEquals(apiChat.getTimestamp(), apiChat.getResult().getTimestamp());
    }

    @Test
    public void testChat_handover() throws DatabaseException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final Response response = target(CHAT_HANDOVER)
                .queryParam("target", "hUmAn")
                .queryParam("chatId", "")
                .request()
                .headers(defaultHeaders)
                .post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testChat_handover_invalidTarget() {
        final Response response = target(CHAT_HANDOVER)
                .queryParam("target", "thisIsNotAValidTarget")
                .queryParam("chatId", "")
                .request()
                .headers(defaultHeaders)
                .post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_response_includesContext() throws ChatLogic.IntentException, WebHooks.WebHookException, ChatStateHandler.ChatStateException {
        final String var1Name = "var1";
        final String var1Value = "value1";
        final String var2Name = "var2";
        final String var2Value = "value2";
        UUID chatId = UUID.randomUUID();
        ChatContext context = new ChatContext();
        context.setValue(var1Name, var1Value);
        context.setValue(var2Name, var2Value);

        MemoryIntent mi = new MemoryIntent("intent1", AIID, chatId, Collections.emptyList(), false);
        List<MemoryIntent> intents = Collections.singletonList(mi);

        ChatState state = new ChatState(DateTime.now(), null, null, null, null, 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), context);
        state.setCurrentIntents(intents);

        when(this.fakeMemoryIntentHandler.getCurrentIntentsStateForChat(any())).thenReturn(intents);
        when(this.fakeIntentProcessorLogic.processIntent(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(state);
        when(this.fakeTools.getTimestamp()).thenReturn(System.currentTimeMillis());

        final Response response = target(CHAT_PATH)
                .queryParam("q", "blablabla")
                .queryParam("chatId", "")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertNotNull(apiChat.getResult().getContext());
        Map<String, String> ctx = apiChat.getResult().getContext();
        Assert.assertEquals(2, ctx.size());
        Assert.assertEquals(var1Value, ctx.get(var1Name));
        Assert.assertEquals(var2Value, ctx.get(var2Name));
    }

    @Test
    public void testChat_resetChat() {
        final Response response = target(CHAT_RESET)
                .queryParam("chatId", "")
                .request().headers(defaultHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiResult result = deserializeResponse(response, ApiResult.class);
        Assert.assertEquals("Chat state cleared", result.getStatus().getInfo());
    }

    @Test
    public void testChat_resetChat_devId_invalid() {
        final Response response = target(CHAT_RESET)
                .queryParam("chatId", "")
                .request().headers(noDevIdHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testChat_triggerIntent() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.checkAIBelongsToDevId(any(), any())).thenReturn(true);

        ApiIntent intent = new ApiIntent("intentName", null, null);
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(intent);

        final Response response = target(TRIGGER_INTENT)
                .queryParam("chatId", "")
                .queryParam("intent_name", "intent")
                .request().headers(defaultHeaders).put(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testChat_triggerIntent_devId_invalid() {
        final Response response = target(TRIGGER_INTENT)
                .queryParam("chatId", "")
                .queryParam("intent_name", "intent")
                .request().headers(noDevIdHeaders).put(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    private WebTarget buildChatDefaultParams(WebTarget webTarget) {
        return webTarget
                .queryParam("q", "question")
                .queryParam("chatId", "");
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return ChatEndpoint.class;
    }

    @Override
    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeEntityRecognizer = mock(IEntityRecognizer.class);
        this.fakeChatTelemetryLogger = mock(ChatLogger.class);
        this.fakeChatStateHandler = mock(ChatStateHandler.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeChatWorkflow = mock(ChatWorkflow.class);
        this.fakeIntentProcessorLogic = mock(IntentProcessor.class);

        this.fakePassthroughHandler = new ChatPassthroughHandler(this.fakeAiChatServices, this.fakeWebHooks, mock(Tools.class),
                mock(ChatLogger.class), mock(ILogger.class));
        this.fakeChatIntenthHandler = new ChatIntentHandler(this.fakeMemoryIntentHandler, this.fakeIntentProcessorLogic);
        this.fakeRequestBETrigger = new ChatRequestTrigger(this.fakeAiChatServices);
        this.fakeEmbHandler = new ChatEmbHandler(this.fakeMemoryIntentHandler, this.fakeIntentProcessorLogic,
                mock(ContextVariableExtractor.class), mock(ILogger.class));
        this.fakeAimlHandler = new ChatAimlHandler(mock(ILogger.class));
        this.fakeDefaultHandler = new ChatDefaultHandler(this.fakeAiStrings, mock(ILogger.class));

        binder.bind(ChatLogic.class).to(ChatLogic.class);

        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeEntityRecognizer)).to(IEntityRecognizer.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeChatTelemetryLogger)).to(ChatLogger.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeChatStateHandler)).to(ChatStateHandler.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeChatWorkflow)).to(ChatWorkflow.class);

        return binder;
    }
}
