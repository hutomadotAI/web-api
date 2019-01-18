package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.SupportedLanguage;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Language logic
 */
public class LanguageLogic {
    private static final String LOGFROM = "languagelogic";
    private final Config config;
    private final FeatureToggler featureToggler;
    private Set<SupportedLanguage> availableLanguages;

    @Inject
    public LanguageLogic(final Config config, final FeatureToggler featureToggler) {
        this.config = config;
        this.featureToggler = featureToggler;
    }

    public Optional<SupportedLanguage> getAvailableLanguage(final Locale locale, final UUID devId, final UUID aiid) {
        if (locale == null) {
            return Optional.empty();
        }
        return getAvailableLanguage(locale.getLanguage(), devId, aiid);
    }

    public Optional<SupportedLanguage> getAvailableLanguage(final String langCode, final UUID devId, final UUID aiid) {
        initAvailableLanguages();
        if (langCode == null || langCode.isEmpty()) {
            return Optional.empty();
        }

        Optional<SupportedLanguage> supportedLanguageOpt = SupportedLanguage.get(langCode);
        if (!supportedLanguageOpt.isPresent()) {
            return Optional.empty();
        }
        SupportedLanguage supportedLanguage = supportedLanguageOpt.get();
        return availableLanguages.contains(supportedLanguage) ? supportedLanguageOpt : Optional.empty();
    }

    private void initAvailableLanguages() {
        if (availableLanguages == null) {
            List<String> langs = config.getLanguagesAvailable();
            availableLanguages = langs.stream()
                .map(SupportedLanguage::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        }
    }
}