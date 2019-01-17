package com.hutoma.api.common;

import java.util.Arrays;
import java.util.Locale;

public enum SupportedLanguage {
    EN,
    ES,
    PT,
    FR,
    NL,
    IT,
    // CA (catalan) -> falls back to ES
    ;

    public static SupportedLanguage get(final String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return SupportedLanguage.EN;
        }
        return getLanguageWithFallbacks(langCode);
    }

    public static SupportedLanguage get(final Locale locale) {
        if (locale == null) {
            return SupportedLanguage.EN;
        }
        return get(locale.getLanguage());
    }

    private static SupportedLanguage getLanguageWithFallbacks(final String language) {
        String substLang = language;
        if (language.length() > 2) {
            // just get the language tag
            String[] parts = language.split("-");
            if (parts.length > 0) {
                substLang = parts[0];
            }
        }

        switch (substLang.toLowerCase()) {
            case "ca":
                substLang = "es";
                break;
            default:
                break;
        }

        final String langTag = substLang;
        return Arrays.stream(SupportedLanguage.values())
                .filter(x -> langTag.equalsIgnoreCase(x.toString()))
                .findFirst()
                .orElse(SupportedLanguage.EN);
    }
}

