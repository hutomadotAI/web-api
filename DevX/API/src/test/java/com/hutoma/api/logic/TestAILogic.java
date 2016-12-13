package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 04/08/2016.
 */
public class TestAILogic {

    private final String DEVID = "devid";
    private final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private final String VALIDDEVID = "DevidExists";
    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    AIServices fakeAiServices;
    Config fakeConfig;
    Tools fakeTools;
    AILogic aiLogic;
    ILogger fakeLogger;

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(this.VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(ILogger.class);

        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.aiLogic = new AILogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeAiServices,
                this.fakeLogger, this.fakeTools);
    }

    @Test
    public void testCreate_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(), any(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenReturn(this.AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(this.AIID);
        ApiResult result = this.aiLogic.createAI(this.fakeContext, this.DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws Database.DatabaseException {
        whenCreateAiReturn(this.AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(this.AIID);
        ApiResult result = this.aiLogic.createAI(this.fakeContext, this.DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertTrue(result instanceof ApiAi);
        Assert.assertNotNull(((ApiAi) result).getClient_token());
        Assert.assertFalse(((ApiAi) result).getClient_token().isEmpty());
    }

    @Test
    public void testCreate_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(), any(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = this.aiLogic.createAI(this.fakeContext, this.DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testCreate_DB_NameClash() throws Database.DatabaseException {
        whenCreateAiReturn(UUID.randomUUID());
        ApiResult result = this.aiLogic.createAI(this.fakeContext, this.DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(getAI());
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(getAI());
        ApiAi result = (ApiAi) this.aiLogic.getSingleAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(this.AIID.toString(), result.getAiid());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("")));
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(this.VALIDDEVID)).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, this.VALIDDEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(this.VALIDDEVID)).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, this.VALIDDEVID);
        Assert.assertTrue(result instanceof ApiAiList);
        ApiAiList list = (ApiAiList) result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertFalse(list.getAiList().isEmpty());
        Assert.assertEquals(this.AIID.toString(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAll_NoneFound() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(this.VALIDDEVID)).thenReturn(new ArrayList<ApiAi>());
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, this.VALIDDEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        this.aiLogic.getAIs(this.fakeContext, this.VALIDDEVID);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, this.VALIDDEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, this.DEVID, this.AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbFail() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, this.DEVID, this.AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, this.DEVID, this.AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, this.VALIDDEVID, this.AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus() throws Database.DatabaseException {
        AiStatus status = new AiStatus(this.DEVID, this.AIID, TrainingStatus.NOT_STARTED);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_false() throws Database.DatabaseException {
        AiStatus status = new AiStatus(this.DEVID, this.AIID, TrainingStatus.NOT_STARTED);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws Database.DatabaseException {
        AiStatus status = new AiStatus(this.DEVID, this.AIID, TrainingStatus.NOT_STARTED);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void whenCreateAiReturn(UUID aiid) throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(), any(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenReturn(aiid);
    }

    private ApiAi getAI() {
        return new ApiAi(this.AIID.toString(), "token", "name", "desc", DateTime.now(), false, 0.5, "debuginfo",
                "trainstatus", null, "", 0, 0.0, 1, Locale.getDefault(),
                "UTC");
    }

    private ArrayList<ApiAi> getAIList() {
        ArrayList<ApiAi> returnList = new ArrayList<>();
        returnList.add(getAI());
        return returnList;
    }
}

