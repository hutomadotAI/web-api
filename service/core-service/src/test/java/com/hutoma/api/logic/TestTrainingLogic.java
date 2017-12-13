package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.HTMLExtractor;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.validation.TestParameterValidation;
import com.hutoma.api.validation.Validate;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 12/08/2016.
 */
@RunWith(DataProviderRunner.class)
public class TestTrainingLogic {

    private static final String UURL = "url://";
    private static final String SOMETEXT = "some text\nsome response";
    private static final String TEXTMULTILINE = "line\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\nline\n";
    private static final String EOL = "\n";
    private static final String DEFAULT_TOPIC = topic("topic1");
    private Config fakeConfig;
    private AIServices fakeAiServices;
    private DatabaseAI fakeDatabaseAi;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private Tools fakeTools;
    private ILogger fakeLogger;
    private HTMLExtractor fakeExtractor;
    private FormDataContentDisposition fakeContentDisposition;
    private Validate fakeValidation;
    private TrainingLogic logic;
    private IMemoryIntentHandler fakeIntentHandler;
    private JsonSerializer fakeSerializer;

    @DataProvider
    public static Object[] updateTraining_successStates() {
        return new Object[]{
                TrainingStatus.AI_TRAINING,
                TrainingStatus.AI_READY_TO_TRAIN,
                TrainingStatus.AI_TRAINING_STOPPED,
                TrainingStatus.AI_TRAINING_COMPLETE,
                TrainingStatus.AI_TRAINING_QUEUED
        };
    }

    @DataProvider
    public static Object[] updateTraining_failureStates() {
        return new Object[]{
                TrainingStatus.AI_UNDEFINED,
                TrainingStatus.AI_ERROR
        };
    }

    @DataProvider
    public static Object[] startTraining_successStates() {
        return new Object[]{
                TrainingStatus.AI_READY_TO_TRAIN,
                TrainingStatus.AI_TRAINING_STOPPED
        };
    }

    @DataProvider
    public static Object[] startTraining_failureStates() {
        return new Object[]{
                TrainingStatus.AI_ERROR,
                TrainingStatus.AI_UNDEFINED,
                TrainingStatus.AI_TRAINING_COMPLETE,
                TrainingStatus.AI_TRAINING_QUEUED,
                TrainingStatus.AI_TRAINING
        };
    }

    @DataProvider
    public static Object[] stopTraining_successStates() {
        return new Object[]{
                TrainingStatus.AI_TRAINING
        };
    }

    @DataProvider
    public static Object[] stopTraining_failureStates() {
        return new Object[]{
                TrainingStatus.AI_ERROR,
                TrainingStatus.AI_UNDEFINED,
                TrainingStatus.AI_TRAINING_COMPLETE
        };
    }

    @DataProvider
    public static Object[] topicsNonClosedConversation() {
        return new Object[]{
                Arrays.asList("Q1", DEFAULT_TOPIC, "Q2", "A2"),
                Arrays.asList("Q1", "", DEFAULT_TOPIC, "Q2", "A2"),
                Arrays.asList("Q2", "A2", DEFAULT_TOPIC)
        };
    }

    private static String topic(final String topicName) {
        return String.format("%s=%s", TrainingLogic.TOPIC_MARKER, topicName);
    }

    @Before
    public void setup() throws DatabaseException {

        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(ILogger.class);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.fakeValidation = TestParameterValidation.getFakeValidation();
        when(this.fakeValidation.filterControlAndCoalesceSpaces(anyString())).thenCallRealMethod();
        this.fakeExtractor = mock(HTMLExtractor.class);
        this.fakeContentDisposition = mock(FormDataContentDisposition.class);
        this.fakeIntentHandler = mock(IMemoryIntentHandler.class);
        this.fakeSerializer = mock(JsonSerializer.class);
        this.logic = new TrainingLogic(this.fakeConfig, this.fakeAiServices, this.fakeExtractor, this.fakeDatabaseAi,
                this.fakeLogger, this.fakeValidation, this.fakeIntentHandler, this.fakeSerializer);

        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(4096);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
    }

