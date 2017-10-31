package com.hutoma.api.endpoints;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by paul on 20/07/17.
 */
public class TestHeaderUtils {

    @Test
    public void test_noHeaders() {
        MultivaluedMap<String, String> headersIn = new MultivaluedHashMap<>();
        Map<String,String> headersOut = HeaderUtils.getClientVariablesFromHeaders(headersIn);
        Assert.assertEquals(0, headersOut.size());
    }

    @Test
    public void test_noMatchingHeaders() {
        MultivaluedMap<String, String> headersIn = new MultivaluedHashMap<>();
        headersIn.put("blah", null);
        headersIn.put("foo", null);
        Map<String,String> headersOut = HeaderUtils.getClientVariablesFromHeaders(headersIn);
        Assert.assertEquals(0, headersOut.size());
    }

    @Test
    public void test_matchingHeaders() {
        MultivaluedMap<String, String> headersIn = new MultivaluedHashMap<>();
        headersIn.put("x-hutoma-var-blah", Arrays.asList("1"));
        headersIn.put("foo", null);
        Map<String,String> headersOut = HeaderUtils.getClientVariablesFromHeaders(headersIn);
        Assert.assertEquals(1, headersOut.size());
        Assert.assertEquals("1", headersOut.get("blah"));
    }

    @Test
    public void test_stripDuplicate() {
        MultivaluedMap<String, String> headersIn = new MultivaluedHashMap<>();

        // add two entries "1" and "2" which is what would happen if a header is duplicated.
        headersIn.put("x-hutoma-var-duplicate", Arrays.asList("1", "2"));
        Map<String,String> headersOut = HeaderUtils.getClientVariablesFromHeaders(headersIn);
        Assert.assertEquals(0, headersOut.size());
    }
}
