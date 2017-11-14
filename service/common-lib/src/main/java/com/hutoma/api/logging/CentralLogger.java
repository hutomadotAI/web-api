package com.hutoma.api.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fluentd.logger.FluentLogger;
import org.fluentd.logger.sender.Reconnector;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.inject.Singleton;

/**
 * Central logger.
 * This class allows to log all events into a central repository.
 */
@Singleton
public class CentralLogger implements ILogger {

    private static final int TIMEOUT = 500; // 500ms
    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int WAIT_MAX_MILLIS = 5 * 60 * 1000; // Max wait is 5 minute

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

    private FluentLogger fluentLogger;
    private static ILoggerConfig config;


    /**
     * Implementation of an exponential reconnector so we can control the timeouts.
     */
    protected static class ExponentialReconnector implements Reconnector {

        private static final double WAIT_INCR_RATE = 1.5;
        private static final int MAX_ERROR_COUNT = 100;

        private int waitMaxCount;
        private final int initialTimeout;
        private final int maxWaitTime;
        private final LinkedList<Long> errorHistory;

        ExponentialReconnector(final int initialTimeout, final int maxWaitTime) {
            this.initialTimeout = initialTimeout;
            this.maxWaitTime = maxWaitTime;
            this.waitMaxCount = getWaitMaxCount();
            this.errorHistory = new LinkedList<>();
        }

        private int getWaitMaxCount() {
            double r = (double) this.maxWaitTime / (double) this.initialTimeout;
            for (int j = 1; j <= MAX_ERROR_COUNT; j++) {
                if (r < WAIT_INCR_RATE) {
                    return j + 1;
                }
                r = r / WAIT_INCR_RATE;
            }
            return MAX_ERROR_COUNT;
        }

        @Override
        public void addErrorHistory(long timestamp) {
            System.out.println(String.format("[%d] Could not connect to FluentD agent", timestamp));
            errorHistory.addLast(timestamp);
            if (errorHistory.size() > waitMaxCount) {
                errorHistory.removeFirst();
            }
        }

        @Override
        public boolean isErrorHistoryEmpty() {
            return errorHistory.isEmpty();
        }

        @Override
        public void clearErrorHistory() {
            errorHistory.clear();
        }

        @Override
        public boolean enableReconnection(long timestamp) {
            int size = errorHistory.size();
            if (size == 0) {
                return true;
            }

            double suppressMillis = (size < waitMaxCount)
                    ? this.initialTimeout * Math.pow(WAIT_INCR_RATE, size - 1) : maxWaitTime;

            return (timestamp - errorHistory.getLast()) >= suppressMillis;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebug(String fromLabel, String logComment) {
        logDebug(fromLabel, logComment, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDebug(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.DEBUG, fromLabel, logComment, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInfo(String fromLabel, String logComment) {
        logOutput(EventType.INFO, fromLabel, logComment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInfo(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.INFO, fromLabel, logComment, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public void logWarning(final String fromLabel, final String logComment) {
        logWarning(fromLabel, logComment, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logWarning(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.WARNING, fromLabel, logComment, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(final String fromLabel, final String logComment) {
        logError(fromLabel, logComment, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(final String fromLabel, final String logComment, final LogMap properties) {
        logOutput(EventType.ERROR, fromLabel, logComment, properties);
    }

    @Override
    public void logPerf(final String fromLabel, final String logComment, final LogMap logMap) {
        logOutput(EventType.PERF, fromLabel, logComment, logMap);
    }

    @Override
    public void initialize(final ILoggerConfig config) {
        if (CentralLogger.config != null) {
            // Multiple initialization
            return;
        }
        CentralLogger.config = config;
    }

    private FluentLogger getFluentLogger() {
        if (this.fluentLogger == null) {
            this.fluentLogger = FluentLogger.getLogger(
                    this.getAppId(),
                    config.getFluentLoggingHost(),
                    config.getFluentLoggingPort(),
                    TIMEOUT,
                    BUFFER_SIZE,
                    new ExponentialReconnector(TIMEOUT, WAIT_MAX_MILLIS));
        }
        return this.fluentLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logUserTraceEvent(final String logFrom, final String event, final String user,
                                  final LogMap properties) {
        this.logOutput(EventType.TRACE, logFrom, event, addUserToMap(user, properties));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logUserTraceEvent(final String logFrom, final String event, final String user) {
        this.logOutput(EventType.TRACE, logFrom, event, addUserToMap(user, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logUserInfoEvent(final String logFrom, final String event, final String user,
                                 final LogMap properties) {
        this.logOutput(EventType.INFO, logFrom, event, addUserToMap(user, properties));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logUserInfoEvent(final String logFrom, final String event, final String user) {
        this.logOutput(EventType.INFO, logFrom, event, addUserToMap(user, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logUserExceptionEvent(final String logFrom, final String event, final String user,
                                      final Exception exception) {
        this.logUserExceptionEvent(logFrom, event, user, exception, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public void logUserErrorEvent(String logFrom, String event, String user, LogMap properties) {
        this.logOutput(EventType.ERROR, logFrom, event, addUserToMap(user, properties));
    }

    @Override
    public void logUserWarnEvent(final String logFrom, final String event, final String user,
                                 final LogMap properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }

    /**
     * {@inheritDoc}
     */
    public void logWarnEvent(String logFrom, String event, String user, LogMap properties) {
        this.logOutput(EventType.WARNING, logFrom, event, addUserToMap(user, properties));
    }

    private LogMap addUserToMap(final String user, final LogMap map) {
        return new LogMap(map).put("user", user == null ? "" : user);
    }

    public static String getStackTraceAsString(StackTraceElement[] stackTrace) {
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

    public static class LogParameters extends HashMap<String, Object> {

        public LogParameters(String action) {
            put("Action", action);
        }


        @Override
        public Object put(final String key, final Object objectValue) {
            return super.put(key, objectValue);
        }

    }

    protected String getAppId() {
        return "API-applog-v1";
    }

    void logOutput(final EventType level, final String fromLabel, final String logComment) {
        this.logOutput(level, fromLabel, logComment, null);
    }

    private void logOutput(final EventType level, final String fromLabel, final String logComment,
                           final LogMap params) {

        long timestamp = System.currentTimeMillis();
        DateTime date = new DateTime(timestamp, DateTimeZone.UTC);
        String message = logComment == null ? "" : logComment;
        String tag = (fromLabel == null || fromLabel.isEmpty()) ? "none" : fromLabel;
        Map<String, Object> map = new LinkedHashMap<String, Object>() {{
            put("type", level.name());
            put("dateTime", date);
            put("message", message);
            put("params", params == null ? null : params.get());
        }};

        LOGGER.log(mapEventToLog4j.get(level), String.format("[%s] %s", tag, message));

        FluentLogger logger = getFluentLogger();
        boolean wasLoggedToFluent = false;
        if (logger != null) {
            wasLoggedToFluent = getFluentLogger().log(
                    tag,
                    map,
                    timestamp);
        }

        if (!wasLoggedToFluent) {
            LOGGER.log(Level.ERROR, "Could not log to fluent!");
        }
    }
}
