package com.hutoma.api.validation;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 06/09/2016.
 */
@RunWith(DataProviderRunner.class)
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
        } catch (ParameterValidationException e) {
            e.printStackTrace();
        }
        return fakeValidation;
    }

    @DataProvider
    public static Object[] dataProviderTopicDisallowedChars() {
        return new Object[]{
                "@"
        };
    }

    @DataProvider
    public static Object[] dataProviderTimezones() {
        return new Object[]{
                "America/Argentina/ComodRivadavia",
                "Pacific/Port_Moresby",
                "Etc/GMT",
                "GMT",
                "UTC",
                "GMT0",
                "Iceland",
                "Zulu"
        };
    }

    @Before
    public void setup() {
        this.example = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
        this.validation = new Validate();
    }

    @Test
    public void testUUIDNormal() throws ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e"));
    }

    @Test
    public void testUUIDUpper() throws ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "41c6e949-4733-42d8-bfcf-95192131137e".toUpperCase()));
    }

    @Test
    public void testUUIDPadded() throws ParameterValidationException {
        Assert.assertEquals(this.example, this.validation.validateUuid("uuid", "            41c6e949-4733-42d8-bfcf-95192131137e "));
    }

    @Test(expected = ParameterValidationException.class)
    public void testUUIDBroken() throws ParameterValidationException {
        assertFailUuid(" 41c6e949-4733 -42d8-bfcf-95192131137e");
    }

    @Test(expected = ParameterValidationException.class)
    public void testUUIDShort() throws ParameterValidationException {
        assertFailUuid("41c6e949-4733-42d8-bfcf-95192131137");
    }

    @Test
    public void testDevidNormal() throws ParameterValidationException {
        String test = "DEMO24869e07-0d0f-4f37-b2fa-c8bf2b7130dd";
        Assert.assertEquals(test, this.validation.validateAlphaNumPlusDashes("devid", test));
    }

    @Test
    public void testDevidTrim() throws ParameterValidationException {
        Assert.assertEquals("Test123", this.validation.validateAlphaNumPlusDashes("devid", " Test123 "));
    }

    @Test(expected = ParameterValidationException.class)
    public void testDevidSpace() throws ParameterValidationException {
        assertFailDevid("test 123");
    }

    @Test(expected = ParameterValidationException.class)
    public void testDevidBrackets() throws ParameterValidationException {
        assertFailDevid("Test(not)");
    }

    @Test(expected = ParameterValidationException.class)
    public void testDevidEmpty() throws ParameterValidationException {
        assertFailDevid("");
    }

    @Test(expected = ParameterValidationException.class)
    public void testDevidNull() throws ParameterValidationException {
        assertFailDevid(null);
    }

    @Test
    public void testAiDescNormal() throws ParameterValidationException {
        String test = "123 tester !$%&().,";
        Assert.assertEquals(test, this.validation.validateOptionalDescription("desc", test));
    }

    @Test
    public void testAiDescAllowEmpty() throws ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", ""));
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", "   \t"));
        Assert.assertEquals("", this.validation.validateOptionalDescription("desc", null));
    }

    @Test
    public void testAiDescQuotes() throws ParameterValidationException {
        assertFailAiDesc("\'");
    }

    @Test
    public void testAiDescDoubleQuotes() throws ParameterValidationException {
        assertFailAiDesc("\"");
    }

    @Test
    public void testAiDescWavyBrackets() throws ParameterValidationException {
        assertFailAiDesc("{");
    }

    @Test
    public void testChatNormal() throws ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, this.validation.validateRequiredSanitized("chat", test));
    }

    @Test
    public void testChatSpaced() throws ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateRequiredSanitized("chat", "    123    "));
    }

    @Test
    public void testChatDisallowedCharacters() throws ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateRequiredSanitized("chat", "\t\n123\t"));
    }

    @Test(expected = ParameterValidationException.class)
    public void testChatEmpty() throws ParameterValidationException {
        assertFailChat("");
        assertFailChat(null);
    }

    @Test(expected = ParameterValidationException.class)
    public void testChatNull() throws ParameterValidationException {
        assertFailChat(null);
    }

    @Test
    public void testHistoryEmpty() throws ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalSanitized(""));
        Assert.assertEquals("", this.validation.validateOptionalSanitized("   \t"));
        Assert.assertEquals("", this.validation.validateOptionalSanitized(null));
    }

    @Test
    public void testTopicNormal() throws ParameterValidationException {
        String test = "123 tester +- !$%().,_?";
        Assert.assertEquals(test, this.validation.validateOptionalSanitizeRemoveAt("topic", test));
    }

    @Test
    public void testTopicSpaced() throws ParameterValidationException {
        Assert.assertEquals("123", this.validation.validateOptionalSanitizeRemoveAt("topic", "    123    "));
    }

    @Test(expected = ParameterValidationException.class)
    @UseDataProvider("dataProviderTopicDisallowedChars")
    public void testTopicDisallowedCharacters(String chars) throws ParameterValidationException {
        assertFailTopic(chars);
    }

    @Test
    public void testTopicEmpty() throws ParameterValidationException {
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", ""));
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", "   \t"));
        Assert.assertEquals("", this.validation.validateOptionalSanitizeRemoveAt("topic", null));
    }

    @Test
    public void testFloatNormal() throws ParameterValidationException {
        assertFloat(0.554f, "0.554");
    }

    @Test
    public void testFloatLow() throws ParameterValidationException {
        assertFloat(0.0f, "0");
    }

    @Test
    public void testFloatHigh() throws ParameterValidationException {
        assertFloat(1.0f, "1");
    }

    @Test
    public void testFloatEmpty() throws ParameterValidationException {
        assertFloat(-1.0f, "");
        assertFloat(-1.0f, null);
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatSpacesFail() throws ParameterValidationException {
        assertFloatFail("   ");
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatEOLFail() throws ParameterValidationException {
        assertFloatFail("\n");
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatFailText() throws ParameterValidationException {
        assertFloatFail("t");
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatFailPunctuation() throws ParameterValidationException {
        assertFloatFail("0:0");
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatFailOutOfRangeLow() throws ParameterValidationException {
        assertFloatFail("-0.1");
    }

    @Test(expected = ParameterValidationException.class)
    public void testFloatFailOutOfRangeHigh() throws ParameterValidationException {
        assertFloatFail("31415926");
    }

    @Test
    public void testFloatInternational() throws ParameterValidationException {
        assertFloat(0.22f, "0,22");
    }

    @Test
    public void testLocaleLangCountry() throws ParameterValidationException {
        Assert.assertEquals(new Locale("fr", "CA"), this.validation.validateLocale("locale", "fr-CA"));
    }

    @Test
    public void testLocaleLang() throws ParameterValidationException {
        Assert.assertEquals(new Locale("en"), this.validation.validateLocale("locale", "en"));
    }

    @Test(expected = ParameterValidationException.class)
    public void testLocaleInvalidFormat() throws ParameterValidationException {
        this.validation.validateLocale("locale", "notALocale");
    }

    @Test(expected = ParameterValidationException.class)
    public void testLocaleInvalidLocale() throws ParameterValidationException {
        this.validation.validateLocale("locale", "xb-BX");
    }

    @Test(expected = ParameterValidationException.class)
    public void testLocaleEmpty() throws ParameterValidationException {
        this.validation.validateLocale("locale", "");
    }

    @Test(expected = ParameterValidationException.class)
    public void testLocaleNull() throws ParameterValidationException {
        this.validation.validateLocale("locale", null);
    }

    @Test
    @UseDataProvider("dataProviderTimezones")
    public void validateTimezoneString(final String tz) throws ParameterValidationException {
        Assert.assertEquals(tz, this.validation.validateTimezoneString("tz", tz));
    }

    @Test(expected = ParameterValidationException.class)
    public void validateTimezoneInvalid() throws ParameterValidationException {
        this.validation.validateTimezoneString("tz", "Nowhere/MiddleOf");
    }

    @Test(expected = ParameterValidationException.class)
    public void validateTimezoneNull() throws ParameterValidationException {
        this.validation.validateTimezoneString("tz", null);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateTimezoneEmpty() throws ParameterValidationException {
        this.validation.validateTimezoneString("tz", "");
    }

    private void assertFailUuid(String param) throws ParameterValidationException {
        this.validation.validateUuid("uuid", param);
    }

    private void assertFailDevid(String param) throws ParameterValidationException {
        this.validation.validateAlphaNumPlusDashes("devid", param);
    }

    private void assertFailAiDesc(String param) throws ParameterValidationException {
        this.validation.validateOptionalDescription("desc", param);
    }

    private void assertFailChat(String param) throws ParameterValidationException {
        this.validation.validateRequiredSanitized("chat", param);
    }

    private void assertFailTopic(String param) throws ParameterValidationException {
        this.validation.validateOptionalSanitizeRemoveAt("topic", param);
    }

    private void assertFloatFail(String actual) throws ParameterValidationException {
        this.validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual);
    }

    private void assertFloat(float expected, String actual) throws ParameterValidationException {
        Assert.assertEquals(expected, this.validation.validateOptionalFloat("float", 0.0f, 1.0f, -1.0f, actual), 0.01f);
    }
}
