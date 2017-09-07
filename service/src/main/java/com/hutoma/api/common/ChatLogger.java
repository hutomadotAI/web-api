package com.hutoma.api.common;

/**
 * Telemetry Logging for Chat iterations.
 */
public class ChatLogger extends CentralLogger {

    public void logChatError(final String logFrom, final String devId, final Exception exception,
                             final LogMap properties) {
        this.logUserExceptionEvent(logFrom, "ApiChatError", devId, exception, properties);
    }

    @Override
    protected String getAppId() {
        return "API-chatlog-v1";
    }
}
