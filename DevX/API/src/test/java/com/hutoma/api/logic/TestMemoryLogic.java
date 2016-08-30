package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiMemory;
import com.hutoma.api.containers.ApiMemoryToken;
import com.hutoma.api.containers.ApiResult;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 19/08/2016.
 */
public class TestMemoryLogic {

    //http://mockito.org/
    SecurityContext fakeContext;
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Tools fakeTools;
    MemoryLogic memoryLogic;
    Logger fakeLogger;

    private String DEVID = "devid";
    private String AIID = "aiid";
    private String UID = "uid";
    private String VARNAME = "varname";

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        memoryLogic = new MemoryLogic(fakeConfig, fakeDatabase, fakeTools, fakeLogger);
    }

    List<ApiMemoryToken> getApiMemoryTokenList() {
        List<ApiMemoryToken> tokens = new ArrayList<>();
        tokens.add(getMemoryToken());
        return tokens;
    }

    ApiMemoryToken getMemoryToken() {
        return new ApiMemoryToken(VARNAME, "varval", "vartype", DateTime.now(), 100, 0);
    }

    @Test
    public void testGetVars_Success() throws Database.DatabaseException {
        when(fakeDatabase.getAllUserVariables(anyString(), anyString(), anyString())).thenReturn(getApiMemoryTokenList());
        ApiResult result = memoryLogic.getVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetVars_Success_Content() throws Database.DatabaseException {
        when(fakeDatabase.getAllUserVariables(anyString(), anyString(), anyString())).thenReturn(getApiMemoryTokenList());
        ApiResult result = memoryLogic.getVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertTrue(result instanceof ApiMemory);
        Assert.assertEquals(VARNAME, ((ApiMemory)result).getMemoryList().get(0).getVariableName());
    }

    @Test
    public void testGetVars_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.getAllUserVariables(anyString(), anyString(), anyString())).thenReturn(new ArrayList<ApiMemoryToken>());
        ApiResult result = memoryLogic.getVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetVars_DBError() throws Database.DatabaseException {
        when(fakeDatabase.getAllUserVariables(anyString(), anyString(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.getVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingleVar_Success() throws Database.DatabaseException {
        when(fakeDatabase.getUserVariable(anyString(), anyString(), anyString(), anyString())).thenReturn(getMemoryToken());
        ApiResult result = memoryLogic.getSingleVariable(fakeContext, DEVID, AIID, UID, "some");
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetSingleVar_Success_Content() throws Database.DatabaseException {
        when(fakeDatabase.getUserVariable(anyString(), anyString(), anyString(), anyString())).thenReturn(getMemoryToken());
        ApiResult result = memoryLogic.getSingleVariable(fakeContext, DEVID, AIID, UID, "some");
        Assert.assertTrue(result instanceof ApiMemoryToken);
        Assert.assertEquals(VARNAME, ((ApiMemoryToken)result).getVariableName());
    }

    @Test
    public void testGetSingleVar_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.getUserVariable(anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        ApiResult result = memoryLogic.getSingleVariable(fakeContext, DEVID, AIID, UID, "some");
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetSingleVar_DBError() throws Database.DatabaseException {
        when(fakeDatabase.getUserVariable(anyString(), anyString(), anyString(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.getSingleVariable(fakeContext, DEVID, AIID, UID, "some");
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testSetVar_Success() throws Database.DatabaseException {
        when(fakeDatabase.setUserVariable(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(true);
        ApiResult result = memoryLogic.setVariable(fakeContext, DEVID, AIID, UID, VARNAME, "varval", 0, 0, "label");
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testSetVar_Failed() throws Database.DatabaseException {
        when(fakeDatabase.setUserVariable(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(false);
        ApiResult result = memoryLogic.setVariable(fakeContext, DEVID, AIID, UID, VARNAME, "varval", 0, 0, "label");
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testSetVar_DBFail() throws Database.DatabaseException {
        when(fakeDatabase.setUserVariable(anyString(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.setVariable(fakeContext, DEVID, AIID, UID, VARNAME, "varval", 0, 0, "label");
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelVar_Success() throws Database.DatabaseException {
        when(fakeDatabase.removeVariable(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        ApiResult result = memoryLogic.delVariable(fakeContext, DEVID, AIID, UID, VARNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDelVar_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.removeVariable(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
        ApiResult result = memoryLogic.delVariable(fakeContext, DEVID, AIID, UID, VARNAME);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testDelVar_DBFail() throws Database.DatabaseException {
        when(fakeDatabase.removeVariable(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.delVariable(fakeContext, DEVID, AIID, UID, VARNAME);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAll_Success() throws Database.DatabaseException {
        when(fakeDatabase.removeAllUserVariables(anyString(), anyString(), anyString())).thenReturn(true);
        ApiResult result = memoryLogic.removeAllUserVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAll_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.removeAllUserVariables(anyString(), anyString(), anyString())).thenReturn(false);
        ApiResult result = memoryLogic.removeAllUserVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAll_DBFail() throws Database.DatabaseException {
        when(fakeDatabase.removeAllUserVariables(anyString(), anyString(), anyString()))
                .thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.removeAllUserVariables(fakeContext, DEVID, AIID, UID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAi_Success() throws Database.DatabaseException {
        when(fakeDatabase.removeAllAiVariables(anyString(), anyString())).thenReturn(true);
        ApiResult result = memoryLogic.removeAllAiVariables(fakeContext, DEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAi_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.removeAllAiVariables(anyString(), anyString())).thenReturn(false);
        ApiResult result = memoryLogic.removeAllAiVariables(fakeContext, DEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testRemoveAi_DBFail() throws Database.DatabaseException {
        when(fakeDatabase.removeAllAiVariables(anyString(), anyString()))
                .thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = memoryLogic.removeAllAiVariables(fakeContext, DEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

}
