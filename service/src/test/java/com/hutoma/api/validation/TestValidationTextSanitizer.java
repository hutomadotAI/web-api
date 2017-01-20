package com.hutoma.api.validation;

import com.hutoma.api.validation.Validate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by David MG on 10/08/2016.
 */
public class TestValidationTextSanitizer {

    Validate validation;

    @Before
    public void setup() {
        validation = new Validate();
    }

    @Test
    public void testTextNormal() {
        Assert.assertEquals("abcXYZ", validation.textSanitizer("abcXYZ"));
    }

    @Test
    public void testTextTrim() {
        Assert.assertEquals("abcXYZ", validation.textSanitizer(" abcXYZ "));
    }

    @Test
    public void testTextTrimLots() {
        Assert.assertEquals("abcXYZ", validation.textSanitizer("            abcXYZ             "));
    }

    @Test
    public void testTextDedupeWhitespace() {
        Assert.assertEquals("a b cX Y Z", validation.textSanitizer("a   b cX\tY\t\t\tZ"));
    }

    /***
     * Test now ensures that only round parentheses are retained
     */
    @Test
    public void testTextRemoveInvalidParentheses() {
        Assert.assertEquals("(abc)XYZ", validation.textSanitizer("(abc)[X]<YZ>"));
    }

    /***
     * Removed the requirement to escape quotes.
     * This test now checks that quotes remain intact
     */
    @Test
    public void testTextNotEscapedQuotes() {
        Assert.assertEquals("\'abcXYZ\"", validation.textSanitizer("\'abcXYZ\""));
    }

    @Test
    public void testTextRemoveNonAscii() {
        Assert.assertEquals("abcXYZ", validation.textSanitizer("abc日本語XYZ"));
    }

    @Test
    public void testTextEmpty() {
        Assert.assertEquals("", validation.textSanitizer(""));
    }

    @Test
    public void testTextNull() {
        Assert.assertEquals("", validation.textSanitizer(null));
    }

    @Test
    public void testTextWhitespaceOnly() {
        Assert.assertEquals("", validation.textSanitizer(" "));
    }

    @Test
    public void testTextWithAmpersand() {
        Assert.assertEquals("hello", validation.textSanitizer("&hello"));
    }

}
