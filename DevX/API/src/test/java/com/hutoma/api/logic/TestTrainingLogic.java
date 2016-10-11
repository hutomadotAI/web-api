package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.validation.TestParameterValidation;
import com.hutoma.api.validation.Validate;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.hutoma.api.containers.sub.TrainingStatus.trainingStatus.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 12/08/2016.
 */
public class TestTrainingLogic {

    Config fakeConfig;
    MessageQueue fakeMessageQueue;
    Database fakeDatabase;
    Tools fakeTools;
    Logger fakeLogger;
    SecurityContext fakeContext;
    HTMLExtractor fakeExtractor;
    FormDataContentDisposition fakeContentDisposition;
    Validate fakeValidation;
    TrainingLogic logic;

    private String DEVID = "devid";
    private UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String UURL = "url://";
    private String SOMETEXT = "some text\nsome response";
    private String TEXTMULTILINE = "line\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\n";

    private ApiAi getFakeAI(TrainingStatus.trainingStatus status) {
        return new ApiAi(AIID.toString(), "client_token", "ai_name", "ai_description", new DateTime(), false,0, "", "", status.name(), null);
    }

    @Before
    public void setup() throws Database.DatabaseException {

        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        when(fakeDatabase.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(Logger.class);
        when(fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.fakeValidation = TestParameterValidation.getFakeValidation();
        when(fakeValidation.textSanitizer(anyString())).thenCallRealMethod();
        this.fakeExtractor = mock(HTMLExtractor.class);
        this.fakeContentDisposition = mock(FormDataContentDisposition.class);
        logic = new TrainingLogic(fakeConfig, fakeMessageQueue, fakeExtractor, fakeDatabase, fakeTools, fakeLogger, fakeValidation);

        when(fakeConfig.getMaxUploadSize()).thenReturn(65536L);
        when(fakeConfig.getMaxClusterLines()).thenReturn(65536);
    }

    InputStream createUpload(String content) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            when(fakeContentDisposition.getSize()).thenReturn((long) stream.available());
        } catch (IOException e) {
            // ignore!
        }
        return stream;
    }

    @Test
    public void testTrain_TextSimple() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testTrain_DocSimple() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 1, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testTrain_UrlSimple() throws HTMLExtractor.HtmlExtractionException {
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 2, UURL, null, null);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Text_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(413, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 1, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(413, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Url_ExtractTooLarge() throws HTMLExtractor.HtmlExtractionException {
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(TEXTMULTILINE);
        when(fakeConfig.getMaxUploadSize()).thenReturn((long) TEXTMULTILINE.length() - 1);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 2, UURL, null, null);
        Assert.assertEquals(413, result.getStatus().getCode());
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

    void makeDBFail(int trainingType) throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        doThrow(new Database.DatabaseException(new Exception("test"))).when(fakeDatabase).updateAiTrainingFile(any(), anyString());
        InputStream stream = createUpload(SOMETEXT);
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, trainingType, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(500, result.getStatus().getCode());
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

    void makeDBUpdateZeroRows(int trainingType) throws Database.DatabaseException, HTMLExtractor.HtmlExtractionException {
        when(fakeDatabase.updateAiTrainingFile(any(), anyString())).thenReturn(false);
        InputStream stream = createUpload(SOMETEXT);
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, trainingType, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(404, result.getStatus().getCode());
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

    void makeMessageQueueFail(int trainingType) throws Exception {
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(fakeMessageQueue).pushMessageReadyForTraining(anyString(), any());
        doThrow(new MessageQueue.MessageQueueException(new Exception("test"))).when(fakeMessageQueue).pushMessagePreprocessTrainingText(anyString(), any());
        InputStream stream = createUpload(SOMETEXT);
        when(fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, trainingType, UURL, stream, fakeContentDisposition);

        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testTrain_BadTrainingType() {
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, -1, UURL, stream, fakeContentDisposition);
        Assert.assertEquals(400, result.getStatus().getCode());
    }


    @Test
    public void testStartTraining() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_not_started));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining_BadRequest_TrainingWasCompleted() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_completed));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining_BadRequest_TrainingIsInProgress() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_in_progress));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining_TrainingWasStopped() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_stopped));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }


    @Test
    public void testStartTraining_TrainingAlreadyQueued() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_queued));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }


    @Test
    public void testStartTraining_TrainigWasDeleted() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_deleted));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.startTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }


    @Test
    public void testStopTraining() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_in_progress));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.stopTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasNotInProgress() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_not_started));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.stopTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasCompleted() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_completed));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.stopTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testStopTraining_BadRequest_TrainingIsOnlyQueued() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_queued));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.stopTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testStopTraining_BadRequest_TrainingWasDeleted() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any(), any())).thenReturn(getFakeAI(training_deleted));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = logic.uploadFile(fakeContext, DEVID, AIID, 0, UURL, stream, fakeContentDisposition);
        result = logic.stopTraining(fakeContext, DEVID, AIID);
        Assert.assertEquals(400, result.getStatus().getCode());
    }







}