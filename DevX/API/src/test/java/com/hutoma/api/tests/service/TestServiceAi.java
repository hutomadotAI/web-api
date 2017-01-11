package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
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

import static com.hutoma.api.common.BotHelper.BOTID;
import static com.hutoma.api.common.BotHelper.SAMPLEBOT;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
@RunWith(JUnitParamsRunner.class)
public class TestServiceAi extends ServiceTestBase {

    private static final String AI_BASEPATH = "/ai";
    private static final String AI_PATH = AI_BASEPATH + "/" + AIID;
    private static final String BOTS_BASEPATH = AI_PATH + "/bots";
    private static final String BOT_BASEPATH = AI_PATH + "/bot";
    private static final String BOT_PATH = BOT_BASEPATH + "/" + BOTID;

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
        when(this.fakeDatabase.getAllAIs(any(), any())).thenReturn(Collections.singletonList(ai));
        final Response response = target(AI_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiList list = deserializeResponse(response, ApiAiList.class);
        Assert.assertEquals(1, list.getAiList().size());
        Assert.assertEquals(ai.getAiid(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAI() throws Database.DatabaseException {
        ApiAi ai = getAI();
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(ai);
        final Response response = target(AI_PATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAi responseAi = deserializeResponse(response, ApiAi.class);
        Assert.assertEquals(ai.getAiid(), responseAi.getAiid());
    }

    @Test
    public void testDeleteAI() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        final Response response = target(AI_PATH).request().headers(defaultHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testCreateAI() throws Database.DatabaseException {
        final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(this.fakeTools.createNewRandomUUID()).thenReturn(uuid);
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyObject(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenReturn(uuid);
        final Response response = target(AI_BASEPATH).request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateAI() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(true);
        final Response response = target(AI_PATH).request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetAI_devId_invalid() {
        Response response = target(AI_PATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetAIs_devId_invalid() {
        Response response = target(AI_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testDeleteAI_devId_invalid() {
        final Response response = target(AI_PATH).request().headers(noDevIdHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testCreateAI_devId_invalid() {
        final Response response = target(AI_BASEPATH).request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUpdateAI_devId_invalid() {
        final Response response = target(AI_PATH).request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetLinkedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTS_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList botList = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, botList.getBotList().size());
        Assert.assertEquals(BOTID, botList.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetLinkedBots_devId_invalid() throws Database.DatabaseException {
        final Response response = target(BOTS_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testLinkBotToAi() throws Database.DatabaseException {
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(true);
        final Response response = target(BOT_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(new MultivaluedHashMap<>()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testLinkBotToAi_devId_invalid() throws Database.DatabaseException {
        final Response response = target(BOT_PATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.form(new MultivaluedHashMap<>()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUnlinkBotToAi() throws Database.DatabaseException {
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(true);
        final Response response = target(BOT_PATH).request().headers(defaultHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUnlLinkBotToAi_devId_invalid() throws Database.DatabaseException {
        final Response response = target(BOT_PATH).request().headers(noDevIdHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetPublishedBotForAI() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(anyString(), any())).thenReturn(SAMPLEBOT);
        final Response response = target(BOT_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetPublishedBotForAI_devId_invalid() throws Database.DatabaseException {
        final Response response = target(BOT_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    private ApiAi getAI() {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                new BackendStatus(), TrainingStatus.AI_UNDEFINED,
                0, 0.0, 1, Locale.UK, "Europe/London");
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
