package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.endpoints.AIEndpoint;
import com.hutoma.api.logic.AILogic;
import junitparams.JUnitParamsRunner;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
@RunWith(JUnitParamsRunner.class)
public class TestServiceAi extends ServiceTestBase {


    private static MultivaluedMap<String, String> getCreateAiRequestParams() {
        return new MultivaluedHashMap<String, String>() {{
            this.add("name", "ainame");
            this.add("description", null);
            this.add("confidence", "0.5");
            this.add("timezone", "UTC");
            this.add("locale", "en-US");
        }};
    }

    @Test
    public void testGetAIs() throws Database.DatabaseException {
        ApiAi ai = getAI();
        when(this.fakeDatabase.getAllAIs(any())).thenReturn(Collections.singletonList(ai));
        final Response response = target("/ai").request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiList list = deserializeResponse(response, ApiAiList.class);
        Assert.assertEquals(1, list.getAiList().size());
        Assert.assertEquals(ai.getAiid(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAI() throws Database.DatabaseException {
        ApiAi ai = getAI();
        when(this.fakeDatabase.getAI(any(), any())).thenReturn(ai);
        final Response response = target("/ai/" + AIID).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAi responseAi = deserializeResponse(response, ApiAi.class);
        Assert.assertEquals(ai.getAiid(), responseAi.getAiid());
    }

    @Test
    public void testDeleteAI() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        final Response response = target("/ai/" + AIID).request().headers(defaultHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testCreateAI() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(),
                anyInt(), anyInt(), any(), anyString(), any(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt())).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        final Response response = target("/ai").request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateAI() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(true);
        final Response response = target("/ai/" + AIID).request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetAI_devId_invalid() {
        Response response = target("/ai/" + AIID).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetAIs_devId_invalid() {
        Response response = target("/ai").request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testDeleteAI_devId_invalid() {
        final Response response = target("/ai/" + AIID).request().headers(noDevIdHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testCreateAI_devId_invalid() {
        final Response response = target("/ai").request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUpdateAI_devId_invalid() {
        final Response response = target("/ai/" + AIID).request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    private ApiAi getAI() {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false, 0.5, "debuginfo",
                "trainstatus", null, "", 0, 0.0, 1, Locale.UK, "Europe/London");
    }

    protected Class<?> getClassUnderTest() {
        return AIEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIServices.class).to(AIServices.class);
        binder.bind(AILogic.class).to(AILogic.class);
        return binder;
    }
}
