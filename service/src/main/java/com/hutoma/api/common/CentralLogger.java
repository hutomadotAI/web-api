package com.hutoma.api.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
public class CentralLogger implements ILogger {

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
        logUserExceptionEvent(fromLabel, sb.toString(), null, ex, (String[]) null);
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

    /**
     * {@inheritDoc}
     */
    public void logUserTraceEvent(final String logFrom, final String event, final String user,
                                  final Map<String, String> properties) {
        this.logOutput(EventType.TRACE, logFrom, event, addUserToMap(user, properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserTraceEvent(final String logFrom, final String event, final String user) {
        this.logOutput(EventType.TRACE, logFrom, event, addUserToMap(user, null));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserTraceEvent(final String logFrom, final String event, final String user,
                                  final String... properties) {
        this.logUserTraceEvent(logFrom, event, user, arrayToMap(properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserExceptionEvent(final String logFrom, final String event, final String user,
                                      final Exception exception) {
        this.logUserExceptionEvent(logFrom, event, user, exception, (String[]) null);
    }

    /**
     * {@inheritDoc}
     */
    public void logUserExceptionEvent(final String logFrom, final String eventName, final String user,
                                      final Exception exception, final String... properties) {
        this.logUserExceptionEvent(logFrom, eventName, user, exception, arrayToMap(properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserExceptionEvent(final String logFrom, final String eventName, final String user,
                                      final Exception exception,
                                      final Map<String, String> properties) {
        Map<String, String> map = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
        map.put("message", exception.getMessage());
        map.put("stackTrace", getStackTraceAsString(exception.getStackTrace()));
        map.put("suppressedExceptions", exception.getSuppressed()
                != null ? Integer.toString(exception.getSuppressed().length) : "0");
        if (exception.getSuppressed() != null) {
            for (int i = 0; i < exception.getSuppressed().length; i++) {
                String key = String.format("suppressed_%d", i + 1);
                Throwable ex = exception.getSuppressed()[i];
                map.put(key + "_message", ex.getMessage());
                map.put(key + "_stackTrace", getStackTraceAsString(ex.getStackTrace()));
            }
        }
        this.logOutput(EventType.EXCEPTION, eventName,
                String.format("%s: %s - %s", exception.getClass().getName(), exception.getMessage(),
                        getStackTraceAsString(exception.getStackTrace())), addUserToMap(user, map));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserErrorEvent(String logFrom, String event, String user, String... properties) {
        this.logUserErrorEvent(logFrom, event, user, arrayToMap(properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserErrorEvent(String logFrom, String event, String user, Map<String, String> properties) {
        this.logOutput(EventType.ERROR, logFrom, event, addUserToMap(user, properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserWarnEvent(String logFrom, String event, String user, String... properties) {
        this.logWarnEvent(logFrom, event, user, arrayToMap(properties));
    }

    @Override
    public void logUserWarnEvent(final String logFrom, final String event, final String user,
                                 final Map<String, String> properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }


    public void logWarnEvent(String logFrom, String event, String user, Map<String, String> properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }

    public void shutdown() {
        this.timer.cancel();
    }

    private Map<String, String> arrayToMap(final String[] array) {
        if (array == null) {
            return new HashMap<>();
        }
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("Properties need to be in the format of key1, value1, etc");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < array.length; i += 2) {
            map.put(array[i], array[i + 1]);
        }
        return map;
    }

    private Map<String, String> addUserToMap(final String user, final Map<String, String> map) {
        Map<String, String> newMap = map == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(map);
        newMap.put("user", user == null ? "" : user);
        return newMap;
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

    private String getStackTraceAsString(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : stackTrace) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
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

    public static class LogParameters extends HashMap<String, String> {

        public LogParameters(String action) {
            put("Action", action);
        }

        @Override
        public String put(final String key, final String value) {
            return super.put(key, (value == null ? "(null)" : value));
        }

        public String put(final String key, final Object objectValue) {
            return super.put(key, (objectValue == null ? "(null)" : objectValue.toString()));
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