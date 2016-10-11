package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedrotei on 06/10/16.
 */
public class TestPair {
    @Test
    public void testAssignment() {
        Pair<String, Integer> p = new Pair<>("s", 2);
        Assert.assertEquals("s", p.getA());
        Assert.assertEquals((Integer)2, p.getB());
    }

    @Test
    public void testEquality() {
        Pair<Integer, Integer> p1 = new Pair<>(1, 2);
        Pair<Integer, Integer> p2 = new Pair<>(1, 2);
        Assert.assertTrue(p1.equals(p2));
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testInequality() {
        Pair<Integer, Integer> p1 = new Pair<>(1, 2);
        Pair<Integer, Integer> p2 = new Pair<>(2, 3);
        Assert.assertNotEquals(p1, p2);
        Assert.assertFalse(p1.equals(p2));

        Pair<Integer, String> p3 = new Pair<>(2, "a");
        Assert.assertNotEquals(p2, p3);
        Assert.assertFalse(p2.equals(p3));

        String s = "not a pair";
        Assert.assertFalse(p2.equals(s));
    }

    @Test
    public void testCopy() {
        Pair<Integer, Integer> p1 = new Pair<>(1, 2);
        Pair<Integer, Integer> p2 = new Pair<>(p1);
        Assert.assertEquals((Integer)1, p2.getA());
        Assert.assertEquals((Integer)2, p2.getB());
    }
}
