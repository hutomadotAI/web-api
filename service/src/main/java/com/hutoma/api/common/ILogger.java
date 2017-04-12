package com.hutoma.api.common;

/**
 * Logger interface.
 */
public interface ILogger {

    /**
     * Adds a DEBUG log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     */
    void logDebug(String fromLabel, String logComment);

    /**
     * Adds a DEBUG log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     * @param properties map of properties
     */
    void logDebug(String fromLabel, String logComment, LogMap properties);

    /**
     * Adds a INFO log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     */
    void logInfo(String fromLabel, String logComment);

    /**
     * Logs an exception.
     * @param fromLabel the origin of the log
     * @param ex        the exception
     */
    void logException(String fromLabel, Exception ex);

    /**
     * Adds a WARNING log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     */
    void logWarning(String fromLabel, String logComment);

    /**
     * Adds a WARNING log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     * @param properties map of properties
     */
    void logWarning(String fromLabel, String logComment, LogMap properties);

    /**
     * Adds an ERROR log.
     * @param fromLabel  the origin of the log
     * @param logComment the comment
     */
    void logError(String fromLabel, String logComment);

    /**
     * Initializes the logger from configuration
     * @param config the configuration
     */
    void initialize(Config config);

    /**
     * Logs a user-related TRACE event.
     * @param logFrom    where it's logging from
     * @param event      the event name
     * @param user       the user
     * @param properties map of properties
     */
    void logUserTraceEvent(String logFrom, String event, String user, LogMap properties);

    /**
     * Logs a user-related TRACE event.
     * @param logFrom where it's logging from
     * @param event   the event name
     * @param user    the user
     */
    void logUserTraceEvent(String logFrom, String event, String user);

    /**
     * Logs a user-related EXCEPTION event.
     * @param logFrom   where it's logging from
     * @param event     the event name
     * @param user      the user
     * @param exception exception
     */
    void logUserExceptionEvent(String logFrom, String event, String user, Exception exception);

    /**
     * Logs a user-related EXCEPTION event.
     * @param logFrom    where it's logging from
     * @param event      the event name
     * @param user       the user
     * @param exception  the exception
     * @param properties map of properties
     */
    void logUserExceptionEvent(String logFrom, String event, String user, Exception exception, LogMap properties);

    /**
     * Logs a user-related ERROR event.
     * @param logFrom    where it's logging from
     * @param event      the event name
     * @param user       the user
     * @param properties map of properties
     */
    void logUserErrorEvent(String logFrom, String event, String user, LogMap properties);

    /**
     * Logs a user-related WARN event.
     * @param logFrom    where it's logging from
     * @param event      the event name
     * @param user       the user
     * @param properties map of properties
     */
    void logUserWarnEvent(String logFrom, String event, String user, LogMap properties);
}
