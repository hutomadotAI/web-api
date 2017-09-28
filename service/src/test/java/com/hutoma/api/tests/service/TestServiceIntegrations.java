package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.sub.Integration;
import com.hutoma.api.endpoints.AIIntegrationEndpoint;
import com.hutoma.api.logic.AIIntegrationLogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.when;

public class TestServiceIntegrations extends ServiceTestBase {

    private static final String BASEPATH = "/ai/integration";

    @Test
    public void testGetIntegrations() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(Collections.singletonList(
                new Integration(1, "", "", "", true)));
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiIntegrationList list = deserializeResponse(response, ApiIntegrationList.class);
        Assert.assertEquals(1, list.getIntegrationList().size());
    }

    @Test
    public void testGetIntegrations_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(new ArrayList<Integration>());
        final Response response = target(BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return AIIntegrationEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIIntegrationLogic.class).to(AIIntegrationLogic.class);
        return binder;
    }
}
