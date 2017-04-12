package com.hutoma.api.common;

import com.google.gson.JsonParseException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    private String esLoggingUrl;

    @Inject
    public CentralLogger(final JerseyClient jerseyClient, final JsonSerializer serializer) {
        this.jerseyClient = jerseyClient;
        this.serializer = serializer;
    }

    /**
     * {@inheritDoc}
     */
    public void logDebug(String fromLabel, String logComment) {
        logDebug(fromLabel, logComment, null);
    }

    /**
     * {@inheritDoc}
     */
    public void logDebug(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.DEBUG, fromLabel, logComment, properties);
    }

    /**
     * {@inheritDoc}
     */
    public void logInfo(String fromLabel, String logComment) {
        logOutput(EventType.INFO, fromLabel, logComment);
    }

    /**
     * {@inheritDoc}
     */
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
        logUserExceptionEvent(fromLabel, sb.toString(), null, ex, null);
    }

    /**
     * {@inheritDoc}
     */
    public void logWarning(final String fromLabel, final String logComment) {
        logWarning(fromLabel, logComment, null);
    }

    /**
     * {@inheritDoc}
     */
    public void logWarning(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.WARNING, fromLabel, logComment, properties);
    }

    /**
     * {@inheritDoc}
     */
    public void logError(String fromLabel, String logComment) {
        logOutput(EventType.ERROR, fromLabel, logComment);
    }

    public void initialize(final Config config) {
        this.startLoggingScheduler(
                config.getElasticSearchLoggingUrl(),
                config.getLoggingUploadCadency());
    }

    /**
     * {@inheritDoc}
     */
    public void logUserTraceEvent(final String logFrom, final String event, final String user,
                                  final LogMap properties) {
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
    public void logUserExceptionEvent(final String logFrom, final String event, final String user,
                                      final Exception exception) {
        this.logUserExceptionEvent(logFrom, event, user, exception, null);
    }

    /**
     * {@inheritDoc}
     */
    public void logUserExceptionEvent(final String logFrom, final String eventName, final String user,
                                      final Exception exception,
                                      final LogMap properties) {
        LogMap map = new LogMap(properties);
        map = map.put("message", exception.getMessage())
                .put("stackTrace", getStackTraceAsString(exception.getStackTrace()))
                .put("suppressedExceptions", exception.getSuppressed()
                        != null ? exception.getSuppressed().length : 0);
        if (exception.getSuppressed() != null) {
            for (int i = 0; i < exception.getSuppressed().length; i++) {
                String key = String.format("suppressed_%d", i + 1);
                Throwable ex = exception.getSuppressed()[i];
                map = map.put(key + "_message", ex.getMessage())
                        .put(key + "_stackTrace", getStackTraceAsString(ex.getStackTrace()));
            }
        }
        this.logOutput(EventType.EXCEPTION, eventName,
                String.format("%s: %s - %s", exception.getClass().getName(), exception.getMessage(),
                        getStackTraceAsString(exception.getStackTrace())), addUserToMap(user, map));
    }

    /**
     * {@inheritDoc}
     */
    public void logUserErrorEvent(String logFrom, String event, String user, LogMap properties) {
        this.logOutput(EventType.ERROR, logFrom, event, addUserToMap(user, properties));
    }

    @Override
    public void logUserWarnEvent(final String logFrom, final String event, final String user,
                                 final LogMap properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }

    public void logPerf(final String fromLabel, final String logComment, final LogMap logMap) {
        logOutput(EventType.PERF, fromLabel, logComment, logMap);
    }

    public void logWarnEvent(String logFrom, String event, String user, LogMap properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }

    public void shutdown() {
        this.timer.cancel();
    }

    private LogMap addUserToMap(final String user, final LogMap map) {
        return new LogMap(map).put("user", user == null ? "" : user);
    }

    private void dumpToStorage() {
        if (this.logQueue.isEmpty() || this.esLoggingUrl == null) {
            return;
        }

        List<LogEvent> events;
        synchronized (this) {
            events = this.logQueue.stream().collect(Collectors.toList());
            this.logQueue.clear();
        }

        Response response = null;
        try {
            List<String> docs = new ArrayList<>();
            for (LogEvent event : events) {
                docs.add(this.serializer.serialize(event));
            }
            ElasticSearchClient esClient = new ElasticSearchClient(this.jerseyClient, this.esLoggingUrl);
            response = esClient.uploadDocumentBulk(this.getAppId().toLowerCase(), docs);
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                LOGGER.error(String.format("Failed to upload logs to the ES logging server! - %s - %s",
                        response.getStatus(), response.readEntity(String.class)));
            }
        } catch (JsonParseException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            if (response != null) {
                response.bufferEntity();
                response.close();
            }
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
        COUNT,
        PERF
    }

    private static class LogEvent {
        private long timestamp;
        private DateTime dateTime;
        private String type;
        private String tag;
        private String message;
        private Map<String, Object> params;

        @Override
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = df.format(this.dateTime);
            return String.format("%s HU:API %s [%s] %s", date, this.type, this.tag, this.message);
        }
    }

    public static class LogParameters extends HashMap<String, Object> {

        public LogParameters(String action) {
            put("Action", action);
        }


        public Object put(final String key, final Object objectValue) {
            return super.put(key, objectValue);
        }

    }

    protected void startLoggingScheduler(final String esLoggingUrl, final int loggingCadence) {
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
        this.esLoggingUrl = esLoggingUrl;
    }

    protected String getAppId() {
        return "API-applog-v1";
    }

    void logOutput(EventType level, String fromLabel, String logComment) {
        this.logOutput(level, fromLabel, logComment, null);
    }

    void logOutput(EventType level, String fromLabel, String logComment, LogMap params) {
        LogEvent event = new LogEvent();
        event.type = level.name();
        event.timestamp = System.currentTimeMillis();
        event.dateTime = new DateTime(DateTimeZone.UTC);
        event.tag = (fromLabel == null || fromLabel.isEmpty()) ? "none" : fromLabel;
        event.message = logComment == null ? "" : logComment;
        event.params = params == null ? null : params.get();

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