package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DeveloperInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 03/01/17.
 */
public class TestDeveloperInfoLogic {

    private static final String DEVID = "devid";
    private static final DeveloperInfo DEVINFO = new DeveloperInfo(
            DEVID, "name", "company", "email@email.com", "address", "post code", "city", "country", "http://web"
    );

    private Database fakeDatabase;
    private DeveloperInfoLogic devInfoLogic;

    @Before
    public void setup() {
        ILogger fakeLogger = mock(ILogger.class);
        this.fakeDatabase = mock(Database.class);
        this.devInfoLogic = new DeveloperInfoLogic(fakeLogger, this.fakeDatabase);
    }

    @Test
    public void testGet_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(DEVINFO);
        ApiDeveloperInfo info = (ApiDeveloperInfo) this.devInfoLogic.getDeveloperInfo(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, info.getStatus().getCode());
        Assert.assertEquals(DEVINFO.getDevId(), info.getInfo().getDevId());
        Assert.assertEquals(DEVINFO.getName(), info.getInfo().getName());
    }

    @Test
    public void testGet_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenThrow(Database.DatabaseException.class);
        ApiResult info = this.devInfoLogic.getDeveloperInfo(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, info.getStatus().getCode());
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

    private ApiResult callSetDeveloperInfo() {
        return this.devInfoLogic.setDeveloperInfo(DEVID, DEVINFO.getName(), DEVINFO.getCompany(),
                DEVINFO.getEmail(), DEVINFO.getAddress(), DEVINFO.getPostCode(), DEVINFO.getCity(),
                DEVINFO.getCountry(), DEVINFO.getWebsite());
    }
}
