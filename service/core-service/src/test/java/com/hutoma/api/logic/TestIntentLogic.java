package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.CsvIntentReader;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.ApiCsvImportResult;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.validation.Validate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Provider;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 10/10/2016.
 */
public class TestIntentLogic {

    private static final String INTENTNAME = "intent";
    private static final String TOPICIN = "topicin";
    private static final String TOPICOUT = "topicout";
    private DatabaseAI fakeDatabase;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private Provider<DatabaseTransaction> fakeDatabaseTransactionProvider;
    private DatabaseTransaction fakeDatabaseTransaction;
    private Config fakeConfig;
    private IntentLogic intentLogic;
    private ILogger fakeLogger;
    private TrainingLogic trainingLogic;
    private CsvIntentReader fakeCsvIntentReader;
    private Validate fakeValidator;

    public static ApiIntent getIntent() {
        return new ApiIntent(INTENTNAME, TOPICIN, TOPICOUT)
                .addResponse("response").addUserSays("usersays")
                .addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                        3, "somevalue", false, "label", false).addPrompt("prompt"));
    }

    @Before
    public void setup() throws DatabaseException {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeDatabaseTransactionProvider = mock(Provider.class);
        this.fakeDatabase = mock(DatabaseAI.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.fakeCsvIntentReader = mock(CsvIntentReader.class);
        this.intentLogic = new IntentLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabaseEntitiesIntents,
                this.fakeDatabase, this.trainingLogic, mock(JsonSerializer.class), this.fakeDatabaseTransactionProvider,
                this.fakeCsvIntentReader, mock(DatabaseUser.class));

        when(this.fakeConfig.getMaxUploadSizeKb()).thenReturn(1000);
        when(this.fakeDatabaseTransactionProvider.get()).thenReturn(this.fakeDatabaseTransaction);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
    }

    @Test
    public void testGetIntents_Success() throws DatabaseException {
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getIntentsDetails(any(), any())).thenReturn(getIntentsDetailsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Success_Return() throws DatabaseException {
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getIntentsDetails(any(), any())).thenReturn(getIntentsDetailsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(2, ((ApiIntentList) result).getIntentNames().size());
        Assert.assertEquals(INTENTNAME, ((ApiIntentList) result).getIntentNames().get(0));
        Assert.assertEquals(INTENTNAME, ((ApiIntentList) result).getIntents().get(0).getIntentName());
    }

    @Test
    public void testGetIntents_NotFound() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(new ArrayList<>());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Error() throws DatabaseException {
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenThrow(DatabaseException.class);
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(getIntent());
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_SuccessWithWebHook() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        ApiIntent intent = getIntent();
        intent.setWebHook(wh);
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(intent);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success_Return() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(getIntent());
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(INTENTNAME, ((ApiIntent) result).getIntentName());
    }

    @Test
    public void testGetIntent_NotFound() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(null);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Error() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenThrow(DatabaseException.class);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Aiid_Invalid() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenThrow(DatabaseException.class);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Success() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(null);
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_AlreadyExisting() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(getIntent());
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Update_Success() throws DatabaseException {
        ApiIntent intent = getIntent();
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(intent);
        final ApiResult result = this.intentLogic.updateIntent(DEVID_UUID, AIID, intent, intent.getIntentName());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Update_NonExisting() throws DatabaseException {
        ApiIntent intent = getIntent();
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), anyString())).thenReturn(null);
        final ApiResult result = this.intentLogic.updateIntent(DEVID_UUID, AIID, intent, intent.getIntentName());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_WebHookWritten() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        ApiIntent intent = getIntent();
        intent.setWebHook(wh);
        when(this.fakeDatabase.createWebHook(any(), anyString(), anyString(), anyBoolean(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, intent);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_NonExistentEntity() throws DatabaseException {
        doThrow(DatabaseEntitiesIntents.DatabaseEntityException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_DuplicateName() throws DatabaseException {
        doThrow(DatabaseIntegrityViolationException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_API_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabase);
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_InternalError() throws DatabaseException {
        doThrow(DatabaseException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Success() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenReturn(true);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Error() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenThrow(new DatabaseException(new Exception("test")));
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_NotFound() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenReturn(false);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabase);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_triggersTrainingStop() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenReturn(true);
        this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testUpdateIntent_triggersTrainingStop() {
        this.intentLogic.createIntent(DEVID_UUID, AIID, getIntent());
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testCreateIntent_autoCreateFollowUpIntents() throws DatabaseException {
        final String followUpIntentName = "followUp";
        final String mainIntentName = "main";
        ApiIntent baseIntent = getIntent();
        baseIntent.setIntentName(mainIntentName);
        baseIntent.setIntentOutConditionals(Collections.singletonList(new IntentOutConditional(followUpIntentName, null)));

        ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, baseIntent);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
        // Capture the arguments being passed to the db class to validate if we're writing the correct data
        ArgumentCaptor<ApiIntent> intentArg = ArgumentCaptor.forClass(ApiIntent.class);
        ArgumentCaptor<String> intentNameArg = ArgumentCaptor.forClass(String.class);
        verify(this.fakeDatabaseEntitiesIntents, atLeastOnce()).writeIntent(any(), any(), intentNameArg.capture(), intentArg.capture(), any());
        List<String> actualIntentNames = intentNameArg.getAllValues();
        List<ApiIntent> actualIntents = intentArg.getAllValues();

        // Check the followup intent is written to the db
        Assert.assertEquals(followUpIntentName, actualIntentNames.get(0));
        Assert.assertEquals(followUpIntentName, actualIntents.get(0).getIntentName());
        // The followup intent should have a new condition that is gated on the main intent
        Assert.assertEquals(1, actualIntents.get(0).getConditionsIn().size());
        Assert.assertEquals(getFollowupIntentGatedVariable(mainIntentName), actualIntents.get(0).getConditionsIn().get(0).getVariable());
        Assert.assertEquals(IntentConditionOperator.SET, actualIntents.get(0).getConditionsIn().get(0).getOperator());

        // Then the main intent
        Assert.assertEquals(mainIntentName, actualIntentNames.get(1));
        Assert.assertEquals(mainIntentName, actualIntents.get(1).getIntentName());
        Assert.assertEquals(baseIntent.getIntentOutConditionals(), actualIntents.get(1).getIntentOutConditionals());
        Assert.assertTrue(actualIntents.get(1).getContextOut().containsKey(getFollowupIntentGatedVariable(mainIntentName)));
    }

    @Test
    public void testCreateIntent_autoCreateFollowUpIntents_existingFollowupIntent() throws DatabaseException {
        final String followUpIntentName = "followUp";
        final String mainIntentName = "main";
        ApiIntent baseIntent = getIntent();
        baseIntent.setIntentName(mainIntentName);
        baseIntent.setIntentOutConditionals(Collections.singletonList(new IntentOutConditional(followUpIntentName, null)));

        // Pretend the the followup intent already exists - just needs to return non-null
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any(), any())).thenReturn(getIntent());

        ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, baseIntent);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
        // Capture the arguments being passed to the db class to validate if we're writing the correct data
        ArgumentCaptor<ApiIntent> intentArg = ArgumentCaptor.forClass(ApiIntent.class);
        ArgumentCaptor<String> intentNameArg = ArgumentCaptor.forClass(String.class);
        verify(this.fakeDatabaseEntitiesIntents, atLeastOnce()).writeIntent(any(), any(), intentNameArg.capture(), intentArg.capture(), any());
        List<String> actualIntentNames = intentNameArg.getAllValues();
        List<ApiIntent> actualIntents = intentArg.getAllValues();

        Assert.assertEquals(1, actualIntentNames.size());
        Assert.assertEquals(1, actualIntents.size());
        Assert.assertEquals(mainIntentName, actualIntentNames.get(0));
        Assert.assertEquals(mainIntentName, actualIntents.get(0).getIntentName());
    }

    @Test
    public void testCreateIntent_autoCreateFollowUpIntents_followupSameNameAsMain() throws DatabaseException {
        final String mainIntentName = "main";
        ApiIntent baseIntent = getIntent();
        baseIntent.setIntentName(mainIntentName);
        // Force a circular reference
        baseIntent.setIntentOutConditionals(Collections.singletonList(new IntentOutConditional(mainIntentName, null)));
        ApiResult result = this.intentLogic.createIntent(DEVID_UUID, AIID, baseIntent);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testCsvBulkImport() {
        final int numIntents = 5;
        List<ApiIntent> intents = buildIntentList(numIntents);
        ApiCsvImportResult results = buildCsvImportResult(intents);
        when(this.fakeCsvIntentReader.parseIntents(anyString())).thenReturn(results);
        ApiCsvImportResult result = (ApiCsvImportResult) this.intentLogic.bulkImportFromCsv(DEVID_UUID, AIID, createUpload(generateIntentsCsv(intents)));
        validateIntentsReturned(intents, result);
    }

    @Test
    public void testCsvBulkImport_duplicateIntentNames() {
        final int numIntents = 3;
        List<ApiIntent> intents = buildIntentList(numIntents);
        intents.get(1).setIntentName("myintent");
        intents.get(2).setIntentName("myintent");
        ApiCsvImportResult results = buildCsvImportResult(intents);
        when(this.fakeCsvIntentReader.parseIntents(anyString())).thenReturn(results);
        ApiResult result = this.intentLogic.bulkImportFromCsv(DEVID_UUID, AIID, createUpload(generateIntentsCsv(intents)));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testCsvBulkImport_dbException() throws DatabaseException {
        List<ApiIntent> intents = buildIntentList(1);
        ApiCsvImportResult results = buildCsvImportResult(intents);
        when(this.fakeCsvIntentReader.parseIntents(anyString())).thenReturn(results);
        doThrow(DatabaseException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), any(), any(), any());
        ApiResult result = this.intentLogic.bulkImportFromCsv(DEVID_UUID, AIID, createUpload(generateIntentsCsv(intents)));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testCsvBulkImport_added_updated() throws DatabaseException {
        List<ApiIntent> intents = buildIntentList(2);
        ApiCsvImportResult results = buildCsvImportResult(intents);
        when(this.fakeCsvIntentReader.parseIntents(anyString())).thenReturn(results);
        // pretend that the first time is an update and the second is an insert
        when(this.fakeDatabaseEntitiesIntents.writeIntent(any(), any(), any(), any(), any())).thenReturn(2).thenReturn(1);
        ApiCsvImportResult result = (ApiCsvImportResult) this.intentLogic.bulkImportFromCsv(DEVID_UUID, AIID, createUpload(generateIntentsCsv(intents)));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals("intent0", result.getImported().get(0).getIntentName());
        Assert.assertEquals("updated", result.getImported().get(0).getAction());
        Assert.assertEquals("intent1", result.getImported().get(1).getIntentName());
        Assert.assertEquals("added", result.getImported().get(1).getAction());
    }

    @Test
    public void testSaveIntent_entityValueLifetime_cannotBeCreatedWith0Turns() {
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.getVariables().get(0).setLifetimeTurns(0);
        this.intentLogic.createIntent(DEVID_UUID, AIID, intent);
        // When it's attempted to be created with 0 turns we change it to -1 (no lifetime)
        Assert.assertEquals(-1, intent.getVariables().get(0).getLifetimeTurns());
    }

    private void validateIntentsReturned(final List<ApiIntent> expected, final ApiCsvImportResult actual) {
        Assert.assertEquals(HttpURLConnection.HTTP_OK, actual.getStatus().getCode());
        Assert.assertEquals(expected.size(), actual.getImported().size());
        for (int i = 0; i < expected.size(); i++) {
            ApiIntent intent = actual.getImported().get(i).getIntent();
            Assert.assertEquals("intent" + i, intent.getIntentName());
            Assert.assertEquals("intent" + i, actual.getImported().get(i).getIntentName());
            assertListEquals(expected.get(i).getUserSays(), intent.getUserSays());
            assertListEquals(expected.get(i).getResponses(), intent.getResponses());
        }
    }

    private ApiCsvImportResult buildCsvImportResult(final List<ApiIntent> imported) {
        ApiCsvImportResult result = new ApiCsvImportResult();
        imported.forEach(result::addImported);
        return result;
    }

    private List<ApiIntent> buildIntentList(final int numIntents) {
        List<ApiIntent> intents = new ArrayList<>();
        for (int i = 0; i < numIntents; i++) {
            ApiIntent intent = new ApiIntent("intent" + i, "", "");
            intent.addUserSays("us0_" + i);
            intent.addUserSays("us1_" + i);
            intent.addResponse("r0_" + i);
            intent.addResponse("r1_" + i);
            intents.add(intent);
        }
        return intents;
    }

    private List<String> getIntentsList() {
        return Arrays.asList(INTENTNAME, "intent2");
    }

    private ApiIntentList getIntentsDetailsList() {
        ApiIntent intent = new ApiIntent(INTENTNAME, "topicIn", "topicOut");
        return new ApiIntentList(AIID, getIntentsList(), Collections.singletonList(intent));
    }

    private void assertListEquals(final Collection collectionA, final Collection collectionB) {
        Collection diff = CollectionUtils.disjunction(collectionA, collectionB);
        if (!diff.isEmpty()) {
            Assert.fail(String.format("Collections are different on the following objects: %s",
                    diff.stream().map(Object::toString).collect(Collectors.joining(", "))));
        }
    }

    private String generateIntentsCsv(final List<ApiIntent> intents) {
        StringBuilder sb = new StringBuilder();
        for (ApiIntent intent : intents) {
            sb.append(intent.getIntentName()).append(",");
            sb.append(StringUtils.join(intent.getUserSays(), ";"));
            sb.append(",");
            sb.append(StringUtils.join(intent.getResponses(), ";"));
            sb.append("\n");
        }
        return sb.toString();
    }

    private InputStream createUpload(final String content) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return stream;
    }

    private String getFollowupIntentGatedVariable(final String mainIntentName) {
        return String.format("%s_complete", mainIntentName);
    }
}
