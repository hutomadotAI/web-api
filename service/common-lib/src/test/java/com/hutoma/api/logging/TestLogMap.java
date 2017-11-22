package com.hutoma.api.logging;

import com.hutoma.api.logging.LogMap;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by pedrotei on 23/03/17.
 */
public class TestLogMap {
    @Test
    public void testCreateStaticMap() {
        LogMap logMap = LogMap.map("a", "b");
        Assert.assertEquals(1, logMap.get().size());
        Assert.assertEquals("b", logMap.get().get("a"));
    }

    @Test
    public void testCopyConstrutor() {
        LogMap logMap1 = LogMap.map("a", "b");
        LogMap logMap2 = new LogMap(logMap1);
        Assert.assertEquals(1, logMap2.get().size());
        Assert.assertEquals("b", logMap2.get().get("a"));
    }

    @Test
    public void testHashMapConstrutor() {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("a", "b");
        }};
        LogMap logMap = new LogMap(map);
        Assert.assertEquals(1, logMap.get().size());
        Assert.assertEquals("b", logMap.get().get("a"));
    }

    @Test
    public void testAdding() {
        LogMap logMap = LogMap.map("a", "b");
        logMap.add("c", "d");
        logMap.add("e", 0);
        Assert.assertEquals(3, logMap.get().size());
        Assert.assertEquals("b", logMap.get().get("a"));
        Assert.assertEquals("d", logMap.get().get("c"));
        Assert.assertEquals(0, logMap.get().get("e"));
    }

    @Test
    public void testPutting() {
        LogMap logMap = LogMap.map("a", "b");
        // put affects what is returned
        Assert.assertEquals("d", logMap.put("c", "d").get().get("c"));
        // but doesn't affect the original object
        Assert.assertEquals(1, logMap.get().size());
    }

    @Test
    public void testMixingAddingAndPutting() {
        LogMap logMap = LogMap.map("a", "b");
        Assert.assertEquals("d", logMap.put("c", "d").get().get("c"));
        logMap.add("e", 0);
        Assert.assertEquals(0, logMap.put("f", "g").get().get("e"));
        Assert.assertFalse(logMap.get().containsKey("f"));
    }

    @Test
    public void testConvertValue_map() {
        // An UUID is converted to string
        LogMap logMap = LogMap.map("a", UUID.randomUUID());
        Assert.assertTrue(logMap.get().get("a") instanceof String);
        // But an integer is still an integer
        logMap = LogMap.map("b", 1);
        Assert.assertTrue(logMap.get().get("b") instanceof Integer);
    }

    @Test
    public void testConvertValue_put() {
        LogMap logMap = new LogMap((Map<String, Object>) null);
        // An UUID is converted to string
        Assert.assertTrue(logMap.put("a", UUID.randomUUID()).get().get("a") instanceof String);
        // But an integer is still an integer
        Assert.assertTrue(logMap.put("b", 1).get().get("b") instanceof Integer);
    }

    @Test
    public void testConvertValue_add() {
        LogMap logMap = new LogMap((Map<String, Object>) null);
        // An UUID is converted to string
        logMap.add("a", UUID.randomUUID());
        Assert.assertTrue(logMap.get().get("a") instanceof String);
        // But an integer is still an integer
        logMap.add("b", 1);
        Assert.assertTrue(logMap.put("b", 1).get().get("b") instanceof Integer);
    }

    @Test
    public void testConvertValue_mapCtor() {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("a", UUID.randomUUID());
            put("b", 1);
            put("c", new HashMap<String, Object>() {{
                put("c_a", UUID.randomUUID());
                put("c_b", 1);
            }});
        }};
        LogMap logMap = new LogMap(map);
        // An UUID is converted to string
        Assert.assertTrue(logMap.get().get("a") instanceof String);
        // But an integer is still an integer
        Assert.assertTrue(logMap.put("b", 1).get().get("b") instanceof Integer);
        // Now the submap needs to have been converted as well
        Map<String, Object> subMap = (Map<String, Object>) logMap.get().get("c");
        Assert.assertTrue(subMap.get("c_a") instanceof String);
        Assert.assertTrue(subMap.get("c_b") instanceof Integer);
    }
}
