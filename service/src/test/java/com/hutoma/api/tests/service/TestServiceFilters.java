package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.endpoints.AIIntegrationEndpoint;
import com.hutoma.api.logic.AIIntegrationLogic;
import junitparams.JUnitParamsRunner;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class TestServiceFilters extends ServiceTestBase {

    private static final String BASEPATH = "/ai/integration";

    @Test
    public void testGetNormal() throws Database.DatabaseException {
        when(this.fakeDatabase.checkRateLimit(anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(new RateLimitStatus(false, 1.0, true));
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testRateLimited() throws Database.DatabaseException {
        when(this.fakeDatabase.checkRateLimit(anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(new RateLimitStatus(true, 1.0, true));
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(429, response.getStatus());
    }

    @Test
    public void testAccountDisabled() throws Database.DatabaseException {
        when(this.fakeDatabase.checkRateLimit(anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(new RateLimitStatus(false, 1.0, false));
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.getStatus());
    }

    @Test
    public void testAccountDisabledAndRateLimited() throws Database.DatabaseException {
        when(this.fakeDatabase.checkRateLimit(anyString(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(new RateLimitStatus(true, 1.0, false));
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.getStatus());
    }

    @Test
    public void testMissingAuthHeaderGives401() throws Database.DatabaseException {
        final Response response = target(BASEPATH).request().get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return AIIntegrationEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIIntegrationLogic.class).to(AIIntegrationLogic.class);
        return binder;
    }
}
