package com.hutoma.api.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

    private static final int LOGGING_QUEUE_LENGTH = 2000;

    private static final Map<EventType, Level> mapEventToLog4j = new HashMap<EventType, Level>() {{
        put(EventType.DEBUG, Level.DEBUG);
        put(EventType.INFO, Level.INFO);
        put(EventType.WARNING, Level.WARN);
        put(EventType.ERROR, Level.ERROR);
        put(EventType.TRACE, Level.TRACE);
        put(EventType.EXCEPTION, Level.ERROR);
        put(EventType.COUNT, Level.INFO);
    }};
    private static final Logger LOGGER = LogManager.getRootLogger();

    protected final JsonSerializer serializer;
    private final JerseyClient jerseyClient;
    private final ArrayBlockingQueue<LogEvent> logQueue = new ArrayBlockingQueue<>(LOGGING_QUEUE_LENGTH);
    private Timer timer;
    private String loggingUrl;

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

    public void logException(String fromLabel, final Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getMessage());
        if (ex.getSuppressed() != null) {
            sb.append(" [");
            for (int i = 0; i < ex.getSuppressed().length; i++) {
                if (i > 0) {
                    sb.append(" ,");
                }
                sb.append(ex.getSuppressed()[i].getMessage());
            }
            sb.append("]");
        }
        logOutput(EventType.ERROR, fromLabel, sb.toString());
    }

    public void logWarning(String fromLabel, String logComment) {
        logOutput(EventType.WARNING, fromLabel, logComment);
    }

    public void logError(String fromLabel, String logComment) {
        logOutput(EventType.ERROR, fromLabel, logComment);
    }

    public void initialize(final Config config) {
        this.startLoggingScheduler(config.getLoggingServiceUrl(), config.getLoggingUploadCadency());
    }

    public void shutdown() {
        this.timer.cancel();
    }

    private void dumpToStorage() {
        if (this.logQueue.isEmpty() || this.loggingUrl == null) {
            return;
        }

        List<LogEvent> events;
        synchronized (this) {
            events = this.logQueue.stream().collect(Collectors.toList());
            this.logQueue.clear();
        }

        String json = this.serializer.serialize(events);
        Response response = this.jerseyClient.target(this.loggingUrl)
                .queryParam("appId", this.getAppId())
                .request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            LOGGER.error("Failed to upload  logs to the logging server! - " + response.getStatus());
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

    protected void startLoggingScheduler(final String loggingServiceUrl, final int loggingCadence) {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CentralLogger.this.dumpToStorage();
            }
        }, loggingCadence, loggingCadence);
        this.loggingUrl = loggingServiceUrl;
    }

    protected String getAppId() {
        return "API-applog-v1";
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

        // If the queue is full then it means that we can't contact the logging service, or that we're
        // logging too many operations within a session
        if (this.logQueue.size() == LOGGING_QUEUE_LENGTH) {
            LOGGER.error("Logging queue full!!! Removing oldest entry.");
            this.logQueue.remove();
        }

        this.logQueue.add(event);
        LOGGER.log(mapEventToLog4j.get(level), String.format("[%s] %s", event.tag, event.message));
    }
}