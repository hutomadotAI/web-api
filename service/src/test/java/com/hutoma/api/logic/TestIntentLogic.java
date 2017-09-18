package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 10/10/2016.
 */
public class TestIntentLogic {

    public static final String INTENTNAME = "intent";
    public static final String TOPICIN = "topicin";
    public static final String TOPICOUT = "topicout";
    DatabaseEntitiesIntents fakeDatabase;
    Config fakeConfig;
    IntentLogic intentLogic;
    ILogger fakeLogger;
    TrainingLogic trainingLogic;

    public static ApiIntent getIntent() {
        return new ApiIntent(INTENTNAME, TOPICIN, TOPICOUT)
                .addResponse("response").addUserSays("usersays")
                .addVariable(new IntentVariable("entity", UUID.randomUUID(), true,
                        3, "somevalue", false, "label").addPrompt("prompt"));
    }

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.intentLogic = new IntentLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase, this.trainingLogic);
    }

    @Test
    public void testGetIntents_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(any(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(any(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(2, ((ApiIntentList) result).getIntentNames().size());
        Assert.assertEquals(INTENTNAME, ((ApiIntentList) result).getIntentNames().get(0));
    }

    @Test
    public void testGetIntents_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(any(), any())).thenReturn(new ArrayList<>());
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(any(), any())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.intentLogic.getIntents(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(any(), anyString())).thenReturn(getIntent());
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_SuccessWithWebHook() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        ApiIntent intent = getIntent();
        intent.setWebHook(wh);
        when(this.fakeDatabase.getIntent(any(), anyString())).thenReturn(intent);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(any(), anyString())).thenReturn(getIntent());
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(INTENTNAME, ((ApiIntent) result).getIntentName());
    }

    @Test
    public void testGetIntent_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(any(), anyString())).thenReturn(null);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(any(), anyString())).thenThrow(Database.DatabaseException.class);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Aiid_Invalid() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(any(), anyString())).thenThrow(Database.DatabaseException.class);
        when(this.fakeDatabase.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        final ApiResult result = this.intentLogic.getIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Success() throws Database.DatabaseException {
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_WebHookWritten() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        ApiIntent intent = getIntent();
        intent.setWebHook(wh);
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, intent);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_NonExistentEntity() throws Database.DatabaseException {
        doThrow(new DatabaseEntitiesIntents.DatabaseEntityException("test")).when(this.fakeDatabase).writeIntent(any(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_DuplicateName() throws Database.DatabaseException {
        doThrow(new Database.DatabaseIntegrityViolationException(new Exception("test"))).when(this.fakeDatabase).writeIntent(any(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_InternalError() throws Database.DatabaseException {
        doThrow(new Database.DatabaseException("test")).when(this.fakeDatabase).writeIntent(any(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(any(), any(), anyString())).thenReturn(true);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(any(), any(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(any(), any(), anyString())).thenReturn(false);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_WebHookDeleted() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhook", true);
        when(this.fakeDatabase.deleteIntent(any(), any(), anyString())).thenReturn(true);
        when(this.fakeDatabase.getWebHook(any(), any())).thenReturn(wh);

        this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        verify(this.fakeDatabase).deleteWebHook(any(), any());
    }

    @Test
    public void testDeleteIntent_triggersTrainingStop() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(any(), any(), anyString())).thenReturn(true);
        this.intentLogic.deleteIntent(DEVID_UUID, AIID, INTENTNAME);
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testUpdateIntent_triggersTrainingStop() throws Database.DatabaseException {
        this.intentLogic.writeIntent(DEVID_UUID, AIID, getIntent());
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    private List<String> getIntentsList() {
        return Arrays.asList(INTENTNAME, "intent2");
    }
}
