package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 02/08/2016.
 */
public class TestAdminLogic {

    private static final String DEVID = "devid";
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String VALIDDEVID = "DevidExists";
    private static final String DEVTOKEN = "wieqejqwkjeqwejqlkejqwejwldslkfhslkdhflkshflskfh-sdfjdf";

    private FakeJsonSerializer fakeSerializer;
    private SecurityContext fakeContext;
    private Database fakeDatabase;
    private AIServices fakeAiServices;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private AdminLogic adminLogic;

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeLogger = mock(ILogger.class);
        this.adminLogic = new AdminLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeLogger,
                this.fakeAiServices);
    }

    @Test
    public void testCreate_Valid() throws Database.DatabaseException {
        validKeyDBSuccess();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, createDev().getStatus().getCode());
    }

//    @Test
//    public void testCreate_Valid_Devid() throws Database.DatabaseException {
//        validKeyDBSuccess();
//        Assert.assertEquals(DEVID, ((ApiAdmin)createDev()).getDevid());
//    }

    @Test
    public void testCreate_Valid_Token() throws Database.DatabaseException {
        validKeyDBSuccess();
        Assert.assertNotNull(((ApiAdmin) createDev()).getDev_token());
    }

    @Test
    public void testCreate_KeyNull() throws Database.DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(null);
        when(this.fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_InvalidKey() throws Database.DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn("[]");
        when(this.fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyUpdateFail() throws Database.DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(false);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyDBFail() throws Database.DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any()))
                .thenThrow(Database.DatabaseException.class);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testDelete_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteDev(anyString())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, deleteDev(VALIDDEVID).getStatus().getCode());
    }

    @Test
    public void testDelete_NullDevid() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteDev(anyString())).thenThrow(Database.DatabaseException.class);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, deleteDev(null).getStatus().getCode());
    }

    @Test
    public void testDelete_NonExistentDevid() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteDev(anyString())).thenReturn(false);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, deleteDev("other").getStatus().getCode());
    }

    @Test
    public void testDelete_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteDev(anyString())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.adminLogic.deleteDev(this.fakeContext, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetDevToken() throws Database.DatabaseException {
        when(this.fakeDatabase.getDevToken(any())).thenReturn(DEVTOKEN);
        ApiAdmin admin = (ApiAdmin) this.adminLogic.getDevToken(this.fakeContext, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, admin.getStatus().getCode());
    }

    @Test
    public void testGetDevToken_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.getDevToken(any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.adminLogic.getDevToken(this.fakeContext, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetDevToken_devTokenEmpty() throws Database.DatabaseException {
        when(this.fakeDatabase.getDevToken(any())).thenReturn("");
        ApiResult result = this.adminLogic.getDevToken(this.fakeContext, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void validKeyDBSuccess() throws Database.DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabase.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(true);
    }

    private ApiResult createDev() {
        return this.adminLogic.createDev(this.fakeContext, "ROLE", "username", "email", "password", "passSalt", "firt_name", "last_time", 0);
    }

    private ApiResult deleteDev(String devid) {
        return this.adminLogic.deleteDev(this.fakeContext, devid);
    }
}
