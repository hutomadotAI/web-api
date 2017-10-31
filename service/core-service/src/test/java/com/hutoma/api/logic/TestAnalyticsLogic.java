package com.hutoma.api.logic;

import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.AnalyticsESConnector;
import com.hutoma.api.containers.ApiListMap;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAnalyticsLogic {

    private AnalyticsLogic analyticsLogic;
    private AnalyticsESConnector fakeConnector;

    private static final String MAP_KEY = "a";
    private static final List<Map<String, Object>> listOfMaps = Collections.singletonList(
            new HashMap<String, Object>() {{
                put(MAP_KEY, "b");
            }}
    );

    @Before
    public void setup() {
        this.fakeConnector = mock(AnalyticsESConnector.class);
        this.analyticsLogic = new AnalyticsLogic(this.fakeConnector, mock(ILogger.class), mock(Config.class), mock(JsonSerializer.class));
    }

    @Test
    public void testAnalytics_getDateFrom() {
        final String dateString = "2017-10-03T19:23:44+00:00";
        final DateTime expected = DateTime.parse(dateString);
        DateTime dt = AnalyticsLogic.getDateFrom(dateString);
        Assert.assertEquals(expected, dt);
    }

    @Test
    public void testAnalytics_getDateFrom_null_usesDefault() {
        final DateTime expected = DateTime.now().minusDays(AnalyticsLogic.DEFAULT_DATE_FROM);
        DateTime dt = AnalyticsLogic.getDateFrom("");
        Assert.assertEquals(expected.getDayOfYear(), dt.getDayOfYear());
        dt = AnalyticsLogic.getDateFrom(null);
        Assert.assertEquals(expected.getDayOfYear(), dt.getDayOfYear());
    }

    @Test
    public void testAnalytics_getDateFrom_usesUTC() {
        testDateTimeToUtc(AnalyticsLogic::getDateFrom);
    }

    @Test
    public void testAnalytics_getDateTo_includesEoD_UTC() {
        final String dateString = "2017-10-03T19:23:44+00:00";
        final DateTime date = DateTime.parse(dateString);
        final DateTime expected = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
                23,59,59).toDateTime(DateTimeZone.UTC);
        DateTime dt = AnalyticsLogic.getDateTo(dateString);
        Assert.assertEquals(expected, dt);
    }

    @Test
    public void testAnalytics_getDateTo_null_usesDefault() {
        final DateTime date = DateTime.now().toDateTime(DateTimeZone.UTC);
        final DateTime expected = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
                23,59,59).toDateTime(DateTimeZone.UTC);
        DateTime dt = AnalyticsLogic.getDateTo(null);
        // Assert that the date is the same, give or take a few milliseconds
        final int toleranceMs = 200;
        assertThat(dt.getMillis()).isBetween(date.getMillis() - toleranceMs, date.getMillis() + toleranceMs);
    }

    @Test
    public void testGetChatLogs() throws AnalyticsESConnector.AnalyticsConnectorException {
        final String logs = "a,b,c";
        when(this.fakeConnector.getChatLogs(any(), any(), any(), any(), any())).thenReturn(logs);
        ApiString result = (ApiString) this.analyticsLogic.getChatLogs(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate(), AnalyticsResponseFormat.CSV);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(logs, result.getString());
    }

    @Test
    public void testGetChatLogs_invalidDateInterval() throws AnalyticsESConnector.AnalyticsConnectorException {
        final String logs = "a,b,c";
        when(this.fakeConnector.getChatLogs(any(), any(), any(), any(), any())).thenReturn(logs);
        ApiResult result = this.analyticsLogic.getChatLogs(DEVID_UUID, AIID, getTomorrowDate(), getYesterdayDate(), AnalyticsResponseFormat.CSV);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetChatLogs_connector_exception() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getChatLogs(any(), any(), any(), any(), any())).thenThrow(AnalyticsESConnector.AnalyticsConnectorException.class);
        ApiResult result = this.analyticsLogic.getChatLogs(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate(), AnalyticsResponseFormat.CSV);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetChatSessions() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getSessions(any(), any(), any(), any())).thenReturn(listOfMaps);
        ApiListMap result = (ApiListMap) this.analyticsLogic.getSessions(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        validateListOfMapsAreSimilar(listOfMaps, result.getObjects());
    }

    @Test
    public void testGetChatSessions_invalidDateInterval() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getSessions(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        ApiResult result = this.analyticsLogic.getSessions(DEVID_UUID, AIID, getTomorrowDate(), getYesterdayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetChatSessions_connector_exception() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getSessions(any(), any(), any(), any())).thenThrow(AnalyticsESConnector.AnalyticsConnectorException.class);
        ApiResult result = this.analyticsLogic.getSessions(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetChatInteractions() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getInteractions(any(), any(), any(), any())).thenReturn(listOfMaps);
        ApiListMap result = (ApiListMap) this.analyticsLogic.getInteractions(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        validateListOfMapsAreSimilar(listOfMaps, result.getObjects());
    }

    @Test
    public void testGetChatInteractions_invalidDateInterval() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getInteractions(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        ApiResult result = this.analyticsLogic.getInteractions(DEVID_UUID, AIID, getTomorrowDate(), getYesterdayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetChatInteractions_connector_exception() throws AnalyticsESConnector.AnalyticsConnectorException {
        when(this.fakeConnector.getInteractions(any(), any(), any(), any())).thenThrow(AnalyticsESConnector.AnalyticsConnectorException.class);
        ApiResult result = this.analyticsLogic.getInteractions(DEVID_UUID, AIID, getYesterdayDate(), getTodayDate());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private static String getTomorrowDate() {
        return DateTime.now().plusDays(1).toString();
    }

    private static String getYesterdayDate() {
        return DateTime.now().minusDays(1).toString();
    }

    private static String getTodayDate() {
        return DateTime.now().toString();
    }

    private static void validateListOfMapsAreSimilar(final List<Map<String, Object>> list1, final List<Map<String, Object>> list2) {
        Assert.assertEquals(list1.size(), list2.size());
        Assert.assertEquals(list1.get(0).values().size(), list2.get(0).values().size());
        Assert.assertEquals(list1.get(0).get(MAP_KEY), list2.get(0).get(MAP_KEY));
    }

    private void testDateTimeToUtc(final Function<String, DateTime> func) {
        final String dateString = "2017-10-03T19:23:44+05:00"; // Note the +5 hours
        final DateTime localDateTime = DateTime.parse(dateString);
        final DateTime utcDateTime = DateTime.parse(dateString).toDateTime(DateTimeZone.UTC);
        DateTime dt = func.apply(dateString);
        Assert.assertEquals(utcDateTime, dt);
        Assert.assertNotEquals(localDateTime, dt);
    }
}
