package com.hutoma.api.common;

import java.util.Map;

/**
 * Created by pedrotei on 03/10/16.
 */
public interface ITelemetry {

    /**
     * Default method to add a telemetry event.
     * @param eventName the event name
     */
    static void addTelemetryEvent(ILogger logger, String eventName) {
        if (logger instanceof ITelemetry) {
            ((ITelemetry) logger).addTelemetryEvent(eventName);
        }
    }

    /**
     * Default method to add a telemetry event.
     * @param eventName  the event name
     * @param properties the properties map
     */
    static void addTelemetryEvent(ILogger logger, String eventName, Map<String, String> properties) {
        if (logger instanceof ITelemetry) {
            ((ITelemetry) logger).addTelemetryEvent(eventName, properties);
        }
    }

    /**
     * Default method to add a telemetry event.
     * @param eventName the event name
     * @param exception the exception
     */
    static void addTelemetryEvent(ILogger logger, String eventName, Exception exception) {
        if (logger instanceof ITelemetry) {
            ((ITelemetry) logger).addTelemetryEvent(eventName, exception);
        }
    }

    /**
     * Default method to add a telemetry event.
     * @param eventName  the event name
     * @param exception  the exception
     * @param properties the properties map
     */
    static void addTelemetryEvent(ILogger logger, String eventName, Exception exception,
                                  Map<String, String> properties) {
        if (logger instanceof ITelemetry) {
            ((ITelemetry) logger).addTelemetryEvent(eventName, exception, properties);
        }
    }

    /**
     * Add a telemetry event.
     * @param eventName the event name
     */
    void addTelemetryEvent(String eventName);

    /**
     * Tracks a metric.
     * @param metricName  the metric name
     * @param sampleCount tehe sample count
     */
    void trackTelemetryMetric(String metricName, int sampleCount);

    /**
     * Enable the telemetry.
     * Note: needs the instrumentation key to have been provided initially.
     */
    void enableTelemetry();

    /**
     * Disable telemetry.
     */
    void disableTelemetry();

    /**
     * Gets whether telemetry is currently enabled or not.
     * @return whether telemetry is currently enabled or not
     */
    boolean isTelemetryEnabled();

    /**
     * Add a telemetry event.
     * @param eventName  the event name
     * @param properties the property map for the event
     */
    void addTelemetryEvent(String eventName, Map<String, String> properties);

    /**
     * Add a telemetry event.
     * @param eventName the event name
     * @param exception the exception
     */
    void addTelemetryEvent(String eventName, Exception exception);

    /**
     * Add a telemetry event.
     * @param eventName  the event name
     * @param exception  the exception
     * @param properties the property map for the event
     */
    void addTelemetryEvent(String eventName, Exception exception, Map<String, String> properties);
}
