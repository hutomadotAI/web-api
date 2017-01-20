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
}
