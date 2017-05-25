package com.hutoma.api.tests.service;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.endpoints.ChatEndpoint;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static junitparams.JUnitParamsRunner.$;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 02/11/16.
 */
@RunWith(JUnitParamsRunner.class)
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

    private static Object[] invalidMinPDataProvider() {
        return $(
                $("1.1"),
                $("-0.1"),
                $("abc"),
                $("-")
        );
    }

    @Before
    public void setup() {
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.randomUUID());
        when(this.fakeChatStateHandler.getState(any(), any())).thenReturn(ChatState.getEmpty());
    }

    @Test
    public void testChat() throws RequestBase.AiControllerException {
        final String answer = "the answer";
        ChatResult semanticAnalysisResult = new ChatResult("Hi");
        semanticAnalysisResult.setAnswer(answer);
        semanticAnalysisResult.setScore(0.9);
        when(this.fakeAiChatServices.awaitWnet()).thenReturn(new HashMap<UUID, ChatResult>() {{
            this.put(AIID, semanticAnalysisResult);
        }});

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
    public void testChat_invalidChatId() {
        final Response response = target(CHAT_PATH).queryParam("q", "").queryParam("chatId", "invalid")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    @Parameters(method = "invalidMinPDataProvider")
    public void testChat_invalidMinP(final String minP) {
        final Response response = buildChatDefaultParams(target(CHAT_PATH)).queryParam("confidence_threshold", minP)
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testChat_invalidChatTopic() {
        final Response response = buildChatDefaultParams(target(CHAT_PATH)).queryParam("current_topic", "@topic")
                .request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
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
