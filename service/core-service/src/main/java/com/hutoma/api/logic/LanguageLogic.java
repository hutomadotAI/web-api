package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.SupportedLanguage;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Language logic
 */
public class LanguageLogic {
    private static final String LOGFROM = "languagelogic";
    private final Config config;
    private final FeatureToggler featureToggler;
    private List<SupportedLanguage> availableLanguages;

    @Inject
    public LanguageLogic(final Config config, final FeatureToggler featureToggler) {
        this.config = config;
        this.featureToggler = featureToggler;
    }

    public Boolean isLanguageAvailable(final Locale lang, final UUID devId, final UUID aiid) {
        return true;
    }
}