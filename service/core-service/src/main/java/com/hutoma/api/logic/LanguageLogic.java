package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.FeatureToggler.FeatureState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.HashSet;
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
    private static final Set<SupportedLanguage> GENERAL_AVAILABILITY_LANGUAGES = new HashSet<>(
            Arrays.asList(SupportedLanguage.EN));
    
    private final ILogger logger;
    private final Config config;
    private final FeatureToggler featureToggler;
    private Set<SupportedLanguage> systemAvailableLanguages;

    @Inject
    public LanguageLogic(final ILogger logger, final Config config, final FeatureToggler featureToggler) {
        this.logger = logger;
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
        initSystemAvailableLanguages();
        if (langCode == null || langCode.isEmpty()) {
            return Optional.empty();
        }

        Optional<SupportedLanguage> supportedLanguageOpt = SupportedLanguage.get(langCode);
        if (!supportedLanguageOpt.isPresent()) {
            return Optional.empty();
        }
        SupportedLanguage supportedLanguage = supportedLanguageOpt.get();
        if (!systemAvailableLanguages.contains(supportedLanguage)) {
            return Optional.empty();
        }
        if (GENERAL_AVAILABILITY_LANGUAGES.contains(supportedLanguage)) {
            return supportedLanguageOpt;
        }
                
        String devIdString = devId != null ? devId.toString() : "null";
        String languageName = supportedLanguage.toString().toLowerCase();
        LogMap logMap = LogMap.map("Language", languageName)
                              .put("aiid", aiid != null ? aiid.toString() : "null");
        // this language is on the system, but not GA. Check the feature toggle
        String featureName = String.format("language-%s", languageName);
        FeatureState featureState = featureToggler.getStateForAiid(devId, aiid, featureName);
        switch (featureState) {
            case C:
                this.logger.logUserWarnEvent(LOGFROM, "Cannot use language: " + languageName,
                    devIdString, logMap);
                return Optional.empty();
            case T1:
                return supportedLanguageOpt;
            default:
                this.logger.logUserErrorEvent(LOGFROM, "Invalid toggle state " + languageName,
                    devIdString, logMap.put("toggleState", featureState.toString()));
                return Optional.empty();
        }
    }

    private void initSystemAvailableLanguages() {
        if (systemAvailableLanguages == null) {
            List<String> langs = config.getLanguagesAvailable();
            systemAvailableLanguages = langs.stream()
                .map(SupportedLanguage::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        }
    }
}