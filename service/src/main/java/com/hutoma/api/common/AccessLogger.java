package com.hutoma.api.common;

/**
 * Telemetry Logging for Chat iterations.
 */
public class AccessLogger extends CentralLogger {

    @Override
    protected String getAppId() {
        return "API-access-v1";
    }
}
