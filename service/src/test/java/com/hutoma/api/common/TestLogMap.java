package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
}
