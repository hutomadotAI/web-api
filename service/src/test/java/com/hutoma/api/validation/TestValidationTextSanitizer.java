package com.hutoma.api.validation;

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
        this.validation = new Validate();
    }

    @Test
    public void testTextNormal() {
        Assert.assertEquals("abcXYZ", this.validation.filterControlAndCoalesceSpaces("abcXYZ"));
    }

    @Test
    public void testTextTrim() {
        Assert.assertEquals("abcXYZ", this.validation.filterControlAndCoalesceSpaces(" abcXYZ "));
    }

    @Test
    public void testTextTrimLots() {
        Assert.assertEquals("abcXYZ", this.validation.filterControlAndCoalesceSpaces("            abcXYZ             "));
    }

    @Test
    public void testTextDedupeWhitespace() {
        Assert.assertEquals("a b cX Y Z", this.validation.filterControlAndCoalesceSpaces("a   b cX\tY\t\t\tZ"));
    }

    /***
     * Test now ensures that only round parentheses are retained
     */
    @Test
    public void testTextParenthesesAllowed() {
        Assert.assertEquals("(abc)[X]<YZ>", this.validation.filterControlAndCoalesceSpaces("(abc)[X]<YZ>"));
    }

    /***
     * Removed the requirement to escape quotes.
     * This test now checks that quotes remain intact
     */
    @Test
    public void testTextNotEscapedQuotes() {
        Assert.assertEquals("\'abcXYZ\"", this.validation.filterControlAndCoalesceSpaces("\'abcXYZ\""));
    }

    @Test
    public void testTextNonAsciiAllowed() {
        Assert.assertEquals("abc日本語XYZ", this.validation.filterControlAndCoalesceSpaces("abc日本語XYZ"));
    }

    @Test
    public void testTextEmpty() {
        Assert.assertEquals("", this.validation.filterControlAndCoalesceSpaces(""));
    }

    @Test
    public void testTextNull() {
        Assert.assertEquals("", this.validation.filterControlAndCoalesceSpaces(null));
    }

    @Test
    public void testTextWhitespaceOnly() {
        Assert.assertEquals("", this.validation.filterControlAndCoalesceSpaces(" "));
    }

    @Test
    public void testTextWithAmpersandAllowed() {
        Assert.assertEquals("&hello", this.validation.filterControlAndCoalesceSpaces("&hello"));
    }

    @Test
    public void testTextWithAtSymbolAllowed() {
        Assert.assertEquals("test@test.com", this.validation.filterControlAndCoalesceSpaces("test@test.com"));
    }

}
