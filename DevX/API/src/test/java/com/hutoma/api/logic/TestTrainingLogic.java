package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.validation.TestParameterValidation;
import com.hutoma.api.validation.Validate;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.containers.sub.TrainingStatus.trainingStatus.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 12/08/2016.
 */
public class TestTrainingLogic {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String UURL = "url://";
    private static final String SOMETEXT = "some text\nsome response";
    private static final String TEXTMULTILINE = "line\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\n";
    private static final String EOL = "\n";
    private Config fakeConfig;
    private MessageQueue fakeMessageQueue;
    private Database fakeDatabase;
    private Tools fakeTools;
    private Logger fakeLogger;
    private SecurityContext fakeContext;
    private HTMLExtractor fakeExtractor;
    private FormDataContentDisposition fakeContentDisposition;
    private Validate fakeValidation;
    private TrainingLogic logic;

    @Before
    public void setup() throws Database.DatabaseException {

        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        when(this.fakeDatabase.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(Logger.class);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.fakeValidation = TestParameterValidation.getFakeValidation();
        when(this.fakeValidation.textSanitizer(anyString())).thenCallRealMethod();
        this.fakeExtractor = mock(HTMLExtractor.class);
        this.fakeContentDisposition = mock(FormDataContentDisposition.class);
        this.logic = new TrainingLogic(this.fakeConfig, this.fakeMessageQueue, this.fakeExtractor, this.fakeDatabase, this.fakeTools, this.fakeLogger, this.fakeValidation);

        when(this.fakeConfig.getMaxUploadSize()).thenReturn(65536L);
        when(this.fakeConfig.getMaxClusterLines()).thenReturn(65536);
    }

    @Test
    public void testTrain_TextSimple() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 0, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_DocSimple() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 1, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_UrlSimple() throws HTMLExtractor.HtmlExtractionException {
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 2, UURL, null, null);
        Assert.assertEquals(HttpServletResponse.SC_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Text_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(this.fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 0, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(this.fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 1, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Url_ExtractTooLarge() throws HTMLExtractor.HtmlExtractionException {
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(TEXTMULTILINE);
        when(this.fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 2, UURL, null, null);
        Assert.assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_TextDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 0;
        makeDBFail(trainingType);
    }

    @Test
    public void testTrain_DocDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 1;
        makeDBFail(trainingType);
    }

    @Test
    public void testTrain_UrlDBFail() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 2;
        makeDBFail(trainingType);
    }

    @Test
    public void testTrain_TextDBNotFound() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 0;
        makeDBUpdateZeroRows(trainingType);
    }

    @Test
    public void testTrain_DocDBNotFound() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 1;
        makeDBUpdateZeroRows(trainingType);
    }

