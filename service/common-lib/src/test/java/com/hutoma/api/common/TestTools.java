package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Unit Tests for Tools.
 */
public class TestTools {

    private Tools tools;

    @Before
    public void setup() {
        this.tools = new Tools();
    }

    @Test
    public void testCreateNewRandomUuid() {
        UUID uuid1 = this.tools.createNewRandomUUID();
        Assert.assertNotNull(uuid1);
        UUID uuid2 = this.tools.createNewRandomUUID();
        Assert.assertNotEquals(uuid2, uuid1);
    }

    @Test
    public void testCreateHashFromUuid_nullUuid() {
        Assert.assertNull(Tools.getHashedDigest((UUID) null));
    }

    @Test
    public void testCreateHashFromUuid() {
        final UUID uuid = UUID.fromString("9d5ff3ae-1a7f-4167-8347-5d3618426096");
        Assert.assertEquals(64, Tools.getHashedDigest(uuid).length());
    }

    @Test
    public void testCreateHashFromUuid_different() {
        Assert.assertNotEquals(Tools.getHashedDigest(UUID.randomUUID()), Tools.getHashedDigest(UUID.randomUUID()));
    }

    @Test
    public void testCreateHashFromString() {
        final String toHash = "this is a string to hash";
        Assert.assertEquals(64, Tools.getHashedDigest(toHash).length());
    }

    @Test
    public void testCreateHashFromString_null() {
        Assert.assertNull(Tools.getHashedDigest((String) null));
    }

    @Test
    public void testCreateHashFromString_notEqual() {
        Assert.assertNotEquals(Tools.getHashedDigest("string1"), Tools.getHashedDigest("string2"));
    }

    @Test
    public void testCreateHashFromString_stringsEqual_resultNotEqual() {
        String string = "this is the same string";
        Assert.assertNotEquals(Tools.getHashedDigest(string), Tools.getHashedDigest(string));
    }


    @Test
    public void testAnalytics_isNumber() {
        Assert.assertTrue(Tools.isNumber("1"));
        Assert.assertTrue(Tools.isNumber("123456"));
        Assert.assertTrue(Tools.isNumber("-1"));
        Assert.assertTrue(Tools.isNumber("0"));
        Assert.assertTrue(Tools.isNumber("0.000001"));
        Assert.assertFalse(Tools.isNumber(""));
        Assert.assertFalse(Tools.isNumber(null));
        Assert.assertFalse(Tools.isNumber(" "));
        Assert.assertFalse(Tools.isNumber("A"));
        Assert.assertFalse(Tools.isNumber("123B"));
    }
}
