package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAnalyticsLogic {

    private AnalyticsLogic analyticsLogic;

    @Before
    public void setup() {
        this.analyticsLogic = new AnalyticsLogic(mock(ILogger.class), mock(Config.class), mock(JsonSerializer.class));
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
    public void testAnalytics_flattenMapRec() {
        Map<String, Object> map = new HashMap<String, Object>() {{
           put("a", "value1");
           put("b", new HashMap<String, Object>() {{
               put("b1", "valB1");
               put("b2", "balB2");
           }});
        }};
        Map<String, String> stored = new HashMap<>();
        this.analyticsLogic.flattenMapRec(map, stored, "");
        Assert.assertEquals(3, stored.size());
        Assert.assertEquals(map.get("a"), stored.get("a"));
        Assert.assertEquals(((Map<String, Object>) map.get("b")).get("b1"), stored.get("b.b1"));
        Assert.assertEquals(((Map<String, Object>) map.get("b")).get("b2"), stored.get("b.b2"));
    }

    @Test
    public void testAnalytics_flattenMapRec_nullValues() {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("a", null);
            put("b", new HashMap<String, Object>() {{
                put("b1", null);
            }});
        }};
        Map<String, String> stored = new HashMap<>();
        this.analyticsLogic.flattenMapRec(map, stored, "");
        Assert.assertEquals(2, stored.size());
        Assert.assertNull(stored.get("a"));
        Assert.assertNull(stored.get("b.b1"));
    }

    @Test
    public void testAnalytics_isNumber() {
        Assert.assertTrue(AnalyticsLogic.isNumber("1"));
        Assert.assertTrue(AnalyticsLogic.isNumber("123456"));
        Assert.assertTrue(AnalyticsLogic.isNumber("-1"));
        Assert.assertTrue(AnalyticsLogic.isNumber("0"));
        Assert.assertTrue(AnalyticsLogic.isNumber("0.000001"));
        Assert.assertFalse(AnalyticsLogic.isNumber(""));
        Assert.assertFalse(AnalyticsLogic.isNumber(null));
        Assert.assertFalse(AnalyticsLogic.isNumber(" "));
        Assert.assertFalse(AnalyticsLogic.isNumber("A"));
        Assert.assertFalse(AnalyticsLogic.isNumber("123B"));
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
