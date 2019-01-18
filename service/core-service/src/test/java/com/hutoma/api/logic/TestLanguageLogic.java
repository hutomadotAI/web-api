package com.hutoma.api.logic;

import com.hutoma.api.common.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
        boolean result = this.languageLogic.isLocaleAvailable(Locale.ENGLISH, null, null);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testEnLanguageValid() {
        boolean result = this.languageLogic.isLanguageAvailable("en", null, null);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testNullLocaleInvalid() {
        boolean result = this.languageLogic.isLocaleAvailable(null, null, null);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testNullLanguageInvalid() {
        boolean result = this.languageLogic.isLanguageAvailable(null, null, null);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testFrLocaleInvalid() {
        boolean result = this.languageLogic.isLocaleAvailable(Locale.FRENCH, null, null);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testFrLanguageInvalid() {
        boolean result = this.languageLogic.isLanguageAvailable("fr", null, null);
        Assert.assertEquals(false, result);
    }
}

