package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by David MG on 10/08/2016.
 */
public class TestToolsTextSanitizer {

    Tools tools;

    @Before
    public void setup() {
        tools = new Tools();
    }

    @Test
    public void testTextNormal() {
        Assert.assertEquals("abcXYZ", tools.textSanitizer("abcXYZ"));
    }

    @Test
    public void testTextTrim() {
        Assert.assertEquals("abcXYZ", tools.textSanitizer(" abcXYZ "));
    }

    @Test
    public void testTextTrimLots() {
        Assert.assertEquals("abcXYZ", tools.textSanitizer("            abcXYZ             "));
    }

    @Test
    public void testTextDedupeWhitespace() {
        Assert.assertEquals("a b cX Y Z", tools.textSanitizer("a   b cX\tY\t\t\tZ"));
    }

    @Test
    public void testTextRemoveInvalidParentheses() {
        Assert.assertEquals("abcXYZ", tools.textSanitizer("(abc)[X]<YZ>"));
    }

    @Test
    public void testTextEscapeQuotes() {
        Assert.assertEquals("\\'abcXYZ\\\"", tools.textSanitizer("\'abcXYZ\""));
    }

    @Test
    public void testTextRemoveNonAscii() {
        Assert.assertEquals("abcXYZ", tools.textSanitizer("abc日本語XYZ"));
    }

    @Test
    public void testTextEmpty() {
        Assert.assertEquals("", tools.textSanitizer(""));
    }

    @Test
    public void testTextNull() {
        Assert.assertEquals("", tools.textSanitizer(null));
    }

    @Test
    public void testTextWhitespaceOnly() {
        Assert.assertEquals("", tools.textSanitizer(" "));
    }

}
