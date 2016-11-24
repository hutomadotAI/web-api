package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Central logger.
 * This class allows to log all events into a central repository.
 */
@Singleton
class CentralLogger implements ILogger {

    private static final String APP_ID = "API-applog-v1";
    protected final JsonSerializer serializer;
    private final JerseyClient jerseyClient;
    private final ArrayBlockingQueue<LogEvent> logQueue = new ArrayBlockingQueue<>(1000);
    private Timer timer;
    private Config config;


    @Inject
    public CentralLogger(final JerseyClient jerseyClient, final JsonSerializer serializer) {
        this.jerseyClient = jerseyClient;
        this.serializer = serializer;
    }

    public void logDebug(String fromLabel, String logComment) {
        logOutput(EventType.DEBUG, fromLabel, logComment);
    }

    public void logInfo(String fromLabel, String logComment) {
        logOutput(EventType.INFO, fromLabel, logComment);
    }

    public void logWarning(String fromLabel, String logComment) {
        logOutput(EventType.WARNING, fromLabel, logComment);
    }

    public void logError(String fromLabel, String logComment) {
        logOutput(EventType.ERROR, fromLabel, logComment);
    }

    public void initialize(Config config) {
        this.config = config;
        this.timer = new Timer();
        int cadency = config.getLoggingUploadCadency();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CentralLogger.this.dumpToStorage();
            }
        }, cadency, cadency);
    }

    public void shutdown() {
        this.timer.cancel();
    }

    private void dumpToStorage() {
        String loggingUrl = this.config.getLoggingServiceUrl();
        if (this.logQueue.isEmpty() || loggingUrl == null) {
            return;
        }

        List<LogEvent> events;
        synchronized (this) {
            events = this.logQueue.stream().collect(Collectors.toList());
            this.logQueue.clear();
        }

        String json = this.serializer.serialize(events);
        Response response = this.jerseyClient.target(loggingUrl)
                .queryParam("appId", APP_ID)
                .request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            System.out.println("Failed to upload  logs to the logging server! - " + response.getStatus());
        }
    }

    protected enum EventType {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        TRACE,
        EXCEPTION,
        COUNT
    }

    private static class LogEvent {
        private long timestamp;
        private String type;
        private String tag;
        private String message;
        private Map<String, String> params;

        @Override
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = df.format(new Date(this.timestamp));
            return String.format("%s HU:API %s [%s] %s", date, this.type, this.tag, this.message);
        }
    }

    void logOutput(EventType level, String fromLabel, String logComment) {
        this.logOutput(level, fromLabel, logComment, null);
    }

    void logOutput(EventType level, String fromLabel, String logComment, Map<String, String> params) {
        LogEvent event = new LogEvent();
        event.type = level.name();
        event.timestamp = System.currentTimeMillis();
        event.tag = (fromLabel == null || fromLabel.isEmpty()) ? "none" : fromLabel;
        event.message = logComment == null ? "" : logComment;
        event.params = params;
        this.logQueue.add(event);
        System.out.println(event.toString());
    }
}