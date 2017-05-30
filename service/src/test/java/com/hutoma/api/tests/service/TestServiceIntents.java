package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiIntent;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

public class TestServiceIntents extends ServiceTestBase {

    private static final String BASEPATH = "/intent/";
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private IMemoryIntentHandler fakeMemoryIntentHandler;

    @Test
    public void testSaveIntent() {
        ApiIntent intent = TestIntentLogic.getIntent();
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
