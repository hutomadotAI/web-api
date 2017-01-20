package com.hutoma.api.validation;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.UUID;

import static junitparams.JUnitParamsRunner.$;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 06/09/2016.
 */
@RunWith(JUnitParamsRunner.class)
public class TestParameterValidation {

    private Validate validation;
    private UUID example;

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
        return fakeValidation;
    }

    private static Object[] dataProviderTopicDisallowedChars() {
        return $(
                $("@"),
                $("<")
        );
    }

    private static Object[] dataProviderTimezones() {
        return $(
                $("America/Argentina/ComodRivadavia"),
                $("Pacific/Port_Moresby"),
                $("Etc/GMT"),
                $("GMT"),
                $("UTC"),
                $("GMT0"),
                $("Iceland"),
                $("Zulu")
        );
    }

    @Before
    public void setup() {
        this.example = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
        this.validation = new Validate();
    }

    @Test
    public void testUUIDNormal() throws Validate.ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e"));
    }

    @Test
    public void testUUIDUpper() throws Validate.ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e".toUpperCase()));
    }

    @Test
    public void testUUIDPadded() throws Validate.ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "            41c6e949-4733-42d8-bfcf-95192131137e "));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testUUIDBroken() throws Validate.ParameterValidationException {
        assertFailUuid(" 41c6e949-4733 -42d8-bfcf-95192131137e");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testUUIDShort() throws Validate.ParameterValidationException {
        assertFailUuid("41c6e949-4733-42d8-bfcf-95192131137");
    }

    @Test
    public void testDevidNormal() throws Validate.ParameterValidationException {
        String test = "DEMO24869e07-0d0f-4f37-b2fa-c8bf2b7130dd";
        Assert.assertEquals(test, this.validation.validateAlphaNumPlusDashes("devid", test));
    }

    @Test
    public void testDevidTrim() throws Validate.ParameterValidationException {
        Assert.assertEquals("Test123", this.validation.validateAlphaNumPlusDashes("devid", " Test123 "));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testDevidSpace() throws Validate.ParameterValidationException {
        assertFailDevid("test 123");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testDevidBrackets() throws Validate.ParameterValidationException {
        assertFailDevid("Test(not)");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testDevidEmpty() throws Validate.ParameterValidationException {
        assertFailDevid("");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testDevidNull() throws Validate.ParameterValidationException {
        assertFailDevid(null);
    }

    @Test
    public void testAiDescNormal() throws Validate.ParameterValidationException {
        String test = "123 tester !£$%&().,";
        Assert.assertEquals(test, this.validation.validateOptionalDescription("desc", test));
    }

    @Test
    public void testAiDescAllowEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", ""));
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", "   \t"));
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", null));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testAiDescQuotes() throws Validate.ParameterValidationException {
        assertFailAiDesc("\'");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testAiDescDoubleQuotes() throws Validate.ParameterValidationException {
        assertFailAiDesc("\"");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testAiDescWavyBrackets() throws Validate.ParameterValidationException {
        assertFailAiDesc("{");
    }

    @Test
    public void testChatNormal() throws Validate.ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, this.validation.validateRequiredSanitized("chat", test));
    }

    @Test
    public void testChatSpaced() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateRequiredSanitized("chat", "    123    "));
    }

    @Test
    public void testChatDisallowedCharacters() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateRequiredSanitized("chat", "\t\n123£"));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testChatEmpty() throws Validate.ParameterValidationException {
        assertFailChat("");
        assertFailChat(null);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testChatNull() throws Validate.ParameterValidationException {
        assertFailChat(null);
    }

    @Test
    public void testHistoryEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalSanitized(""));
        Assert.assertEquals("", this.validation.validateOptionalSanitized("   \t"));
        Assert.assertEquals("", this.validation.validateOptionalSanitized(null));
    }

    @Test
    public void testTopicNormal() throws Validate.ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, this.validation.validateOptionalSanitizeRemoveAt("topic", test));
    }

    @Test
    public void testTopicSpaced() throws Validate.ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateOptionalSanitizeRemoveAt("topic", "    123    "));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    @Parameters(method = "dataProviderTopicDisallowedChars")
    public void testTopicDisallowedCharacters(String chars) throws Validate.ParameterValidationException {
        assertFailTopic(chars);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testTopicDisallowedBrackets() throws Validate.ParameterValidationException {
        assertFailTopic("]");
    }

    @Test
    public void testTopicEmpty() throws Validate.ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", ""));
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", "   \t"));
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", null));
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

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatSpacesFail() throws Validate.ParameterValidationException {
        assertFloatFail("   ");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatEOLFail() throws Validate.ParameterValidationException {
        assertFloatFail("\n");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatFailText() throws Validate.ParameterValidationException {
        assertFloatFail("t");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatFailPunctuation() throws Validate.ParameterValidationException {
        assertFloatFail("0:0");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatFailOutOfRangeLow() throws Validate.ParameterValidationException {
        assertFloatFail("-0.1");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testFloatFailOutOfRangeHigh() throws Validate.ParameterValidationException {
        assertFloatFail("31415926");
    }

    @Test
    public void testFloatInternational() throws Validate.ParameterValidationException {
        assertFloat(0.22f, "0,22");
    }

    @Test
    public void testLocaleLangCountry() throws Validate.ParameterValidationException {
        Assert.assertEquals(new Locale("fr", "CA"), this.validation.validateLocale("locale", "fr-CA"));
    }

    @Test
    public void testLocaleLang() throws Validate.ParameterValidationException {
        Assert.assertEquals(new Locale("en"), this.validation.validateLocale("locale", "en"));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testLocaleInvalidFormat() throws Validate.ParameterValidationException {
        this.validation.validateLocale("locale", "notALocale");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testLocaleInvalidLocale() throws Validate.ParameterValidationException {
        this.validation.validateLocale("locale", "xb-BX");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testLocaleEmpty() throws Validate.ParameterValidationException {
        this.validation.validateLocale("locale", "");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void testLocaleNull() throws Validate.ParameterValidationException {
        this.validation.validateLocale("locale", null);
    }

    @Test
    @Parameters(method = "dataProviderTimezones")
    public void validateTimezoneString(final String tz) throws Validate.ParameterValidationException {
        Assert.assertEquals(tz, this.validation.validateTimezoneString("tz", tz));
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateTimezoneInvalid() throws Validate.ParameterValidationException {
        this.validation.validateTimezoneString("tz", "Nowhere/MiddleOf");
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateTimezoneNull() throws Validate.ParameterValidationException {
        this.validation.validateTimezoneString("tz", null);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateTimezoneEmpty() throws Validate.ParameterValidationException {
        this.validation.validateTimezoneString("tz", "");
    }

    private void assertFailUuid(String param) throws Validate.ParameterValidationException {
        this.validation.validateUuid("uuid", param);
    }

    private void assertFailDevid(String param) throws Validate.ParameterValidationException {
        this.validation.validateAlphaNumPlusDashes("devid", param);
    }

    private void assertFailAiDesc(String param) throws Validate.ParameterValidationException {
        this.validation.validateOptionalDescription("desc", param);
    }

    private void assertFailChat(String param) throws Validate.ParameterValidationException {
        this.validation.validateRequiredSanitized("chat", param);
    }

    private void assertFailTopic(String param) throws Validate.ParameterValidationException {
        this.validation.validateOptionalSanitizeRemoveAt("topic", param);
    }

    private void assertFloatFail(String actual) throws Validate.ParameterValidationException {
        this.validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual);
    }

    private void assertFloat(float expected, String actual) throws Validate.ParameterValidationException {
        Assert.assertEquals(expected, this.validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual), 0.01f);
    }
}
