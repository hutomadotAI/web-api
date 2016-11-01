package com.hutoma.api.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Singleton;

/**
 * Created by pedrotei on 29/09/16.
 */
@Singleton
public class TelemetryLogger extends Logger implements ITelemetry {

    /**
     * Telemetry app name for config file.
     */
    private static final String APP_NAME = "api";
    /**
     * The telemetry client.
     */
    private TelemetryClient telemetryClient;
    /**
     * Whether the telemetry is enabled or disabled.
     */
    private boolean isTelemetryEnabled;

    /**
     * Sanitize the property map since AppInsights uses ConcurrentHashMap which doesn't
     * support null values.
     * @param map the map
     */
    private static Map<String, String> sanitizePropertiesMap(final Map<String, String> map) {
        if (map.values().stream().anyMatch(x -> x == null)) {
            final Map<String, String> otherMap = new HashMap<>();
            for (Entry<String, String> entry : map.entrySet()) {
                otherMap.put(entry.getValue(), entry.getValue() == null ? "" : entry.getValue());
            }
            return otherMap;
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName) {
        this.addTelemetryEvent(eventName, (Map<String, String>) null);
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Map<String, String> properties) {
        if (this.isTelemetryEnabled()) {
            this.telemetryClient.trackEvent(eventName, sanitizePropertiesMap(properties), null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Exception exception) {
        this.addTelemetryEvent(eventName, exception, null);
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Exception exception, Map<String, String> properties) {
        if (this.isTelemetryEnabled()) {
            Map<String, String> map = new HashMap<>();
            if (properties != null) {
                map.putAll(properties);

            }
            // Add the exception properties
            map.put("ExceptionName", exception.getClass().getSimpleName());
            map.put("ExceptionMessage", exception.getMessage());
            map.put("ExceptionStackTrace", getStackTraceAsString(exception.getStackTrace()));

            this.telemetryClient.trackEvent(eventName, sanitizePropertiesMap(map), null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void trackTelemetryMetric(String metricName, int sampleCount) {
        if (this.isTelemetryEnabled()) {
            this.telemetryClient.trackMetric(metricName, sampleCount);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void enableTelemetry() {
        this.isTelemetryEnabled = true;
    }

    /**
     * {@inheritDoc}
     */
    public void disableTelemetry() {
        this.isTelemetryEnabled = false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTelemetryEnabled() {
        return this.isTelemetryEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(String fromLabel, String logComment) {
        logOutput(Level.ERROR, fromLabel, logComment);
        // Log to any errors to telemetry
        if (this.isTelemetryEnabled()) {
            this.addTelemetryEvent("ApplicationError", new HashMap<String, String>() {{
                this.put("Comment", logComment);
            }});
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(Config config) {
        String key = config.getTelemetryKey(APP_NAME);
        if (key != null) {
            TelemetryConfiguration telemetryConfig = TelemetryConfiguration.getActive();
            telemetryConfig.setInstrumentationKey(key);
            this.telemetryClient = new TelemetryClient(telemetryConfig);
            this.isTelemetryEnabled = true;
            this.logInfo("telemetrylogger", String.format("Initialized telemetry for key: [%s]", key));
        } else {
            this.isTelemetryEnabled = false;
        }
    }

    private String getStackTraceAsString(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : stackTrace) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
    }
}
