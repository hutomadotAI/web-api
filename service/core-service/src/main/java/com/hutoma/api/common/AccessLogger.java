package com.hutoma.api.common;

import com.hutoma.api.logging.CentralLogger;

/**
 * Telemetry Logging for Chat iterations.
 */
public class AccessLogger extends CentralLogger {

    @Override
    protected String getAppId() {
        return "API-access-v1";
    }
}
