package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * Telemetry Logging for Chat iterations.
 */
public class ChatLogger extends CentralLogger {

    // Log chat iterations every 10 seconds
    private static final int CHAT_LOGGING_CADENCE = 10000;
    private static final String APP_ID = "API-chatlog-v1";

    @Inject
    public ChatLogger(final JerseyClient jerseyClient, final JsonSerializer serializer, final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getLoggingServiceUrl(), config.getElasticSearchLoggingUrl(),
                CHAT_LOGGING_CADENCE);
    }

    public void logChatError(final String logFrom, final String devId, final Exception exception,
                             final LogMap properties) {
        this.logUserExceptionEvent(logFrom, "ApiChatError", devId, exception, properties);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
