package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiCsvImportResult;
import com.hutoma.api.endpoints.IntentsEndpoint;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TestServiceIntents extends ServiceTestBase {

    private static final String PATH_INTENTS_CSV_UPLOAD = "/intents/" + TestDataHelper.AIID.toString() + "/csv";

    @Test
    public void testImportIntentCsv() {
        doReturn(1000).when(this.fakeConfig).getMaxUploadSizeKb();
        final Response response = sendCsvUpload();
        ApiCsvImportResult result = deserializeResponse(response, ApiCsvImportResult.class);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        Assert.assertEquals(59, result.getImported().size());
    }

    @Test
    public void testImportIntentCsv_invalid_devId() {
        doReturn(1000).when(this.fakeConfig).getMaxUploadSizeKb();
        final Response response = sendCsvUpload(noDevIdHeaders);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testImportIntentCsv_preventsUploadLargeFiles() {
        doReturn(1).when(this.fakeConfig).getMaxUploadSizeKb();
        final Response response = sendCsvUpload();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testImportIntentCsv_duplicateIntents() {
        doReturn(1).when(this.fakeConfig).getMaxUploadSizeKb();
        FormDataMultiPart multipart = generateCsvFileWithDuplicatesForUpload();
        final Response response = sendUpload(defaultHeaders, multipart);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    private Response sendCsvUpload() {
        return sendCsvUpload(defaultHeaders);
    }

    private Response sendCsvUpload(final MultivaluedHashMap<String, Object> headers) {
        return sendUpload(headers, generateCsvFileForUpload());
    }

    private Response sendUpload(final MultivaluedHashMap<String, Object> headers, final FormDataMultiPart multipart) {
        final Response response = target(PATH_INTENTS_CSV_UPLOAD)
                .register(MultiPartFeature.class)
                .request()
                .headers(headers)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        try {
            multipart.close();
        } catch (IOException ex) {
            Assert.fail("Exception: " + ex);
        }
        return response;
    }

    private FormDataMultiPart generateCsvFileForUpload() {
        return generateFileForUpload("intents1.csv");
    }

    private FormDataMultiPart generateCsvFileWithDuplicatesForUpload() {
        return generateFileForUpload("intents2.csv");
    }

    private FormDataMultiPart generateFileForUpload(final String filename) {
        final FileDataBodyPart filePart = new FileDataBodyPart("file",
                new File(getTestsBaseLocation(), String.format("test-textfiles/%s", filename)));
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        return (FormDataMultiPart) formDataMultiPart.bodyPart(filePart);
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return IntentsEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(IntentLogic.class).to(IntentLogic.class);
        binder.bind(TrainingLogic.class).to(TrainingLogic.class);
        binder.bind(MemoryIntentHandler.class).to(IMemoryIntentHandler.class);
        binder.bindFactory(new InstanceFactory<>(mock(DatabaseUser.class))).to(DatabaseUser.class);
        return binder;
    }

}