    @Test
    public void testTrain_UrlDBNotFound() throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        int trainingType = 2;
        makeDBUpdateZeroRows(trainingType);
    }

    @Test
    public void testTrain_TextMessageFail() throws Exception {
        int trainingType = 0;
        makeMessageQueueFail(trainingType);
    }

    @Test
    public void testTrain_DocMessageFail() throws Exception {
        int trainingType = 1;
        makeMessageQueueFail(trainingType);
    }

    @Test
    public void testTrain_UrlMessageFail() throws Exception {
        int trainingType = 2;
        makeMessageQueueFail(trainingType);
    }

    @Test
    public void testTrain_BadTrainingType() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, -1, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining() throws Database.DatabaseException {
        testStartTrainingCommon(training_not_started, HttpServletResponse.SC_OK);
    }

    @Test
    public void testStartTraining_BadRequest_TrainingWasCompleted() throws Database.DatabaseException {
        testStartTrainingCommon(training_completed, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStartTraining_BadRequest_TrainingIsInProgress() throws Database.DatabaseException {
        testStartTrainingCommon(training_in_progress, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStartTraining_TrainingWasStopped() throws Database.DatabaseException {
        testStartTrainingCommon(training_stopped, HttpServletResponse.SC_OK);
    }

    @Test
    public void testStartTraining_TrainingAlreadyQueued() throws Database.DatabaseException {
        testStartTrainingCommon(training_queued, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStartTraining_TrainigWasDeleted() throws Database.DatabaseException {
        testStartTrainingCommon(training_deleted, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStopTraining() throws Database.DatabaseException {
        testStopTrainingCommon(training_in_progress, HttpServletResponse.SC_OK);
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasNotInProgress() throws Database.DatabaseException {
        testStopTrainingCommon(TrainingStatus.trainingStatus.training_not_started, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasCompleted() throws Database.DatabaseException {
        testStopTrainingCommon(TrainingStatus.trainingStatus.training_completed, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStopTraining_BadRequest_TrainingIsOnlyQueued() throws Database.DatabaseException {
        testStopTrainingCommon(TrainingStatus.trainingStatus.training_queued, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasDeleted() throws Database.DatabaseException {
        testStopTrainingCommon(TrainingStatus.trainingStatus.training_deleted, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testGetTrainingMaterials_multipleIntents() throws Database.DatabaseException {
        final String trainingFile = "Q1\nA1\n";
        final List<String> intentNames = Arrays.asList("intent1", "intent2");
        final List<String> userSaysIntent1 = Arrays.asList("a b", "xy");
        final List<String> userSaysIntent2 = Collections.singletonList("request something");

        ApiAi apiAi = new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), true, 0.5, "", "", "", null);
        ApiIntent intent1 = new ApiIntent(intentNames.get(0), "", "");
        intent1.setUserSays(userSaysIntent1);
        ApiIntent intent2 = new ApiIntent(intentNames.get(1), "", "");
        intent2.setUserSays(userSaysIntent2);

        when(this.fakeDatabase.getAI(DEVID, AIID)).thenReturn(apiAi);
        when(this.fakeDatabase.getAiTrainingFile(AIID)).thenReturn(trainingFile);
        when(this.fakeDatabase.getIntents(DEVID, AIID)).thenReturn(intentNames);
        when(this.fakeDatabase.getIntent(DEVID, AIID, intentNames.get(0))).thenReturn(intent1);
        when(this.fakeDatabase.getIntent(DEVID, AIID, intentNames.get(1))).thenReturn(intent2);
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);

        // build the expected training which will be:
        // training file \n userSaysIntent1 \n intent1 \n ...
        StringBuilder sb = new StringBuilder();
        sb.append(trainingFile).append(EOL);
        sb.append(userSaysIntent1.get(0)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(0)).append(EOL).append(EOL);
        sb.append(userSaysIntent1.get(1)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(0)).append(EOL).append(EOL);
        sb.append(userSaysIntent2.get(0)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(1)).append(EOL);

        Assert.assertEquals(HttpServletResponse.SC_OK, materials.getStatus().getCode());
        Assert.assertEquals(sb.toString(), materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_noIntents() throws Database.DatabaseException {
        final String trainingFile = "Q1\nA1\n";
        ApiAi apiAi = new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), true, 0.5, "", "", "", null);
        when(this.fakeDatabase.getAI(DEVID, AIID)).thenReturn(apiAi);
        when(this.fakeDatabase.getAiTrainingFile(AIID)).thenReturn(trainingFile);
        when(this.fakeDatabase.getIntents(DEVID, AIID)).thenReturn(new ArrayList<>());
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(trainingFile, materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_noTrainingFileNoIntents() throws Database.DatabaseException {
        ApiAi apiAi = new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), true, 0.5, "", "", "", null);
        when(this.fakeDatabase.getAI(DEVID, AIID)).thenReturn(apiAi);
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(HttpServletResponse.SC_OK, materials.getStatus().getCode());
        Assert.assertEquals("", materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_invalidAiid() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any())).thenReturn(null);
        ApiError error = (ApiError) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, error.getStatus().getCode());
    }

    @Test
    public void testGetTrainingMaterials_withIntentsNoTrainingFile() throws Database.DatabaseException {
        final String userSays = "the user says";
        final String intentName = "intent1";
        ApiAi apiAi = new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), true, 0.5, "", "", "", null);
        ApiIntent intent1 = new ApiIntent(intentName, "", "");
        intent1.setUserSays(Collections.singletonList(userSays));
        when(this.fakeDatabase.getAI(DEVID, AIID)).thenReturn(apiAi);
        when(this.fakeDatabase.getIntents(DEVID, AIID)).thenReturn(Collections.singletonList(intentName));
        when(this.fakeDatabase.getIntent(DEVID, AIID, intentName)).thenReturn(intent1);
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(HttpServletResponse.SC_OK, materials.getStatus().getCode());

        StringBuilder sb = new StringBuilder();
        sb.append(EOL);
        sb.append(userSays).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentName).append(EOL);
        Assert.assertEquals(sb.toString(), materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_dbException() throws Database.DatabaseException {
        Database.DatabaseException exception = new Database.DatabaseException(new Exception("dummy exception"));
        when(this.fakeDatabase.getAI(DEVID, AIID)).thenThrow(exception);
        ApiError error = (ApiError) this.logic.getTrainingMaterials(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.getStatus().getCode());
        verify(this.fakeLogger).logError(anyString(), anyString());
    }

    private void testStartTrainingCommon(final TrainingStatus.trainingStatus trainingStatus, final int expectedStatus)
            throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(trainingStatus));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 0, UURL, stream, this.fakeContentDisposition);
        result = this.logic.startTraining(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(expectedStatus, result.getStatus().getCode());
    }

    private ApiAi getFakeAI(TrainingStatus.trainingStatus status) {
        return new ApiAi(AIID.toString(), "client_token", "ai_name", "ai_description", new DateTime(), false, 0, "", "", status.name(), null);
    }

    private void testStopTrainingCommon(final TrainingStatus.trainingStatus status, final int expectedCode) throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(status));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, 0, UURL, stream, this.fakeContentDisposition);
        result = this.logic.stopTraining(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(expectedCode, result.getStatus().getCode());
    }

    private InputStream createUpload(String content) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            when(this.fakeContentDisposition.getSize()).thenReturn((long) stream.available());
        } catch (IOException e) {
            // ignore!
        }
        return stream;
    }

    private void makeDBFail(int trainingType) throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        doThrow(new Database.DatabaseException(new Exception("test"))).when(this.fakeDatabase).updateAiTrainingFile(any(), anyString());
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result.getStatus().getCode());
    }

    private void makeDBUpdateZeroRows(int trainingType) throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        when(this.fakeDatabase.updateAiTrainingFile(any(), anyString())).thenReturn(false);
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    private void makeMessageQueueFail(int trainingType) throws Exception {
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(this.fakeMessageQueue).pushMessageReadyForTraining(anyString(), any());
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(this.fakeMessageQueue).pushMessagePreprocessTrainingText(anyString(), any());
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(this.fakeContext, DEVID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);

        result = this.logic.startTraining(this.fakeContext, DEVID, AIID);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result.getStatus().getCode());
    }


}