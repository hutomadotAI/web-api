package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by pedrotei on 29/09/16.
 */
@Singleton
public class TelemetryCentralLogger extends CentralLogger implements ITelemetry {

    /**
     * Whether the telemetry is enabled or disabled.
     */
    private boolean isTelemetryEnabled;

    @Inject
    public TelemetryCentralLogger(final JerseyClient jerseyClient, final JsonSerializer serializer) {
        super(jerseyClient, serializer);

    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName) {
        this.logOutput(EventType.TRACE, eventName, null, null);
    }


    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Map<String, String> properties) {
        this.logOutput(EventType.TRACE, eventName, null, properties);
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Exception exception) {
        addTelemetryEvent(eventName, exception, null);
    }

    /**
     * {@inheritDoc}
     */
    public void addTelemetryEvent(String eventName, Exception exception, Map<String, String> properties) {
        Map<String, String> map = properties == null ? new HashMap<>() : new HashMap<>(properties);
        map.put("message", exception.getMessage());
        map.put("stackTrace", getStackTraceAsString(exception.getStackTrace()));
        this.logOutput(EventType.EXCEPTION, eventName, exception.getClass().getName(), map);
    }

    /**
     * {@inheritDoc}
     */
    public void trackTelemetryMetric(String metricName, int sampleCount) {
        this.logOutput(EventType.COUNT, metricName, Integer.toString(sampleCount));
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

    private String getStackTraceAsString(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : stackTrace) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
    }
}
