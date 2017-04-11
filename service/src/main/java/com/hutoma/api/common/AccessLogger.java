package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * Telemetry Logging for Chat iterations.
 */
public class AccessLogger extends CentralLogger {

    // Log chat iterations every 20 seconds
    private static final int ACCESS_LOGGING_CADENCE = 20000;
    private static final String APP_ID = "API-access-v1";

    @Inject
    public AccessLogger(final JerseyClient jerseyClient, final JsonSerializer serializer, final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getElasticSearchLoggingUrl(), ACCESS_LOGGING_CADENCE);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
