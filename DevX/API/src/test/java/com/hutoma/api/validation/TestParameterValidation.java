package com.hutoma.api.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 06/09/2016.
 */
public class TestParameterValidation {

    Validate validation;
    UUID example;

    public static Validate getFakeValidation() {
        Validate fakeValidation = mock(Validate.class);
        try {
            when(fakeValidation.validateAlphaNumPlusDashes(anyString(), anyString())).thenReturn("devid");
            when(fakeValidation.validateUuid(anyString(), anyString())).thenReturn(UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e"));
            when(fakeValidation.validateAlphaNumPlusDashes(anyString(), anyString())).thenReturn("ainame");
            when(fakeValidation.validateOptionalDescription(anyString(), anyString())).thenReturn("aidesc");
        } catch (Validate.ParameterValidationException e) {
            e.printStackTrace();
        }
        return  fakeValidation;
    }

    @Before
    public void setup() {
        example = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
        validation = new Validate();
    }

    private void assertFailUuid(String param) {
        try {
            validation.validateUuid("uuid", param);
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    @Test
    public void testUUIDNormal() throws Validate.ParameterValidationException {
        Assert.assertEquals(example, validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e"));
    }

    @Test
    public void testUUIDUpper() throws Validate.ParameterValidationException {
        Assert.assertEquals(example, validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e".toUpperCase()));
    }

    @Test
    public void testUUIDPadded() throws Validate.ParameterValidationException {
        Assert.assertEquals(example, validation.validateUuid("uuid", "            41c6e949-4733-42d8-bfcf-95192131137e "));
    }

    @Test
    public void testUUIDBroken() throws Validate.ParameterValidationException {
        assertFailUuid(" 41c6e949-4733 -42d8-bfcf-95192131137e");
    }

    @Test
    public void testUUIDShort() throws Validate.ParameterValidationException {
        assertFailUuid("41c6e949-4733-42d8-bfcf-95192131137");
    }

    private void assertFailDevid(String param) {
        try {
            validation.validateAlphaNumPlusDashes("devid", param);
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    @Test
    public void testDevidNormal() throws Validate.ParameterValidationException {
        String test = "DEMO24869e07-0d0f-4f37-b2fa-c8bf2b7130dd";
        Assert.assertEquals(test, validation.validateAlphaNumPlusDashes("devid", test));
    }

    @Test
    public void testDevidTrim() throws Validate.ParameterValidationException {
        Assert.assertEquals("Test123", validation.validateAlphaNumPlusDashes("devid", " Test123 "));
    }

    @Test
    public void testDevidSpace() throws Validate.ParameterValidationException {
        assertFailDevid("test 123");
    }

    @Test
    public void testDevidBrackets() throws Validate.ParameterValidationException {
        assertFailDevid("Test(not)");
    }

    @Test
    public void testDevidEmptyNull() throws Validate.ParameterValidationException {
        assertFailDevid("");
        assertFailDevid(null);
    }

    private void assertFailAiDesc(String param) {
        try {
            validation.validateOptionalDescription("desc", param);
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    @Test
    public void testAiDescNormal() throws Validate.ParameterValidationException {
        String test = "123 tester !£$%&().,";
        Assert.assertEquals(test, validation.validateOptionalDescription("desc", test));
    }

    @Test
    public void testAiDescAllowEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", validation.validateOptionalDescription("desc", ""));
        Assert.assertEquals("", validation.validateOptionalDescription("desc", "   \t"));
        Assert.assertEquals("", validation.validateOptionalDescription("desc", null));
    }

    @Test
    public void testAiDescQuotes() throws Validate.ParameterValidationException {
        assertFailAiDesc("\'");
    }

    @Test
    public void testAiDescDoubleQuotes() throws Validate.ParameterValidationException {
        assertFailAiDesc("\"");
    }

    @Test
    public void testAiDescWavyBrackets() throws Validate.ParameterValidationException {
        assertFailAiDesc("{");
    }
    
    private void assertFailChat(String param) {
        try {
            validation.validateRequiredSanitized("chat", param);
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    @Test
    public void testChatNormal() throws Validate.ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, validation.validateRequiredSanitized("chat", test));
    }

    @Test
    public void testChatSpaced() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", validation.validateRequiredSanitized("chat", "    123    "));
    }

    @Test
    public void testChatDisallowedCharacters() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", validation.validateRequiredSanitized("chat", "\t\n123£"));
    }

    @Test
    public void testChatEmpty() throws Validate.ParameterValidationException {
        assertFailChat("");
        assertFailChat(null);
    }

    @Test
    public void testHistoryEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", validation.validateOptionalSanitized(""));
        Assert.assertEquals("", validation.validateOptionalSanitized("   \t"));
        Assert.assertEquals("", validation.validateOptionalSanitized(null));
    }

    private void assertFailTopic(String param) {
        try {
            validation.validateOptionalSanitizeRemoveAt("topic", param);
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    @Test
    public void testTopicNormal() throws Validate.ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, validation.validateOptionalSanitizeRemoveAt("topic", test));
    }

    @Test
    public void testTopicSpaced() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", validation.validateOptionalSanitizeRemoveAt("topic", "    123    "));
    }

    @Test
    public void testTopicDisallowedCharacters() throws Validate.ParameterValidationException {
        assertFailTopic("@");
        assertFailTopic("<");
    }
    @Test
    public void testTopicDisallowedBrackets() throws Validate.ParameterValidationException {
        assertFailTopic("]");
    }

    @Test
    public void testTopicEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", validation.validateOptionalSanitizeRemoveAt("topic", ""));
        Assert.assertEquals("", validation.validateOptionalSanitizeRemoveAt("topic", "   \t"));
        Assert.assertEquals("", validation.validateOptionalSanitizeRemoveAt("topic", null));
    }

    private void assertFloatFail(String actual) {
        try {
            float res = validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual).floatValue();
            Assert.fail("should have thrown");
        } catch (Validate.ParameterValidationException e) {
        }
    }

    private void assertFloat(float expected, String actual) throws Validate.ParameterValidationException {
        Assert.assertEquals(expected, validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual).floatValue(), 0.01f);
    }

    @Test
    public void testFloatNormal() throws Validate.ParameterValidationException {
        assertFloat(0.554f, "0.554");
    }

    @Test
    public void testFloatLow() throws Validate.ParameterValidationException {
        assertFloat(0.0f, "0");
    }

    @Test
    public void testFloatHigh() throws Validate.ParameterValidationException {
        assertFloat(1.0f, "1");
    }

    @Test
    public void testFloatEmpty() throws Validate.ParameterValidationException {
        assertFloat(-1.0f, "");
        assertFloat(-1.0f, null);
    }

    @Test
    public void testFloatSpacesFail() throws Validate.ParameterValidationException {
        assertFloatFail("   ");
        assertFloatFail("\n");
    }

    @Test
    public void testFloatFailText() throws Validate.ParameterValidationException {
        assertFloatFail("t");
    }

    @Test
    public void testFloatFailPunctuation() throws Validate.ParameterValidationException {
        assertFloatFail("0:0");
    }

    @Test
    public void testFloatFailOutOfRangeLow() throws Validate.ParameterValidationException {
        assertFloatFail("-0.1");
    }

    @Test
    public void testFloatFailOutOfRangeHigh() throws Validate.ParameterValidationException {
        assertFloatFail("31415926");
    }

    @Test
    public void testFloatInternational() throws Validate.ParameterValidationException {
        assertFloat(0.22f, "0,22");
    }
}
