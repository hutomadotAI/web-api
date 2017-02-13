package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;

import static com.hutoma.api.common.DeveloperInfoHelper.DEVINFO;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 03/01/17.
 */
public class TestDeveloperInfoLogic {

    private Database fakeDatabase;
    private DeveloperInfoLogic devInfoLogic;

    @Before
    public void setup() {
        ILogger fakeLogger = mock(ILogger.class);
        this.fakeDatabase = mock(Database.class);
        this.devInfoLogic = new DeveloperInfoLogic(fakeLogger, this.fakeDatabase);
    }

    @Test
    public void testGet_Valid_requestOwnInfo() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(DEVINFO);
        ApiDeveloperInfo info = (ApiDeveloperInfo) this.devInfoLogic.getDeveloperInfo(DEVID, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, info.getStatus().getCode());
        Assert.assertEquals(DEVINFO.getDevId(), info.getInfo().getDevId());
        Assert.assertEquals(DEVINFO.getName(), info.getInfo().getName());
    }

    @Test
    public void testGet_Valid_requestOthedDevInfo() throws Database.DatabaseException {
        final String requesterDevId = "requester";
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(DEVINFO);
        ApiDeveloperInfo info = (ApiDeveloperInfo) this.devInfoLogic.getDeveloperInfo(requesterDevId, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, info.getStatus().getCode());
        Assert.assertEquals(DEVINFO.getDevId(), info.getInfo().getDevId());
        Assert.assertEquals(DEVINFO.getCompany(), info.getInfo().getCompany());
        Assert.assertEquals(DEVINFO.getWebsite(), info.getInfo().getWebsite());
        Assert.assertNull(info.getInfo().getName());
        Assert.assertNull(info.getInfo().getAddress());
        Assert.assertNull(info.getInfo().getCity());
        Assert.assertNull(info.getInfo().getCountry());
        Assert.assertNull(info.getInfo().getPostCode());
        Assert.assertNull(info.getInfo().getEmail());
    }

    @Test
    public void testGet_notFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(null);
        ApiResult info = this.devInfoLogic.getDeveloperInfo(DEVID, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, info.getStatus().getCode());
    }

    @Test
    public void testGet_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenThrow(Database.DatabaseException.class);
        ApiResult info = this.devInfoLogic.getDeveloperInfo(DEVID, DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, info.getStatus().getCode());
    }

    @Test
    public void testSet_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.setDeveloperInfo(any())).thenReturn(true);
        ApiResult result = callSetDeveloperInfo();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testSet_requiredParamNull() throws Database.DatabaseException {
        ApiResult result = this.devInfoLogic.setDeveloperInfo(DEVID, null, DEVINFO.getCompany(),
                DEVINFO.getEmail(), DEVINFO.getAddress(), DEVINFO.getPostCode(), DEVINFO.getCity(),
                DEVINFO.getCountry(), DEVINFO.getWebsite());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSet_requiredParamEmpty() throws Database.DatabaseException {
        ApiResult result = this.devInfoLogic.setDeveloperInfo(DEVID, DEVINFO.getName(), "",
                DEVINFO.getEmail(), DEVINFO.getAddress(), DEVINFO.getPostCode(), DEVINFO.getCity(),
                DEVINFO.getCountry(), DEVINFO.getWebsite());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSet_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.setDeveloperInfo(any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = callSetDeveloperInfo();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testSet_DB_no_updates() throws Database.DatabaseException {
        when(this.fakeDatabase.setDeveloperInfo(any())).thenReturn(false);
        ApiResult result = callSetDeveloperInfo();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testSet_alreadyExists() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(DEVINFO);
        ApiResult result = callSetDeveloperInfo();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private ApiResult callSetDeveloperInfo() {
        return this.devInfoLogic.setDeveloperInfo(DEVID, DEVINFO.getName(), DEVINFO.getCompany(),
                DEVINFO.getEmail(), DEVINFO.getAddress(), DEVINFO.getPostCode(), DEVINFO.getCity(),
                DEVINFO.getCountry(), DEVINFO.getWebsite());
    }
}
