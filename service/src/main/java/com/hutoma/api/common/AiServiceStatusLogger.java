package com.hutoma.api.common;

import com.hutoma.api.containers.sub.AiStatus;

import org.glassfish.jersey.client.JerseyClient;

import java.util.HashMap;
import javax.inject.Inject;

/**
 * AI Service Status Logger.
 */
public class AiServiceStatusLogger extends TelemetryCentralLogger {

    // Log chat iterations every 10 seconds
    private static final int SERVICESTATUS_LOGGING_CADENCE = 8000;
    private static final String APP_ID = "API-servicesStatus-v1";

    @Inject
    public AiServiceStatusLogger(final JerseyClient jerseyClient, final JsonSerializer serializer,
                                 final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getLoggingServiceUrl(), SERVICESTATUS_LOGGING_CADENCE);
    }

    public void logStatusUpdate(final String tag, final AiStatus status) {
        this.addTelemetryEvent(tag, new HashMap<String, String>() {{
            this.put("AIEngine", status.getAiEngine());
            this.put("AIID", status.getAiid().toString());
            this.put("DEVID", status.getDevId());
            this.put("Status", status.getTrainingStatus().value());
            this.put("TrainingError", Double.toString(status.getTrainingError()));
            this.put("TrainingProgress", Double.toString(status.getTrainingProgress()));
        }});
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
