package com.hutoma.api.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pedrotei on 29/09/16.
 */
@Singleton
public class TelemetryLogger extends Logger {

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
     * Initialize telemetry.
     * Loads the instrumentation key from configuration and sets up the client.
     * If the instrumentation key cannot be found then disables telemetry.
     * @param config the configuration
     */
    public void initialize(Config config) {
        String key = config.getTelemetryKey(APP_NAME);
        if (key != null) {
            TelemetryConfiguration telemetryConfig = TelemetryConfiguration.getActive();
            telemetryConfig.setInstrumentationKey(key);
            telemetryClient = new TelemetryClient(telemetryConfig);
            isTelemetryEnabled = true;
            this.logInfo("telemetrylogger", String.format("Initialized telemetry for key: [%s]", key));
        } else {
            isTelemetryEnabled = false;
        }
    }

    /**
     * Enable the telemetry.
     * Note: needs the instrumentation key to have been provided initially.
     */
    public void enableTelemetry() {
        isTelemetryEnabled = true;
    }

    /**
     * Disable telemetry.
     */
    public void disableTelemetry() {
        isTelemetryEnabled = false;
    }

    /**
     * Gets whether telemetry is currently enabled or not.
     * @return whether telemetry is currently enabled or not
     */
    public boolean isTelemetryEnabled() {
        return isTelemetryEnabled;
    }

    /**
     * Add a telemetry event.
     * @param eventName  the event name
     * @param properties the property map for the event
     */
    public void addTelemetryEvent(String eventName, Map<String, String> properties) {
        if (this.isTelemetryEnabled()) {
            telemetryClient.trackEvent(eventName, properties, null);
        }
    }

    /**
     * Add a telemetry event.
     * @param eventName the event name
     */
    public void addTelemetryEvent(String eventName) {
        this.addTelemetryEvent(eventName, null);
    }

    /**
     * Tracks a metric.
     * @param metricName  the metric name
     * @param sampleCount tehe sample count
     */
    public void trackTelemetryMetric(String metricName, int sampleCount) {
        if (this.isTelemetryEnabled()) {
            telemetryClient.trackMetric(metricName, sampleCount);
        }
    }

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
}
