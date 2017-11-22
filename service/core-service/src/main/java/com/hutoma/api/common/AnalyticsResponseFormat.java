package com.hutoma.api.common;

/**
 * Created by pedrotei on 07/07/17.
 */
public enum AnalyticsResponseFormat {
    JSON("JSON"),
    CSV("CSV");
    private String name;

    AnalyticsResponseFormat(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static AnalyticsResponseFormat forName(final String name) {
        for (AnalyticsResponseFormat f : AnalyticsResponseFormat.values()) {
            if (f.getName().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return null;
    }
}
