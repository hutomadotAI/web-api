package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.endpoints.TrainingEndpoint;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 13/01/17.
 */
public class TestServiceTraining extends ServiceTestBase {

    private static final String TRAINING_BASEPATH = "/ai/" + TestDataHelper.AIID + "/training";
    @Mock
    private IMemoryIntentHandler fakeMemoryIntentHandler;
    @Mock
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    @Mock
    private AIServices fakeAiServices;

    @Before
    public void setup() {
    }

    @Test
    public void testTrainingUpload() throws Database.DatabaseException, IOException {
        doReturn(1000).when(this.fakeConfig).getMaxUploadSizeKb();
        when(this.fakeDatabaseEntitiesIntents.updateAiTrainingFile(any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(TestDataHelper.getSampleAI());
        Response response = upload(String.valueOf(TrainingLogic.TrainingType.TEXT.type()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testTrainingUpload_invalidSourceType() throws Database.DatabaseException, IOException {
        Response response = upload("-999");
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testTrainingUpload_invalid_devId() throws Database.DatabaseException, IOException {
        FormDataMultiPart multipart = generateTrainingUpload();
        final Response response = target(TRAINING_BASEPATH)
                .register(MultiPartFeature.class)
                .queryParam("source_type", String.valueOf(TrainingLogic.TrainingType.TEXT.type()))
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        multipart.close();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testTrainingStart() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_STOPPED, false));
        final Response response = testTraining("start", defaultHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testTrainingStart_invalid_devId() throws Database.DatabaseException, IOException {
        final Response response = testTraining("start", noDevIdHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testTrainingStop() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING, false));
        final Response response = testTraining("stop", defaultHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testTrainingStop_invalid_devId() throws Database.DatabaseException, IOException {
        final Response response = testTraining("stop", noDevIdHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testTrainingUpdate() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING, false));
        when(this.fakeDatabaseEntitiesIntents.getAiTrainingFile(any())).thenReturn("Q1\nA1");
        final Response response = testTraining("update", defaultHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testTrainingUpdate_invalid_devId() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING, false));
        final Response response = testTraining("update", noDevIdHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetTrainingMaterials() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        final Response response = target(TRAINING_BASEPATH)
                .path("materials")
                .request()
                .headers(defaultHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetTrainingMaterials_invalid_devId() throws Database.DatabaseException, IOException {
        final Response response = target(TRAINING_BASEPATH)
                .path("materials")
                .request()
                .headers(noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testTrainingUpdate_intentOnly() throws Database.DatabaseException, IOException {
        when(this.fakeDatabaseEntitiesIntents.getAI(any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING, false));
        when(this.fakeDatabaseEntitiesIntents.getAiTrainingFile(any())).thenReturn(null);
        ApiIntent intent = new ApiIntent("intent1", "", "");
        intent.setUserSays(Collections.singletonList("userSays"));
        intent.setResponses(Collections.singletonList("response"));
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList(intent.getIntentName()));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(intent);
        final Response response = testTraining("update", defaultHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    private Response testTraining(final String path, final MultivaluedHashMap<String, Object> headers) {
        return target(TRAINING_BASEPATH)
                .path(path)
                .request()
                .headers(headers)
                .put(Entity.text(""));
    }

    private FormDataMultiPart generateTrainingUpload() {
        final FileDataBodyPart filePart = new FileDataBodyPart("file",
                new File(getTestsBaseLocation(), "test-textfiles/training1.txt"));
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        return (FormDataMultiPart) formDataMultiPart.bodyPart(filePart);
    }

    private Response upload(final String sourceType) throws IOException {
        FormDataMultiPart multipart = generateTrainingUpload();
        final Response response = target(TRAINING_BASEPATH)
                .register(MultiPartFeature.class)
                .queryParam("source_type", sourceType)
                .request()
                .headers(defaultHeaders)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        multipart.close();
        return response;
    }

    protected Class<?> getClassUnderTest() {
        return TrainingEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(TrainingLogic.class).to(TrainingLogic.class);
        binder.bind(AILogic.class).to(AILogic.class);

        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeAiServices = mock(AIServices.class);

        binder.bindFactory(new InstanceFactory<>(TestServiceTraining.this.fakeDatabaseEntitiesIntents)).to(DatabaseEntitiesIntents.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceTraining.this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceTraining.this.fakeAiServices)).to(AIServices.class);
        return binder;
    }
}
