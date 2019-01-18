package com.hutoma.api.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.Optional;

public class TestSupportedLanguage {

    @Test
    public void testSupportedAiLanguage_get_default() {
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get((String)null));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get(""));
    }

    @Test
    public void testSupportedAiLanguage_get_invalidLanguageTag_invalid() {
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("  "));
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("zz"));
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("1234"));
    }

    @Test
    public void testSupportedAiLanguage_get_validLanguageTag() {
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get("en"));
        Assert.assertEquals(Optional.of(SupportedLanguage.ES), SupportedLanguage.get("es"));
        Assert.assertEquals(Optional.of(SupportedLanguage.ES), SupportedLanguage.get("eS"));
    }

    @Test
    public void testSupportedAiLanguage_get_locale() {
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get(Locale.ENGLISH));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get(Locale.UK));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get(Locale.CANADA));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get((Locale)null));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get(Locale.forLanguageTag("en")));
        Assert.assertEquals(Optional.of(SupportedLanguage.ES), SupportedLanguage.get(new Locale("es")));
    }

    @Test
    public void testSupportedAiLanguage_no_locale_substitutions() {
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get(new Locale("ca")));
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get(new Locale("ca", "ES")));
    }

    @Test
    public void testSupportedAiLanguage_get_localeAsString() {
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get("en-US"));
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), SupportedLanguage.get("en-CA"));
        Assert.assertEquals(Optional.of(SupportedLanguage.ES), SupportedLanguage.get("es-ES"));
    }

    @Test
    public void testSupportedAiLanguage_no_localeAsString_substitutions() {
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("ca-ES"));
    }

    @Test
    public void testSupportedAiLanguage_no_localeAsString_fallback() {
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("XX-KK"));
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("zh-CN"));
        Assert.assertEquals(Optional.empty(), SupportedLanguage.get("zh-PT"));
    }

    @Test
    public void testSupportedAiLanguage_deserialize() {
        JsonSerializer serializer = new JsonSerializer();
        Assert.assertEquals(SupportedLanguage.ES, serializer.deserialize("ES", SupportedLanguage.class));
        Assert.assertEquals(SupportedLanguage.ES, serializer.deserialize("es", SupportedLanguage.class));
    }
}
