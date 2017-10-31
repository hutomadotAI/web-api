package com.hutoma.api.tests.service;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.endpoints.ChatEndpoint;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 02/11/16.
 */
public class TestServiceChat extends ServiceTestBase {
    private static final String CHAT_PATH = "/ai/" + AIID + "/chat";

    @Mock
    protected IMemoryIntentHandler fakeMemoryIntentHandler;
    @Mock
    protected IEntityRecognizer fakeEntityRecognizer;
    @Mock
    protected ChatLogger fakeChatTelemetryLogger;
    @Mock
    protected ChatStateHandler fakeChatStateHandler;

    @Before
    public void setup() {
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.randomUUID());
        when(this.fakeChatStateHandler.getState(any(), any(), any())).thenReturn(ChatState.getEmpty());
    }

    @Test
    public void testChat() throws RequestBase.AiControllerException {
        final String answer = "the answer";
        final double score = 0.9;
        ChatResult semanticAnalysisResult = new ChatResult("Hi");
        semanticAnalysisResult.setAnswer(answer);
        semanticAnalysisResult.setScore(score);
        when(this.fakeAiChatServices.getMinPMap()).thenReturn(ImmutableMap.of(AIID, score - 0.1));
        when(this.fakeAiChatServices.awaitWnet()).thenReturn(ImmutableMap.of(AIID, semanticAnalysisResult));

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
    public void testChat_invalidChatId() {
        final Response response = target(CHAT_PATH).queryParam("q", "").queryParam("chatId", "invalid")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
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
    public void testChat_response_includesVariablesMap() {
        UUID chatId = UUID.randomUUID();
        String label1 = "label1";
        String label2 = "label2";
        MemoryVariable mv1 = new MemoryVariable("entity1", null, true, Collections.singletonList("1"),
                Collections.singletonList("prompt1"), 2, 0, false, false, label1);
        MemoryVariable mv2 = new MemoryVariable("entity2", null, true, Collections.singletonList("2"),
                Collections.singletonList("prompt2"), 2, 0, false, false, label2);
        List<MemoryVariable> vars = Arrays.asList(mv1, mv2);
        MemoryIntent mi = new MemoryIntent("intent1", AIID, chatId, vars, false);
        List<MemoryIntent> intents = Collections.singletonList(mi);
        when(this.fakeMemoryIntentHandler.getCurrentIntentsStateForChat(any(), any())).thenReturn(intents);

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
    }

    private WebTarget buildChatDefaultParams(WebTarget webTarget) {
        return webTarget
                .queryParam("q", "question")
                .queryParam("chatId", "");
    }

    protected Class<?> getClassUnderTest() {
        return ChatEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeEntityRecognizer = mock(IEntityRecognizer.class);
        this.fakeChatTelemetryLogger = mock(ChatLogger.class);
        this.fakeChatStateHandler = mock(ChatStateHandler.class);

        binder.bind(ChatLogic.class).to(ChatLogic.class);

        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeEntityRecognizer)).to(IEntityRecognizer.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeChatTelemetryLogger)).to(ChatLogger.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceChat.this.fakeChatStateHandler)).to(ChatStateHandler.class);

        return binder;
    }
}
