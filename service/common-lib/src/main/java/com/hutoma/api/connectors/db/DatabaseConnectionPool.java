package com.hutoma.api.connectors.db;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.CentralLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Created by David MG on 02/09/2016.
 */
public class DatabaseConnectionPool {

    private static final String LOGFROM = "dbconnectionpool";
    private final IDatabaseConfig config;
    private final ILogger logger;
    private final DataSource dataSource;
    private final int maxActiveConnections;
    private final boolean trackLeaks;
    private HashMap<Connection, TrackedConnectionInfo> trackedConnections = new HashMap<>();

    @Inject
    public DatabaseConnectionPool(final IDatabaseConfig config, final ILogger logger) {
        this.config = config;
        this.logger = logger;
        this.maxActiveConnections = config.getDatabaseConnectionPoolMaximumSize();
        this.trackLeaks = config.getDatabaseConnectionPoolLeakTracer();

        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(config.getDatabaseConnectionString());
        poolProperties.setDriverClassName("com.mysql.cj.jdbc.Driver");
        poolProperties.setJmxEnabled(true);
        poolProperties.setTestWhileIdle(true);
        poolProperties.setTestOnBorrow(false);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setTestOnReturn(true);
        poolProperties.setValidationInterval(30000);
        poolProperties.setTimeBetweenEvictionRunsMillis(30000);
        poolProperties.setMaxActive(this.maxActiveConnections);
        poolProperties.setMaxIdle(config.getDatabaseConnectionPoolMaximumSize());
        poolProperties.setInitialSize(config.getDatabaseConnectionPoolMinimumSize());
        poolProperties.setMinIdle(config.getDatabaseConnectionPoolMinimumSize());
        poolProperties.setMaxWait(10000);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setMinEvictableIdleTimeMillis(30000);
        poolProperties.setLogAbandoned(false);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                        + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        poolProperties.setDefaultAutoCommit(true);
        this.dataSource = new DataSource();
        this.dataSource.setPoolProperties(poolProperties);
    }

    public synchronized Connection borrowConnection() throws DatabaseException {
        int activeConnections = this.dataSource.getActive();
        this.logger.logDebug(LOGFROM, "idle/active/maxactive " + this.dataSource.getIdle() + "/"
                + activeConnections + "/" + this.maxActiveConnections);
        if ((activeConnections + 1) >= this.maxActiveConnections) {
            this.logger.logError(LOGFROM, "reached maximum number of active connections: "
                    + this.maxActiveConnections);
            if (this.trackLeaks) {
                logOpenConnections();
            }
        }
        try {
            Connection connection = this.dataSource.getConnection();
            if (this.trackLeaks) {
                trackedConnections.put(connection, new TrackedConnectionInfo(System.currentTimeMillis(),
                        Thread.currentThread().getStackTrace()));
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public synchronized void returnConnection(final Connection connection) {
        try {
            if (trackLeaks) {
                trackedConnections.remove(connection);
            }
            connection.close();
        } catch (SQLException e) {
            this.logger.logWarning(LOGFROM, "Could not close the connection");
        }
    }

    private void logOpenConnections() {
        final long currTimestamp = System.currentTimeMillis();
        // Get only connections that are still marked as open
        Map<Connection, TrackedConnectionInfo> newMap = new HashMap<>();
        for (Map.Entry<Connection, TrackedConnectionInfo> entry: trackedConnections.entrySet()) {
            try {
                if (!entry.getKey().isClosed()) {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            } catch (SQLException ex) {
                //ignore
            }
        }

        LogMap logMap = LogMap.map("NumOpenConnections", newMap.size());
        List<LeakedConnectionInfo> leaked = new ArrayList<>();
        for (Map.Entry<Connection, TrackedConnectionInfo> entry: newMap.entrySet()) {
            TrackedConnectionInfo tc = entry.getValue();
            try {
                leaked.add(new LeakedConnectionInfo(
                    currTimestamp - tc.borrowTimestamp,
                    CentralLogger.getStackTraceAsString(tc.stackTraceBorrow, 12),
                    entry.getKey().isClosed(),
                entry.getKey().isValid(0)));
            } catch (SQLException ex) {
                leaked.add(new LeakedConnectionInfo(0,
                        "Error obtaining data for connection: " + ex.getMessage(), false, false));
            }
        }
        logMap.add("Connections", new JsonSerializer().serialize(leaked).replace("\\n", "\n"));
        this.logger.logDebug(LOGFROM, "DatabaseConnectionPool-open connections", logMap);
        this.trackedConnections = new HashMap<>(newMap);
    }

    private static class TrackedConnectionInfo {
        private long borrowTimestamp;
        private StackTraceElement[] stackTraceBorrow;
        TrackedConnectionInfo(final long borrowTimestamp, final StackTraceElement[] stackTrace) {
            this.borrowTimestamp = borrowTimestamp;
            this.stackTraceBorrow = stackTrace;
        }
    }

    private static class LeakedConnectionInfo {
        private long durationMs;
        private String stackTrace;
        private boolean isClosed;
        private boolean isValid;
        LeakedConnectionInfo(final long durationMs, final String stackTrace, final boolean isClosed,
                             final boolean isValid) {
            this.durationMs = durationMs;
            this.stackTrace = stackTrace;
            this.isClosed = isClosed;
            this.isValid = isValid;
        }
    }
}
