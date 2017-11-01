package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.logging.ILogger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

    public static ApiIntent getIntent() {
        return new ApiIntent(INTENTNAME, TOPICIN, TOPICOUT)
                .addResponse("response").addUserSays("usersays")
                .addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                        3, "somevalue", false, "label").addPrompt("prompt"));
    }

    @Before
    public void setup() throws DatabaseException {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeDatabaseTransactionProvider = mock (Provider.class);
        this.fakeDatabase = mock(DatabaseAI.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.intentLogic = new IntentLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabaseEntitiesIntents,
                this.fakeDatabase, this.trainingLogic, mock(JsonSerializer.class), this.fakeDatabaseTransactionProvider);

        when(this.fakeDatabaseTransactionProvider.get()).thenReturn(this.fakeDatabaseTransaction);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
    }

    @Test
    public void testGetIntents_Success() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Success_Return() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(2, ((ApiIntentList) result).getIntentNames().size());
        Assert.assertEquals(INTENTNAME, ((ApiIntentList) result).getIntentNames().get(0));
    }

    @Test
    public void testGetIntents_NotFound() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(new ArrayList<>());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Error() throws DatabaseException {
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
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Success() throws DatabaseException {
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_WebHookWritten() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        ApiIntent intent = getIntent();
        intent.setWebHook(wh);
        when(this.fakeDatabase.createWebHook(any(), anyString(), anyString(), anyBoolean(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, intent);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_NonExistentEntity() throws DatabaseException {
        doThrow(DatabaseEntitiesIntents.DatabaseEntityException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_DuplicateName() throws DatabaseException {
        doThrow(DatabaseIntegrityViolationException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_API_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabase);
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_InternalError() throws DatabaseException {
        doThrow(DatabaseException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
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
    public void testDeleteIntent_WebHookDeleted() throws DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.getWebHook(any(), any())).thenReturn(wh);

        this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        verify(this.fakeDatabaseEntitiesIntents).deleteWebHook(any(), any());
    }

    @Test
    public void testDeleteIntent_triggersTrainingStop() throws DatabaseException {
        when(this.fakeDatabaseEntitiesIntents.deleteIntent(any(), any(), anyString())).thenReturn(true);
        this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testUpdateIntent_triggersTrainingStop() throws DatabaseException {
        this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    private List<String> getIntentsList() {
        return Arrays.asList(INTENTNAME, "intent2");
    }
}
