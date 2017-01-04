package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.endpoints.AIBotStoreEndpoint;
import com.hutoma.api.logic.AIBotStoreLogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * AI Bot Store service tests.
 */
public class TestServiceAiBotstore extends ServiceTestBase {

    private static final int BOTID = 1234;
    private static final String BOTSTORE_BASEPATH = "/botstore";
    private static final String BOTSTORE_BOTPATH = BOTSTORE_BASEPATH + "/" + BOTID;
    private static final String BOTSTORE_PURCHASEDPATH = BOTSTORE_BASEPATH + "/purchased";
    private static final String BOTSTORE_PURCHASEBOTPATH = BOTSTORE_BASEPATH + "/purchase/" + BOTID;

    private static final AiBot SAMPLEBOT =
            new AiBot(DEVID.toString(), AIID, BOTID, "name", "description", "long description", "alert message", "badge",
                    BigDecimal.valueOf(1.123), "sample", "category", DateTime.now(), "privacy policy",
                    "classification", "version", "http://video", true);


    private static final MultivaluedMap<String, String> BOT_PUBLISH_POST = new MultivaluedHashMap<String, String>() {{
        this.put("aiid", Collections.singletonList(UUID.randomUUID().toString()));
        this.put("name", Collections.singletonList("bot name"));
        this.put("description", Collections.singletonList("bot description"));
        this.put("price", Collections.singletonList("1.0"));
    }};

    @Test
    public void testGetPublishedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTSTORE_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList list = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, list.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), list.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPublishedBots_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetBotDetails() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(BOTID)).thenReturn(SAMPLEBOT);
        final Response response = target(BOTSTORE_BOTPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBot bot = deserializeResponse(response, ApiAiBot.class);
        Assert.assertNotNull(bot.getBot());
        Assert.assertEquals(BOTID, bot.getBot().getBotId());
    }

    @Test
    public void testGetBotDetails_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BOTPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetPurchasedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTSTORE_PURCHASEDPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList list = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, list.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), list.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPurchasedBots_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_PURCHASEDPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testPurchaseBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(true);
        final Response response = target(BOTSTORE_PURCHASEBOTPATH).request().headers(defaultHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testPurchaseBot_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_PURCHASEBOTPATH).request().headers(noDevIdHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testPublishBot() throws Database.DatabaseException {
        final int newBotId = 76832;
        when(this.fakeDatabase.publishBot(any())).thenReturn(newBotId);
        final Response response = target(BOTSTORE_BASEPATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(BOT_PUBLISH_POST));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBot bot = deserializeResponse(response, ApiAiBot.class);
        Assert.assertNotNull(bot.getBot());
        Assert.assertEquals(newBotId, bot.getBot().getBotId());
    }

    @Test
    public void testPublishBot_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BASEPATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.form(BOT_PUBLISH_POST));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return AIBotStoreEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        return binder;
    }
}
