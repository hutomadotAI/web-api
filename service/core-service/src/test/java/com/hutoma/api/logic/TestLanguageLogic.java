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

        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(5);
        when(this.fakeConfig.getMaxTotalEntityValues()).thenReturn(200);
        when(this.fakeConfig.getMaxEntityValuesPerEntity()).thenReturn(100);
        this.languageLogic = new LanguageLogic(this.fakeConfig,
                this.fakeFeatureToggler);
    }

    @Test
    public void testEnValid() {
        when(this.fakeConfig.getLanguagesAvailable()).thenReturn(Arrays.asList("en"));
        boolean result = this.languageLogic.isLanguageAvailable(Locale.ENGLISH, null, null);
        Assert.assertEquals(true, result);
    }

}

