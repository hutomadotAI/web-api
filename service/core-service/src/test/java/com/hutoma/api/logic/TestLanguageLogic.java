package com.hutoma.api.logic;

import com.hutoma.api.common.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the AI logic.
 */
public class TestLanguageLogic {

    private LanguageLogic languageLogic;

    private Config fakeConfig;
    private FeatureToggler fakeFeatureToggler;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);

        when(this.fakeConfig.getLanguagesAvailable()).thenReturn(Arrays.asList("en", "es"));
        this.languageLogic = new LanguageLogic(this.fakeConfig,
                this.fakeFeatureToggler);
    }

    @Test
    public void testEnLocaleValid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage(Locale.ENGLISH, null, null);
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), result);
    }

    @Test
    public void testEnLanguageValid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage("en", null, null);
        Assert.assertEquals(Optional.of(SupportedLanguage.EN), result);
    }

    @Test
    public void testNullLocaleInvalid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage((Locale) null, null, null);
        Assert.assertEquals(Optional.empty(), result);
    }

    @Test
    public void testNullLanguageInvalid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage((String) null, null, null);
        Assert.assertEquals(Optional.empty(), result);
    }

    @Test
    public void testFrLocaleInvalid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage(Locale.FRENCH, null, null);
        Assert.assertEquals(Optional.empty(), result);
    }

    @Test
    public void testFrLanguageInvalid() {
        Optional<SupportedLanguage> result = this.languageLogic.getAvailableLanguage("fr", null, null);
        Assert.assertEquals(Optional.empty(), result);
    }
}

