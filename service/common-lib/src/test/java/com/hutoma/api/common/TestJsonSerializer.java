package com.hutoma.api.common;

import com.google.gson.reflect.TypeToken;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestJsonSerializer {

    @Test
    public void testDeserializeObject() {
        String json = "{\"member1\":\"aaa\", \"member2\":123, \"member3\":[\"a\",\"b\"]}";
        JsonSerializer js = new JsonSerializer();
        ObjTest obj = (ObjTest) js.deserialize(json, ObjTest.class);
        Assert.assertEquals("aaa", obj.member1);
        Assert.assertEquals(123, obj.member2);
        Assert.assertEquals("a", obj.member3.get(0));
        Assert.assertEquals("b", obj.member3.get(1));
    }

    @Test
    public void testSerializeObject() {
        ObjTest obj = new ObjTest("aaa", 123, Arrays.asList("a", "b"));
        JsonSerializer js = new JsonSerializer(false);
        String json = js.serialize(obj);
        Assert.assertEquals("{\"member1\":\"aaa\",\"member2\":123,\"member3\":[\"a\",\"b\"]}", json);
    }

    @Test
    public void testDeserializeListAutoDetect() {
        String jsonList = "[\"a\",\"b\"]";
        JsonSerializer js = new JsonSerializer();
        List<String> list = js.deserializeListAutoDetect(jsonList);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
    }

    @Test
    public void testDeserializeObjectList() {
        String json = "[{\"member1\":\"aaa\", \"member2\":123, \"member3\":[\"a\",\"b\"]},"
                + "{\"member1\":\"bbb\", \"member2\":456, \"member3\":[\"c\",\"d\"]}]";
        JsonSerializer js = new JsonSerializer();
        List<ObjTest> list = js.deserializeList(json, new TypeToken<List<ObjTest>>() {}.getType());
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("aaa", list.get(0).member1);
        Assert.assertEquals(123, list.get(0).member2);
        Assert.assertEquals("a", list.get(0).member3.get(0));
        Assert.assertEquals("b", list.get(0).member3.get(1));
        Assert.assertEquals("bbb", list.get(1).member1);
        Assert.assertEquals(456, list.get(1).member2);
        Assert.assertEquals("c", list.get(1).member3.get(0));
        Assert.assertEquals("d", list.get(1).member3.get(1));
    }

    @Test
    public void testDeserializeStringMap() {
        String json = "{\"a\":\"123\", \"b\":\"456\"}";
        JsonSerializer js = new JsonSerializer();
        Map<String, String> map = js.deserializeStringMap(json);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("123", map.get("a"));
        Assert.assertEquals("456", map.get("b"));
    }

    @Test
    public void testDeserializeStringMapNullOrEmpty() {
        JsonSerializer js = new JsonSerializer();
        Assert.assertTrue(js.deserializeStringMap((String)null).isEmpty());
        Assert.assertTrue(js.deserializeStringMap("").isEmpty());
    }

    @Test
    public void testSerialization_allowNulls() {
        ObjTest obj = new ObjTest("aaa", 123, null);
        JsonSerializer js = new JsonSerializer(false);
        js.allowNullsOnSerialization();
        String json = js.serialize(obj);
        Assert.assertEquals("{\"member1\":\"aaa\",\"member2\":123,\"member3\":null}", json);
    }

    @Test
    public void testSerialization_removeNulls() {
        ObjTest obj = new ObjTest("aaa", 123, null);
        JsonSerializer js = new JsonSerializer(false);
        // Don't call JsonSerializer::allowNullsOnSerialization, as removing nulls should be the default
        String json = js.serialize(obj);
        Assert.assertEquals("{\"member1\":\"aaa\",\"member2\":123}", json);
    }
    
    private static class ObjTest {
        private String member1;
        private int member2;
        private List<String> member3;
        ObjTest(String member1, int member2, List<String> member3) {
            this.member1 = member1;
            this.member2 = member2;
            this.member3 = member3;
        }
    }
}
