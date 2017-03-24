package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiWebHook;
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
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 10/10/2016.
 */
public class TestIntentLogic {

    private final String INTENTNAME = "intent";
    private final String TOPICIN = "topicin";
    private final String TOPICOUT = "topicout";
    DatabaseEntitiesIntents fakeDatabase;
    Config fakeConfig;
    IntentLogic intentLogic;
    ILogger fakeLogger;
    TrainingLogic trainingLogic;

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
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(DEVID, AIID);
        Assert.assertEquals(2, ((ApiIntentList) result).getIntentNames().size());
        Assert.assertEquals(this.INTENTNAME, ((ApiIntentList) result).getIntentNames().get(0));
    }

    @Test
    public void testGetIntents_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(new ArrayList<>());
        final ApiResult result = this.intentLogic.getIntents(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.intentLogic.getIntents(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(getIntent());
        final ApiResult result = this.intentLogic.getIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(getIntent());
        final ApiResult result = this.intentLogic.getIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(this.INTENTNAME, ((ApiIntent) result).getIntentName());
    }

    @Test
    public void testGetIntent_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(null);
        final ApiResult result = this.intentLogic.getIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.intentLogic.getIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_Success() throws Database.DatabaseException {
        final ApiResult result = this.intentLogic.writeIntent(DEVID, AIID, this.getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_NonExistentEntity() throws Database.DatabaseException {
        doThrow(new DatabaseEntitiesIntents.DatabaseEntityException("test")).when(this.fakeDatabase).writeIntent(anyString(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID, AIID, this.getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_DuplicateName() throws Database.DatabaseException {
        doThrow(new Database.DatabaseIntegrityViolationException(new Exception("test"))).when(this.fakeDatabase).writeIntent(anyString(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID, AIID, this.getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testWriteIntent_InternalError() throws Database.DatabaseException {
        doThrow(new Database.DatabaseException("test")).when(this.fakeDatabase).writeIntent(anyString(), any(), anyString(), any());
        final ApiResult result = this.intentLogic.writeIntent(DEVID, AIID, this.getIntent());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(anyString(), any(), anyString())).thenReturn(true);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(anyString(), any(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.intentLogic.deleteIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(anyString(), any(), anyString())).thenReturn(false);
        final ApiResult result = this.intentLogic.deleteIntent(DEVID, AIID, this.INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDeleteIntent_triggersTrainingStop() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteIntent(anyString(), any(), anyString())).thenReturn(true);
        this.intentLogic.deleteIntent(DEVID, AIID, this.INTENTNAME);
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testUpdateIntent_triggersTrainingStop() throws Database.DatabaseException {
        this.intentLogic.writeIntent(DEVID, AIID, getIntent());
        verify(this.trainingLogic).stopTraining(any(), any());
    }

    @Test
    public void testIntentWebhook_createWebhook() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);

        when(this.fakeDatabase.createWebHook(AIID, this.INTENTNAME, wh.getEndpoint(), wh.getEnabled())).thenReturn(true);

        final ApiResult result = this.intentLogic.updateWebHook(DEVID, AIID, this.INTENTNAME, wh);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testIntentWebhook_getWebhook() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), INTENTNAME, "https://fakewebhookaddress/webhook", true);
        when(this.fakeDatabase.getWebHook(AIID, INTENTNAME)).thenReturn(wh);

        final ApiResult result = this.intentLogic.getWebHook(DEVID, AIID, INTENTNAME);
        Assert.assertEquals(this.INTENTNAME, ((ApiWebHook) result).getWebHook().getIntentName());
    }

    @Test
    public void testIntentWebhook_getWebHookNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getWebHook(AIID, INTENTNAME)).thenReturn(null);

        final ApiResult result = this.intentLogic.getWebHook(DEVID, AIID, INTENTNAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testIntentWebhook_updateWebhook() throws Database.DatabaseException {
        WebHook wh = new WebHook(UUID.randomUUID(), "testName", "https://fakewebhookaddress/webhook", true);

        when(this.fakeDatabase.createWebHook(AIID, this.INTENTNAME, wh.getEndpoint(), wh.getEnabled())).thenReturn(true);
        when(this.fakeDatabase.updateWebHook(AIID, this.INTENTNAME, wh.getEndpoint(), wh.getEnabled())).thenReturn(true);

        ApiResult result = this.intentLogic.updateWebHook(DEVID, AIID, this.INTENTNAME, wh);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());

        // Update the webhook.
        wh = new WebHook(wh.getAiid(), wh.getIntentName(), wh.getEndpoint(), false);
        result = this.intentLogic.updateWebHook(DEVID, AIID, this.INTENTNAME, wh);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    private List<String> getIntentsList() {
        return Arrays.asList(this.INTENTNAME, "intent2");
    }

    private ApiIntent getIntent() {
        return new ApiIntent(this.INTENTNAME, this.TOPICIN, this.TOPICOUT)
                .addResponse("response").addUserSays("usersays")
                .addVariable(new IntentVariable("entity", true, 3, "somevalue").addPrompt("prompt"));
    }
}
