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

    public static Optional<SupportedLanguage> get(final Locale locale) {
        if (locale == null) {
            return Optional.of(SupportedLanguage.EN);
        }
        return get(locale.getLanguage());
    }

    public static Optional<SupportedLanguage> get(final String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return Optional.of(SupportedLanguage.EN);
        }

        String substLang = langCode;
        if (langCode.length() > 2) {
            // just get the language tag
            String[] parts = langCode.split("-");
            if (parts.length > 0) {
                substLang = parts[0];
            }
        }

        final String langTag = substLang;
        return Arrays.stream(SupportedLanguage.values())
                .filter(x -> langTag.equalsIgnoreCase(x.toString()))
                .findFirst();
    }

    public Locale toLocale() {
        Locale locale = new Locale(this.toString());
        return locale;
    }
}

