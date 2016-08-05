package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

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
    }

    @Test
    public void testCreate_Valid() {
        validKeyDBSuccess();
        Assert.assertEquals(200, createDev().status.code);
    }

    @Test
    public void testCreate_Valid_Devid() {
        validKeyDBSuccess();
        Assert.assertEquals(DEVID, createDev().devid);
    }

    @Test
    public void testCreate_Valid_Token() {
        validKeyDBSuccess();
        Assert.assertNotNull(createDev().dev_token);
    }

    private void validKeyDBSuccess() {
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
    }

    @Test
    public void testCreate_KeyNull() {
        when(fakeConfig.getEncodingKey()).thenReturn(null);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(500, createDev().status.code);
    }

    @Test
    public void testCreate_InvalidKey() {
        when(fakeConfig.getEncodingKey()).thenReturn("[]");
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(500, createDev().status.code);
    }

    @Test
    public void testCreate_ValidKeyDBFail() {
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(false);
        Assert.assertEquals(500, createDev().status.code);
    }

    private api_root._myAIs createDev() {
        AdminLogic adminLogic = new AdminLogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue);
        adminLogic.createDev(fakeContext, "ROLE", DEVID, "username", "email", "password", "passSalt", "name", "attempt", "devToken", 0, "devID");

        // get the unserialized result
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        return apiRoot;
    }

    @Test
    public void testDelete_Success() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(true);
        when(fakeMessageQueue.pushMessageDeleteDev(any(), anyString())).thenReturn(true);
        Assert.assertEquals(200, deleteDev(VALIDDEVID).status.code);
    }

    @Test
    public void testDelete_NullDevid() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(false);
        Assert.assertEquals(500, deleteDev(null).status.code);
    }

    @Test
    public void testDelete_NonExistentDevid() {
        when(fakeDatabase.deleteDev(anyString())).thenReturn(false);
        Assert.assertEquals(404, deleteDev("other").status.code);
    }

    private api_root._newai deleteDev(String devid) {
        AdminLogic adminLogic = new AdminLogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue);
        adminLogic.deleteDev(fakeContext, devid);

        // get the unserialized result
        api_root._newai apiRoot = ((api_root._newai)fakeSerializer.getUnserialized());
        return apiRoot;
    }
}