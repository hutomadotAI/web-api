package com.hutoma.api.common;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SupportedLanguage {
    EN,
    ES,
    PT,
    FR,
    NL,
    IT,
    ;

    public static Optional<SupportedLanguage> get(final String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return Optional.of(SupportedLanguage.EN);
        }
        return getLanguage(langCode);
    }

    public static Optional<SupportedLanguage> get(final Locale locale) {
        if (locale == null) {
            return Optional.of(SupportedLanguage.EN);
        }
        return get(locale.getLanguage());
    }

    private static Optional<SupportedLanguage> getLanguage(final String language) {
        String substLang = language;
        if (language.length() > 2) {
            // just get the language tag
            String[] parts = language.split("-");
            if (parts.length > 0) {
                substLang = parts[0];
            }
        }

        final String langTag = substLang;
        return Arrays.stream(SupportedLanguage.values())
                .filter(x -> langTag.equalsIgnoreCase(x.toString()))
                .findFirst();
    }
}

