package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.endpoints.IntentEndpoint;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.logic.TestIntentLogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.validation.Validate;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestServiceIntent extends ServiceTestBase {

    private static final String BASEPATH = "/intent/";
    private IMemoryIntentHandler fakeMemoryIntentHandler;

    @Test
    public void testSaveIntent() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.createWebHook(any(), anyString(), anyString(), anyBoolean(), any())).thenReturn(true);
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setUserSays(Collections.singletonList(
                String.join("", Collections.nCopies(250, "A"))));
        intent.setResponses(Collections.singletonList(
                String.join("", Collections.nCopies(250, "A"))));
        intent.setWebHook(new WebHook(TestDataHelper.AIID, "name",
                String.join("", Collections.nCopies(2048, "A")), true));
        intent.addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                3, "somevalue", false, "label2", false)
                .addPrompt(String.join("", Collections.nCopies(250, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongResponse() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setResponses(Collections.singletonList(
                String.join("", Collections.nCopies(Validate.INTENT_RESPONSE_MAX_LENGTH + 1, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongUserExpression() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setUserSays(Collections.singletonList(
                String.join("", Collections.nCopies(Validate.INTENT_USERSAYS_MAX_LENGTH + 1, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongPrompt() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                3, "somevalue", false, "", false)
                .addPrompt(String.join("", Collections.nCopies(Validate.INTENT_PROMPT_MAX_LENGTH + 1, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongWebhookUrl() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setWebHook(new WebHook(TestDataHelper.AIID, "name",
                String.join("", Collections.nCopies(2048 + 1, "A")), true));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    private Response sendRequest(final String path, final String statusJson) {
        return target(path)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(statusJson));
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return IntentEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(IntentLogic.class).to(IntentLogic.class);
        binder.bind(TrainingLogic.class).to(TrainingLogic.class);
        binder.bind(AILogic.class).to(AILogic.class);

        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeAiServices = mock(AIServices.class);

        binder.bindFactory(new InstanceFactory<>(this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(this.fakeAiServices)).to(AIServices.class);
        binder.bindFactory(new InstanceFactory<>(mock(DatabaseUser.class))).to(DatabaseUser.class);
        return binder;
    }

}
