package com.hutoma.api.common;

import com.hutoma.api.logging.CentralLogger;
import com.hutoma.api.logging.LogMap;

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
