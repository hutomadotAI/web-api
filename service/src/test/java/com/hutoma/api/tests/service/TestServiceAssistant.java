package com.hutoma.api.tests.service;

import com.hutoma.api.common.ChatTelemetryLogger;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.endpoints.AssistantEndpoint;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import junitparams.JUnitParamsRunner;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.UUID;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Assistant service.
 */
@RunWith(JUnitParamsRunner.class)
public class TestServiceAssistant extends ServiceTestBase {
    private static final String CHAT_PATH = "/assistant/" + AIID + "/chat";

    @Mock
    protected IMemoryIntentHandler fakeMemoryIntentHandler;
    @Mock
    protected IEntityRecognizer fakeEntityRecognizer;
    @Mock
    protected ChatTelemetryLogger fakeChatTelemetryLogger;

    /***
     * Carry out pre-test set-up.
     */
    @Before
    public void setup() {
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.randomUUID());
    }

    /***
     * Test a valid chat response.
     * @throws ServerConnector.AiServicesException
     */
    @Test
    public void testStubbedChat() throws ServerConnector.AiServicesException {
        final String answer = "Hello";
        ChatResult semanticAnalysisResult = new ChatResult();
        semanticAnalysisResult.setAnswer(answer);
        semanticAnalysisResult.setScore(1.0);

        final Response response = buildChatDefaultParams(target(CHAT_PATH)).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiChat apiChat = deserializeResponse(response, ApiChat.class);
        Assert.assertEquals(answer, apiChat.getResult().getAnswer());
        Assert.assertNotNull(apiChat.getChatId());
        Assert.assertNotNull(apiChat.getResult().getElapsedTime());
        Assert.assertNotNull(apiChat.getResult().getScore());
    }

    /***
     * Test for an unauthorised response to an invalid devId.
     */
    @Test
    public void testChat_devId_invalid() {
        final Response response = buildChatDefaultParams(target(CHAT_PATH)).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    /***
     * Get the class under test.
     * @return
     */
    protected Class<?> getClassUnderTest() {
        return AssistantEndpoint.class;
    }

    /***
     * Apply default query parameters.
     * @param webTarget
     * @return
     */
    private WebTarget buildChatDefaultParams(WebTarget webTarget) {
        return webTarget
                .queryParam("q", "question")
                .queryParam("chatId", "");
    }

    /***
     * Set up additional bindings required for the tests.
     * @param binder
     * @return
     */
    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeEntityRecognizer = mock(IEntityRecognizer.class);
        this.fakeChatTelemetryLogger = mock(ChatTelemetryLogger.class);

        binder.bind(ChatLogic.class).to(ChatLogic.class);

        binder.bindFactory(new InstanceFactory<>(TestServiceAssistant.this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceAssistant.this.fakeEntityRecognizer)).to(IEntityRecognizer.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceAssistant.this.fakeChatTelemetryLogger)).to(ChatTelemetryLogger.class);

        return binder;
    }
}
