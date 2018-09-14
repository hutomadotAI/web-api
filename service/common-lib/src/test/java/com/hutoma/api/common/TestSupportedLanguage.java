package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class TestSupportedLanguage {

    @Test
    public void testSupportedAiLanguage_get_default() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get((String)null));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get(""));
    }

    @Test
    public void testSupportedAiLanguage_get_invalidLanguageTag_default() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("  "));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("zz"));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("1234"));
    }

    @Test
    public void testSupportedAiLanguage_get_validLanguageTag() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("en"));
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get("es"));
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get("eS"));
    }

    @Test
    public void testSupportedAiLanguage_get_locale() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get(Locale.ENGLISH));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get(Locale.UK));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get(Locale.CANADA));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get((Locale)null));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get(Locale.forLanguageTag("en")));
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get(new Locale("es")));
    }

    @Test
    public void testSupportedAiLanguage_get_locale_substitutions() {
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get(new Locale("ca")));
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get(new Locale("ca", "ES")));
    }

    @Test
    public void testSupportedAiLanguage_get_localeAsString() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("en-US"));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("en-CA"));
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get("es-ES"));
    }

    @Test
    public void testSupportedAiLanguage_get_localeAsString_substitutions() {
        Assert.assertEquals(SupportedLanguage.ES, SupportedLanguage.get("ca-ES"));
    }

    @Test
    public void testSupportedAiLanguage_get_localeAsString_fallback() {
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("XX-KK"));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("zh-CN"));
        Assert.assertEquals(SupportedLanguage.EN, SupportedLanguage.get("zh-PT"));
    }

    @Test
    public void testSupportedAiLanguage_deserialize() {
        JsonSerializer serializer = new JsonSerializer();
        Assert.assertEquals(SupportedLanguage.ES, serializer.deserialize("ES", SupportedLanguage.class));
        Assert.assertEquals(SupportedLanguage.ES, serializer.deserialize("es", SupportedLanguage.class));
    }
}
