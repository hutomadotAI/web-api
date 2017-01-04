package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AI Bot Store service tests.
 */
public class TestServiceAiBotstore extends ServiceTestBase {

    private static final int BOTID = 1234;
    private static final String BOTSTORE_BASEPATH = "/botstore";
    private static final String BOTSTORE_PURCHASEDPATH = BOTSTORE_BASEPATH + "/purchased";
    private static final String BOTSTORE_PURCHASEBOTPATH = BOTSTORE_BASEPATH + "/purchase/" + BOTID;
    private static final AiBot SAMPLEBOT =
            new AiBot(DEVID.toString(), AIID, BOTID, "name", "description", "long description", "alert message", "badge",
                    BigDecimal.valueOf(1.123), "sample", "category", DateTime.now(), "privacy policy",
                    "classification", "version", "http://video", true);

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

    protected Class<?> getClassUnderTest() {
        return AIBotStoreEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        return binder;
    }
}
