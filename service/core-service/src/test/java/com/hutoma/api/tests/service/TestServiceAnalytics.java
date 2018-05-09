package com.hutoma.api.tests.service;

import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.connectors.AnalyticsESConnector;
import com.hutoma.api.endpoints.AnalyticsEndpoint;
import com.hutoma.api.logic.AnalyticsLogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Collections;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestServiceAnalytics extends ServiceTestBase {

    private static final String INSIGHTS_BASE_PATH =  "/insights/" + AIID.toString();
    private static final String INSIGHTS_CHATLOGS_PATH =  INSIGHTS_BASE_PATH + "/chatlogs";
    private static final String INSIGHTS_GRAPH_PATH =  INSIGHTS_BASE_PATH + "/graph";
    private static final String INSIGHTS_GRAPH_SESSIONS_PATH =  INSIGHTS_GRAPH_PATH + "/sessions";
    private static final String INSIGHTS_GRAPH_INTERACTIONS_PATH =  INSIGHTS_GRAPH_PATH + "/interactions";
    private static final String CHATLOGS_PARAM_FORMAT = "format";

    private AnalyticsESConnector fakeConnector;

    @Before
    public void setup() {
        when(this.fakeConfig.getRateLimit_Analytics_BurstRequests()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_Analytics_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getElasticSearchAnalyticsUrls()).thenReturn(Collections.singletonList(""));
    }

    @Test
    public void testGetChatLogs() {
        Response response = target(INSIGHTS_CHATLOGS_PATH)
                .queryParam(CHATLOGS_PARAM_FORMAT, AnalyticsResponseFormat.CSV.getName())
                .request()
                .headers(defaultHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetChatLogs_devId_invalid() {
        Response response = target(INSIGHTS_CHATLOGS_PATH)
                .queryParam(CHATLOGS_PARAM_FORMAT, AnalyticsResponseFormat.CSV.getName())
                .request()
                .headers(noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetChatSessions() {
        Response response = target(INSIGHTS_GRAPH_SESSIONS_PATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetChatSessions_devId_invalid() {
        Response response = target(INSIGHTS_GRAPH_SESSIONS_PATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetChatInteractions() {
        Response response = target(INSIGHTS_GRAPH_INTERACTIONS_PATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetChatInteractions_devId_invalid() {
        Response response = target(INSIGHTS_GRAPH_INTERACTIONS_PATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return AnalyticsEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeConnector = mock(AnalyticsESConnector.class);
        binder.bindFactory(new InstanceFactory<>(TestServiceAnalytics.this.fakeConnector)).to(AnalyticsESConnector.class);

        binder.bind(AnalyticsLogic.class).to(AnalyticsLogic.class);
        return binder;
    }
}