    @Test
    public void testTrain_TextSimple() throws DatabaseException {
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(TrainingStatus.AI_READY_TO_TRAIN, true));
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn("training file");
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("training file");
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_DocSimple() throws DatabaseException {
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.DOCUMENT, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_UrlSimple() throws HTMLExtractor.HtmlExtractionException, DatabaseException {
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.WEBPAGE, UURL, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Text_UploadTooLarge() {
        InputStream stream = createUpload(moreThan1Kb());
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Text_UploadNull() {
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, null, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_UploadTooLarge() {
        InputStream stream = createUpload(TEXTMULTILINE);
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(0);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.DOCUMENT, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_UploadNull() {
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.DOCUMENT, UURL, null, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_UploadTooLarge_checkUploadFileSizeIncorrect() {
        InputStream stream = createUpload(moreThan1Kb());
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.DOCUMENT, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Doc_Upload_nullFileDetail() {
        InputStream stream = createUpload(moreThan1Kb());
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.DOCUMENT, UURL, stream, null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testTrain_Url_ExtractTooLarge() throws HTMLExtractor.HtmlExtractionException, DatabaseException {
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(moreThan1Kb());
        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.WEBPAGE, UURL, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, result.getStatus().getCode());
    }

    @Test
    public void testTrain_upload_invalidContentDisposition() throws HTMLExtractor.HtmlExtractionException {
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testTrain_upload_AI_notFound() throws HTMLExtractor.HtmlExtractionException, DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testTrain_upload_AI_readonly() throws HTMLExtractor.HtmlExtractionException, DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testTrain_TextDBFail() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBFail(TrainingLogic.TrainingType.TEXT);
    }

    @Test
    public void testTrain_DocDBFail() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBFail(TrainingLogic.TrainingType.DOCUMENT);
    }

    @Test
    public void testTrain_UrlDBFail() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBFail(TrainingLogic.TrainingType.WEBPAGE);
    }

    @Test
    public void testTrain_TextDBNotFound() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBUpdateZeroRows(TrainingLogic.TrainingType.TEXT);
    }

    @Test
    public void testTrain_DocDBNotFound() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBUpdateZeroRows(TrainingLogic.TrainingType.DOCUMENT);
    }

    @Test
    public void testTrain_UrlDBNotFound() throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        makeDBUpdateZeroRows(TrainingLogic.TrainingType.WEBPAGE);
    }

    @Test
    public void testTrain_TextMessageFail() throws Exception {
        makeAiServiceLogicFail(TrainingLogic.TrainingType.TEXT);
    }

    @Test
    public void testTrain_DocMessageFail() throws Exception {
        makeAiServiceLogicFail(TrainingLogic.TrainingType.DOCUMENT);
    }

    @Test
    public void testTrain_UrlMessageFail() throws Exception {
        makeAiServiceLogicFail(TrainingLogic.TrainingType.WEBPAGE);
    }

    @Test
    @UseDataProvider("startTraining_successStates")
    public void testStartTraining_initialStates_success(TrainingStatus initialState) throws DatabaseException,
            AIServices.AiServicesException {
        testStartTrainingCommon(initialState, HttpURLConnection.HTTP_OK);
    }

    @Test
    @UseDataProvider("startTraining_failureStates")
    public void testStartTraining_initialStates_failure(TrainingStatus initialState) throws DatabaseException,
            AIServices.AiServicesException {
        testStartTrainingCommon(initialState, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void testStartTraining_unknownAi() throws DatabaseException, AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.logic.startTraining(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining_AI_readonly() throws DatabaseException, AIServices.AiServicesException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = this.logic.startTraining(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testStartTraining_dbException() throws DatabaseException, AIServices.AiServicesException {
        testTraining_dbException(() -> this.logic.startTraining(DEVID_UUID, AIID),
                HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    @UseDataProvider("stopTraining_successStates")
    public void testStopTraining_initialStates_success(TrainingStatus initialState) throws DatabaseException {
        testStopTrainingCommon(initialState, HttpURLConnection.HTTP_OK);
    }

    @Test
    @UseDataProvider("stopTraining_failureStates")
    public void testStopTraining_initialStates_failure(TrainingStatus initialState) throws DatabaseException {
        testStopTrainingCommon(initialState, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void testStopTraining_unknownAi() throws DatabaseException, AIServices.AiServicesException {
        testTraining_invalidAi(() -> this.logic.stopTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testStopTraining_AI_readonly() throws DatabaseException, AIServices.AiServicesException {
        testTraining_AI_readonly(() -> this.logic.stopTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testStopTraining_dbException() throws DatabaseException, AIServices.AiServicesException {
        testTraining_dbException(() -> this.logic.stopTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testStartTraining_invalidAi() throws DatabaseException, AIServices.AiServicesException {
        testTraining_invalidAi(() -> this.logic.startTraining(DEVID_UUID, AIID));
    }

    @Test
    @UseDataProvider("updateTraining_successStates")
    public void testUpdateTraining_initialStates_success(TrainingStatus initialState) throws DatabaseException {
        testUpdateTraining_initialStates_common(initialState, HttpURLConnection.HTTP_OK);
    }

    @Test
    @UseDataProvider("updateTraining_failureStates")
    public void testUpdateTraining_initialStates_failure(TrainingStatus initialState) throws DatabaseException {
        testUpdateTraining_initialStates_common(initialState, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void testUpdateTraining_invalidAi() throws DatabaseException, AIServices.AiServicesException {
        testTraining_invalidAi(() -> this.logic.updateTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testUpdateTraining_AI_readonly() throws DatabaseException, AIServices.AiServicesException {
        testTraining_AI_readonly(() -> this.logic.updateTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testUpdateTraining_dbException() throws DatabaseException, AIServices.AiServicesException {
        testTraining_dbException(() -> this.logic.updateTraining(DEVID_UUID, AIID));
    }

    @Test
    public void testUpdateTraining_failedUploading() throws DatabaseException, AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(SOMETEXT);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(SOMETEXT);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).uploadTraining(any(), any(), any(), anyString());
        ApiResult result = this.logic.updateTraining(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateTraining_withFileToUpload_withIntents()
            throws DatabaseException, AIServices.AiServicesException {
        verifyUpdateTraining_withIntents(SOMETEXT);
    }

    @Test
    public void testUpdateTraining_noFileToUpload_withIntents()
            throws DatabaseException, AIServices.AiServicesException {
        verifyUpdateTraining_withIntents(null);
    }

    @Test
    public void testUpdateTraining_emptyFileToUpload_withIntents()
            throws DatabaseException, AIServices.AiServicesException {
        verifyUpdateTraining_withIntents("");
    }

    @Test
    public void testUpdateTraining_noFileToUpload_noIntents()
            throws DatabaseException, AIServices.AiServicesException {
        verifyUpdateTraining_noIntents(null);
    }

    @Test
    public void testUpdateTraining_withFileToUpload_noIntents()
            throws DatabaseException, AIServices.AiServicesException {
        verifyUpdateTraining_noIntents(SOMETEXT);
    }

    @Test
    public void testGetTrainingMaterials_multipleIntents() throws DatabaseException {
        final String trainingFile = "Q1\nA1\n";
        final List<String> intentNames = Arrays.asList("intent1", "intent2");
        final List<String> userSaysIntent1 = Arrays.asList("a b", "xy");
        final List<String> userSaysIntent2 = Collections.singletonList("request something");

        ApiAi apiAi = getAi(TrainingStatus.AI_READY_TO_TRAIN, true);
        ApiIntent intent1 = new ApiIntent(intentNames.get(0), "", "");
        intent1.setUserSays(userSaysIntent1);
        ApiIntent intent2 = new ApiIntent(intentNames.get(1), "", "");
        intent2.setUserSays(userSaysIntent2);

        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(getTrainingMaterials(trainingFile, intentNames, userSaysIntent1, userSaysIntent2));
        when(this.fakeDatabaseAi.getAI(DEVID_UUID, AIID, this.fakeSerializer)).thenReturn(apiAi);
        when(this.fakeDatabaseAi.getAiTrainingFile(AIID)).thenReturn(trainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(DEVID_UUID, AIID)).thenReturn(intentNames);
        when(this.fakeDatabaseEntitiesIntents.getIntent(AIID, intentNames.get(0))).thenReturn(intent1);
        when(this.fakeDatabaseEntitiesIntents.getIntent(AIID, intentNames.get(1))).thenReturn(intent2);
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(DEVID_UUID, AIID);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, materials.getStatus().getCode());
        Assert.assertEquals(getTrainingMaterials(trainingFile, intentNames, userSaysIntent1, userSaysIntent2),
                materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_noIntents() throws DatabaseException {
        final String trainingFile = "Q1\nA1\n";
        ApiAi apiAi = getAi(TrainingStatus.AI_READY_TO_TRAIN, true);
        when(this.fakeDatabaseAi.getAI(DEVID_UUID, AIID, this.fakeSerializer)).thenReturn(apiAi);
        when(this.fakeDatabaseAi.getAiTrainingFile(AIID)).thenReturn(trainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(DEVID_UUID, AIID)).thenReturn(new ArrayList<>());
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(trainingFile);
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(DEVID_UUID, AIID);
        Assert.assertEquals(trainingFile, materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_noTrainingFileNoIntents() throws DatabaseException {
        ApiAi apiAi = getAi(TrainingStatus.AI_READY_TO_TRAIN, true);
        when(this.fakeDatabaseAi.getAI(DEVID_UUID, AIID, this.fakeSerializer)).thenReturn(apiAi);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("");
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, materials.getStatus().getCode());
        Assert.assertEquals("", materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_invalidAiid() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiError error = (ApiError) this.logic.getTrainingMaterials(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, error.getStatus().getCode());
    }

    @Test
    public void testGetTrainingMaterials_withIntentsNoTrainingFile() throws DatabaseException {
        final String userSays = "the user says";
        final String intentName = "intent1";
        ApiAi apiAi = getAi(TrainingStatus.AI_READY_TO_TRAIN, true);
        ApiIntent intent1 = new ApiIntent(intentName, "", "");
        intent1.setUserSays(Collections.singletonList(userSays));

        StringBuilder sb = new StringBuilder();
        sb.append(userSays).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentName).append(EOL);

        when(this.fakeDatabaseAi.getAI(DEVID_UUID, AIID, this.fakeSerializer)).thenReturn(apiAi);
        when(this.fakeDatabaseEntitiesIntents.getIntents(DEVID_UUID, AIID)).thenReturn(Collections.singletonList(intentName));
        when(this.fakeDatabaseEntitiesIntents.getIntent(AIID, intentName)).thenReturn(intent1);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(sb.toString());
        ApiTrainingMaterials materials = (ApiTrainingMaterials) this.logic.getTrainingMaterials(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, materials.getStatus().getCode());
        Assert.assertEquals(sb.toString(), materials.getTrainingFile());
    }

    @Test
    public void testGetTrainingMaterials_dbException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(DEVID_UUID, AIID, this.fakeSerializer)).thenThrow(DatabaseException.class);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenThrow(DatabaseException.class);
        ApiError error = (ApiError) this.logic.getTrainingMaterials(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, error.getStatus().getCode());
        verify(this.fakeLogger).logUserExceptionEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testUploadTraining_serviceException() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(TrainingStatus.AI_TRAINING_COMPLETE, true));
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(SOMETEXT);
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).uploadTraining(any(), any(), any(), anyString());
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, null, createUpload(SOMETEXT), this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testTrain_topics_oddNumberOfTopics_doesNotGenerateError() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList("Q1", "A1", DEFAULT_TOPIC, "Q2", "A2"));
        Assert.assertEquals(String.format("Q1\nA1\n%s\nQ2\nA2\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(0, result.getEvents().size());
    }

    @Test
    public void testTrain_topics_topicAfterNotClosedConversation_ignoresQuestion() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList("Q1", DEFAULT_TOPIC, "Q2", "A2"));
        Assert.assertEquals(String.format("%s\nQ2\nA2\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(1, result.getEvents().size());
        result = this.logic.parseTrainingFile(Arrays.asList("Q1", "", DEFAULT_TOPIC, "Q2", "A2"));
        Assert.assertEquals(String.format("\n%s\nQ2\nA2\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(1, result.getEvents().size());
        result = this.logic.parseTrainingFile(Arrays.asList("Q1", "A1", DEFAULT_TOPIC));
        Assert.assertEquals(String.format("Q1\nA1\n%s\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(0, result.getEvents().size());
    }

    @Test
    public void testTrain_topics_fileStartsWithTopic() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList(DEFAULT_TOPIC, "Q1", "A1"));
        Assert.assertEquals(String.format("%s\nQ1\nA1\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(0, result.getEvents().size());
    }

    @Test
    public void testTrain_topics_topicEnd_blankLine_isPreserved() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList(DEFAULT_TOPIC, "Q1", "A1", "", "Q2", "A2"));
        Assert.assertEquals(String.format("%s\nQ1\nA1\n\nQ2\nA2\n\n", DEFAULT_TOPIC), result.getTrainingText());
        Assert.assertEquals(0, result.getEvents().size());
    }

    @Test
    public void testTrain_topics_topicWithNoConversations_doesNotGenerateError() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList("Q1", "A1", DEFAULT_TOPIC));
        Assert.assertEquals(String.format("Q1\nA1\n%s\n\n", DEFAULT_TOPIC), result.getTrainingText());
        result = this.logic.parseTrainingFile(Arrays.asList("Q1", "A1", DEFAULT_TOPIC, topic("topic2"), "Q2", "A2"));
        Assert.assertEquals(String.format("Q1\nA1\n%s\n%s\nQ2\nA2\n\n", DEFAULT_TOPIC, topic("topic2")), result.getTrainingText());
    }

    private void verifyUpdateTraining_noIntents(final String fileToUpload)
            throws DatabaseException, AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(fileToUpload);
        when(this.fakeDatabaseEntitiesIntents.getIntents(DEVID_UUID, AIID)).thenReturn(Collections.emptyList());
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(fileToUpload);
        ApiResult result = this.logic.updateTraining(DEVID_UUID, AIID);
        if (fileToUpload == null) {
            Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
        } else {
            Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
            verify(this.fakeAiServices).uploadTraining(any(), eq(DEVID_UUID), eq(AIID), eq(fileToUpload));
        }
    }

    private void verifyUpdateTraining_withIntents(final String fileToUpload)
            throws DatabaseException, AIServices.AiServicesException {
        final List<String> intentNames = Arrays.asList("intent1", "intent2");
        final List<String> userSaysIntent1 = Arrays.asList("a b", "xy");
        final List<String> userSaysIntent2 = Collections.singletonList("request something");
        ApiIntent intent1 = new ApiIntent(intentNames.get(0), "", "");
        intent1.setUserSays(userSaysIntent1);
        ApiIntent intent2 = new ApiIntent(intentNames.get(1), "", "");
        intent2.setUserSays(userSaysIntent2);

        when(this.fakeDatabaseEntitiesIntents.getIntent(AIID, intentNames.get(0))).thenReturn(intent1);
        when(this.fakeDatabaseEntitiesIntents.getIntent(AIID, intentNames.get(1))).thenReturn(intent2);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(fileToUpload);
        when(this.fakeDatabaseEntitiesIntents.getIntents(DEVID_UUID, AIID)).thenReturn(intentNames);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn(getTrainingMaterials(fileToUpload, intentNames, userSaysIntent1, userSaysIntent2));
        ApiResult result = this.logic.updateTraining(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());

        verify(this.fakeAiServices).uploadTraining(any(), eq(DEVID_UUID), eq(AIID),
                eq(getTrainingMaterials(fileToUpload, intentNames, userSaysIntent1, userSaysIntent2)));
    }

    private String getTrainingMaterials(final String trainingFile, final List<String> intentNames,
                                        final List<String> userSaysIntent1, final List<String> userSaysIntent2) {
        // build the expected training which will be:
        // training file \n userSaysIntent1 \n intent1 \n ...
        StringBuilder sb = new StringBuilder();
        if (trainingFile != null && !trainingFile.isEmpty()) {
            sb.append(trainingFile).append(EOL);
        }
        sb.append(userSaysIntent1.get(0)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(0)).append(EOL).append(EOL);
        sb.append(userSaysIntent1.get(1)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(0)).append(EOL).append(EOL);
        sb.append(userSaysIntent2.get(0)).append(EOL);
        sb.append(MemoryIntentHandler.META_INTENT_TAG).append(intentNames.get(1)).append(EOL);
        return sb.toString();
    }

    private void testTraining_invalidAi(Supplier<ApiResult> supplier) throws DatabaseException,
            AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = supplier.get();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).startTraining(any(), any(), any());
    }

    private void testTraining_AI_readonly(Supplier<ApiResult> supplier) throws DatabaseException,
            AIServices.AiServicesException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = supplier.get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(any(), any(), any());
        verify(this.fakeAiServices, never()).startTraining(any(), any(), any());
    }

    private void testTraining_dbException(Supplier<ApiResult> supplier, int expectedErrorCode)
            throws DatabaseException, AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = supplier.get();
        Assert.assertEquals(expectedErrorCode, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).startTraining(any(), any(), any());
    }

    private void testTraining_dbException(Supplier<ApiResult> supplier) throws DatabaseException,
            AIServices.AiServicesException {
        testTraining_dbException(supplier, HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    private void testUpdateTraining_initialStates_common(TrainingStatus initialState, int expectedCode)
            throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(initialState, false));
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn("Q1\nA1");
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("Q1\nA1");
        ApiResult result = this.logic.updateTraining(DEVID_UUID, AIID);
        Assert.assertEquals(expectedCode, result.getStatus().getCode());
    }

    private void testStartTrainingCommon(final TrainingStatus trainingStatus, final int expectedStatus)
            throws DatabaseException, AIServices.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(trainingStatus, false));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, stream, this.fakeContentDisposition);
        result = this.logic.startTraining(DEVID_UUID, AIID);
        Assert.assertEquals(expectedStatus, result.getStatus().getCode());
    }

    private void testStopTrainingCommon(final TrainingStatus status, final int expectedCode) throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getAi(status, false));
        InputStream stream = createUpload(SOMETEXT);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, TrainingLogic.TrainingType.TEXT, UURL, stream, this.fakeContentDisposition);
        result = this.logic.stopTraining(DEVID_UUID, AIID);
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

    private String moreThan1Kb() {
        StringBuilder sb = new StringBuilder();
        byte[] content = null;
        do {
            sb.append(TEXTMULTILINE);
        } while (sb.toString().getBytes(StandardCharsets.UTF_8).length <= 1024);
        return sb.toString();
    }

    private void makeDBFail(TrainingLogic.TrainingType trainingType) throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        doThrow(DatabaseException.class).when(this.fakeDatabaseAi).updateAiTrainingFile(any(), anyString());
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void makeDBUpdateZeroRows(TrainingLogic.TrainingType trainingType) throws DatabaseException, HTMLExtractor.HtmlExtractionException {
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString())).thenReturn(false);
        InputStream stream = createUpload(SOMETEXT);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    private void makeAiServiceLogicFail(TrainingLogic.TrainingType trainingType) throws Exception {
        doThrow(AIServices.AiServicesException.class).when(this.fakeAiServices).startTraining(any(), any(), any());
        InputStream stream = createUpload(SOMETEXT);
        ApiAi ai = new ApiAi(AIID.toString(), "", "ai", "", DateTime.now(), true, new BackendStatus(),
                true,
                0, 0.5, 1, Locale.UK, "UTC", null, "", DEFAULT_CHAT_RESPONSES, DEFAULT_API_KEY_DESC) {
            @Override
            public TrainingStatus getSummaryAiStatus() {
                return TrainingStatus.AI_READY_TO_TRAIN;
            }
        };
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(ai);
        when(this.fakeExtractor.getTextFromUrl(anyString())).thenReturn(SOMETEXT);
        ApiResult result = this.logic.uploadFile(DEVID_UUID, AIID, trainingType, UURL, stream, this.fakeContentDisposition);

        result = this.logic.startTraining(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }
}
