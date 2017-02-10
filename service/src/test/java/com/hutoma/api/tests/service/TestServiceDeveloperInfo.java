package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.endpoints.DeveloperInfoEndpoint;
import com.hutoma.api.logic.DeveloperInfoLogic;
import junitparams.JUnitParamsRunner;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Developer Info endpoint service tests.
 */
@RunWith(JUnitParamsRunner.class)
public class TestServiceDeveloperInfo extends ServiceTestBase {

    private static final String DEVINFO_BASEPATH = "/developer/";
    private static final String DEVINFO_PATH = DEVINFO_BASEPATH + DEVID;

    private static final DeveloperInfo DEVINFO = getDevInfo(DEVID.toString());

    private static DeveloperInfo getDevInfo(final String devId) {
        return new DeveloperInfo(
                devId, "name", "company", "email@email.com", "address", "post code", "city", "country", "http://web");
    }

    private static MultivaluedMap<String, String> getSetDevInfoRequestParams() {
        return new MultivaluedHashMap<String, String>() {{
            this.add("name", "dev name");
            this.add("company", "dev company");
            this.add("email", "dev email");
            this.add("address", "dev address");
            this.add("postCode", "dev post code");
            this.add("city", "dev city");
            this.add("country", "dev city");
        }};
    }

    private static MultivaluedMap<String, String> getSetDevInfoRequestNullParams() {
        return new MultivaluedHashMap<String, String>() {{
            this.add("website", "dev website");
        }};
    }

    @Test
    public void testGetDeveloperInfo_ownInfo() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(DEVINFO);
        final Response response = target(DEVINFO_PATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiDeveloperInfo info = deserializeResponse(response, ApiDeveloperInfo.class);
        Assert.assertNotNull(info.getInfo());
        Assert.assertEquals(DEVINFO.getDevId(), info.getInfo().getDevId());
    }

    public void testGetDeveloperInfo_otherDevInfo() throws Database.DatabaseException {
        final DeveloperInfo info = getDevInfo("other dev");
        when(this.fakeDatabase.getDeveloperInfo(any())).thenReturn(info);
        final Response response = target(DEVINFO_BASEPATH + info.getDevId()).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiDeveloperInfo apiInfo = deserializeResponse(response, ApiDeveloperInfo.class);
        Assert.assertNotNull(apiInfo.getInfo());
        Assert.assertEquals(info.getDevId(), apiInfo.getInfo().getDevId());
        Assert.assertNull(info.getDevId(), apiInfo.getInfo().getAddress());
    }

    @Test
    public void testPostDeveloperInfo() throws Database.DatabaseException {
        when(this.fakeDatabase.setDeveloperInfo(any())).thenReturn(true);
        final Response response = target(DEVINFO_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(getSetDevInfoRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiDeveloperInfo info = deserializeResponse(response, ApiDeveloperInfo.class);
        Assert.assertNotNull(info.getInfo());
        Assert.assertEquals(DEVINFO.getDevId(), info.getInfo().getDevId());
    }

    @Test
    public void testPostDeveloperInfo_nullParams() throws Database.DatabaseException {
        final Response response = target(DEVINFO_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(getSetDevInfoRequestNullParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return DeveloperInfoEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(DeveloperInfoLogic.class).to(DeveloperInfoLogic.class);
        return binder;
    }
}
