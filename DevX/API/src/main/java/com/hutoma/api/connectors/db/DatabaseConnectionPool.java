package com.hutoma.api.connectors.db;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;

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
    private final Config config;
    private final Logger logger;
    private final DataSource dataSource;
    private final int maxActiveConnections;

    @Inject
    public DatabaseConnectionPool(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
        this.maxActiveConnections = config.getDatabaseConnectionPoolMaximumSize();

        PoolProperties p = new PoolProperties();
        p.setUrl(config.getDatabaseConnectionString());
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setJmxEnabled(true);
        p.setTestWhileIdle(true);
        p.setTestOnBorrow(false);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(true);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(this.maxActiveConnections);
        p.setMaxIdle(config.getDatabaseConnectionPoolMaximumSize());
        p.setInitialSize(config.getDatabaseConnectionPoolMinimumSize());
        p.setMinIdle(config.getDatabaseConnectionPoolMinimumSize());
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setLogAbandoned(false);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        p.setDefaultAutoCommit(true);
        this.dataSource = new DataSource();
        this.dataSource.setPoolProperties(p);
    }

    public Connection borrowConnection() throws Database.DatabaseException {
        int activeConnections = this.dataSource.getActive();
        this.logger.logDebug(LOGFROM, "idle/active/maxactive " + this.dataSource.getIdle() + "/" + activeConnections + "/" + this.maxActiveConnections);
        if ((activeConnections + 1) >= this.maxActiveConnections) {
            this.logger.logWarning(LOGFROM, "reached maximum number of active connections: " + this.maxActiveConnections);
        }
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

}
