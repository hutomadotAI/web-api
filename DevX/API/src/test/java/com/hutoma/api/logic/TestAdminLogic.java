package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiResult;
import hutoma.api.server.ai.api_root;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 02/08/2016.
 */
public class TestAdminLogic {

    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Logger fakeLogger;

    private String DEVID = "devid";
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String VALIDDEVID = "DevidExists";

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeLogger = mock(Logger.class);
    }

    @Test
    public void testCreate_Valid() {
        validKeyDBSuccess();
        Assert.assertEquals(200, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Devid() {
        validKeyDBSuccess();
        Assert.assertEquals(DEVID, ((ApiAdmin)createDev()).getDevid());
    }

    @Test
    public void testCreate_Valid_Token() {
        validKeyDBSuccess();
        Assert.assertNotNull(((ApiAdmin)createDev()).getDev_token());
    }

    private void validKeyDBSuccess() {
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
    }

    @Test
    public void testCreate_KeyNull() {
        when(fakeConfig.getEncodingKey()).thenReturn(null);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(500, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_InvalidKey() {
        when(fakeConfig.getEncodingKey()).thenReturn("[]");
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(500, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyDBFail() {
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(false);
        Assert.assertEquals(500, createDev().getStatus().getCode());
    }

    private ApiResult createDev() {
        AdminLogic adminLogic = new AdminLogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue, fakeLogger);
        ApiResult result = adminLogic.createDev(fakeContext, "ROLE", DEVID, "username", "email", "password", "passSalt", "name", "attempt", "devToken", 0, "devID");
        return result;
    }

    @Test
    public void testDelete_Success() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(true);
        Assert.assertEquals(200, deleteDev(VALIDDEVID).getStatus().getCode());
    }

    @Test
    public void testDelete_NullDevid() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(false);
        Assert.assertEquals(500, deleteDev(null).getStatus().getCode());
    }

    @Test
    public void testDelete_NonExistentDevid() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(false);
        Assert.assertEquals(400, deleteDev("other").getStatus().getCode());
    }

    private ApiResult deleteDev(String devid) {
        AdminLogic adminLogic = new AdminLogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue, fakeLogger);
        return adminLogic.deleteDev(fakeContext, devid);
    }
}
