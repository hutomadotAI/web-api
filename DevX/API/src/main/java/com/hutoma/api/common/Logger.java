package com.hutoma.api.common;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Singleton;

/**
 * Created by David MG on 05/08/2016.
 */
@Singleton
public class Logger implements ILogger {

    protected void logOutput(Level level, String fromLabel, String logComment) {
        DateTimeFormatter dateFormat = DateTimeFormat.mediumDateTime();
        DateTime dateTime = new DateTime();

        if ((null == fromLabel) || fromLabel.isEmpty()) {
            fromLabel = "none";
        }

        if ((null == logComment) || logComment.isEmpty()) {
            logComment = "";
        }

        System.out.println(dateFormat.print(dateTime) + " HU:API " + level.toString() + " [" + fromLabel + "] " + logComment);
    }

    public void logDebug(String fromLabel, String logComment) {
        logOutput(Level.DEBUG, fromLabel, logComment);
    }

    public void logInfo(String fromLabel, String logComment) {
        logOutput(Level.INFO, fromLabel, logComment);
    }

    public void logWarning(String fromLabel, String logComment) {
        logOutput(Level.WARNING, fromLabel, logComment);
    }

    public void logError(String fromLabel, String logComment) {
        logOutput(Level.ERROR, fromLabel, logComment);
    }

    public void initialize(Config config) {
    }

    protected enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
    }
}