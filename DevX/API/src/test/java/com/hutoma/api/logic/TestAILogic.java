package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.validation.TestParameterValidation;
import com.hutoma.api.validation.Validate;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 04/08/2016.
 */
public class TestAILogic {

    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Tools fakeTools;
    AILogic aiLogic;
    Logger fakeLogger;

    private String DEVID = "devid";
    private UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String VALIDDEVID = "DevidExists";

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(Logger.class);

        when(fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        aiLogic = new AILogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue, fakeLogger, fakeTools);
    }

    private ApiAi getAI() {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false, 0.0d, null, "status", "status", null);
    }

    private ArrayList<ApiAi> getAIList() {
        ArrayList<ApiAi> returnList = new ArrayList<>();
        returnList.add(getAI());
        return returnList;
    }

    @Test
    public void testCreate_Valid() throws Database.DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(true);
        ApiResult result = aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws Database.DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(true);
        ApiResult result = aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        Assert.assertTrue(result instanceof ApiAi);
        Assert.assertNotNull(((ApiAi)result).getClient_token());
        Assert.assertFalse(((ApiAi)result).getClient_token().isEmpty());
    }

    @Test
    public void testCreate_DBFail_Error() throws Database.DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testCreate_DB_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(false);
        ApiResult result = aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any())).thenReturn(getAI());
        ApiResult result = aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any())).thenReturn(getAI());
        ApiAi result = (ApiAi)aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(AIID.toString(), result.getAiid());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any())).thenThrow(new Database.DatabaseException(new Exception("")));
        ApiResult result = aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.getAI(any())).thenReturn(null);
        ApiResult result = aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(returnList);
        ApiResult result = aiLogic.getAIs(fakeContext, VALIDDEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(returnList);
        ApiResult result = aiLogic.getAIs(fakeContext, VALIDDEVID);
        Assert.assertTrue(result instanceof ApiAiList);
        ApiAiList list = (ApiAiList)result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertFalse(list.getAiList().isEmpty());
        Assert.assertEquals(AIID.toString(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAll_NoneFound() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(new ArrayList<ApiAi>());
        ApiResult result = aiLogic.getAIs(fakeContext, VALIDDEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(fakeDatabase.getAllAIs(anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        aiLogic.getAIs(fakeContext, VALIDDEVID);
        ApiResult result = aiLogic.getAIs(fakeContext, VALIDDEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws Database.DatabaseException {
        when(fakeDatabase.deleteAi(any())).thenReturn(true);
        ApiResult result = aiLogic.deleteAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.deleteAi(any())).thenReturn(false);
        ApiResult result = aiLogic.deleteAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_Error() throws Database.DatabaseException {
        when(fakeDatabase.deleteAi(any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = aiLogic.deleteAI(fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }
}

