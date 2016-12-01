package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * Telemetry Logging for Chat iterations.
 */
public class ChatTelemetryLogger extends TelemetryCentralLogger {

    // Log chat iterations every 10 seconds
    private static final int CHAT_LOGGING_CADENCE = 10000;
    private static final String APP_ID = "API-chatlog-v1";

    @Inject
    public ChatTelemetryLogger(final JerseyClient jerseyClient, final JsonSerializer serializer, final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getLoggingServiceUrl(), CHAT_LOGGING_CADENCE);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
