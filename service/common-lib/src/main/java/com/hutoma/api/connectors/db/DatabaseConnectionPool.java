package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;
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

    @Inject
    public DatabaseConnectionPool(final IDatabaseConfig config, final ILogger logger) {
        this.config = config;
        this.logger = logger;
        this.maxActiveConnections = config.getDatabaseConnectionPoolMaximumSize();

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

    public Connection borrowConnection() throws DatabaseException {
        int activeConnections = this.dataSource.getActive();
        this.logger.logDebug(LOGFROM, "idle/active/maxactive " + this.dataSource.getIdle() + "/"
                + activeConnections + "/" + this.maxActiveConnections);
        if ((activeConnections + 1) >= this.maxActiveConnections) {
            this.logger.logWarning(LOGFROM, "reached maximum number of active connections: "
                    + this.maxActiveConnections);
        }
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}
