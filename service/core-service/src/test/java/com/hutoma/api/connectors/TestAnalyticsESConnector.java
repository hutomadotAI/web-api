package com.hutoma.api.connectors;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestAnalyticsESConnector {

    @SuppressWarnings("unchecked")
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
        AnalyticsESConnector.flattenMapRec(map, stored, "");
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
        AnalyticsESConnector.flattenMapRec(map, stored, "");
        Assert.assertEquals(2, stored.size());
        Assert.assertNull(stored.get("a"));
        Assert.assertNull(stored.get("b.b1"));
    }
}
