package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.endpoints.IntentEndpoint;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.logic.TestIntentLogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestServiceIntents extends ServiceTestBase {

    private static final String BASEPATH = "/intent/";
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private IMemoryIntentHandler fakeMemoryIntentHandler;

    @Test
    public void testSaveIntent() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setUserSays(Collections.singletonList(
                String.join("", Collections.nCopies(250, "A"))));
        intent.setResponses(Collections.singletonList(
                String.join("", Collections.nCopies(250, "A"))));
        intent.setWebHook(new WebHook(TestDataHelper.AIID, "name",
                String.join("", Collections.nCopies(2048, "A")), true));
        intent.addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                3, "somevalue", false, "label2")
                .addPrompt(String.join("", Collections.nCopies(250, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongResponse() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setResponses(Collections.singletonList(
                String.join("", Collections.nCopies(250 + 1, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongUserExpression() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.setUserSays(Collections.singletonList(
                String.join("", Collections.nCopies(250 + 1, "A"))));
        final Response response = sendRequest(BASEPATH + TestDataHelper.AIID.toString(),
                this.serializeObject(intent));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testSaveIntent_LongPrompt() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                3, "somevalue", false, "")
                .addPrompt(String.join("", Collections.nCopies(250 + 1, "A"))));
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

        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeAiServices = mock(AIServices.class);

        binder.bindFactory(new InstanceFactory<>(this.fakeDatabaseEntitiesIntents)).to(DatabaseEntitiesIntents.class);
        binder.bindFactory(new InstanceFactory<>(this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(this.fakeAiServices)).to(AIServices.class);
        return binder;
    }

}
