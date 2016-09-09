package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by David MG on 02/09/2016.
 */
public class DatabaseConnectionPool {

    Config config;
    Logger logger;
    DataSource dataSource;
    int maxActiveConnections;

    private final String LOGFROM = "dbconnectionpool";

    @Inject
    public DatabaseConnectionPool(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
        maxActiveConnections = config.getDatabaseConnectionPoolMaximumSize();

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
        p.setMaxActive(maxActiveConnections);
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
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);
    }

    public Connection borrowConnection() throws Database.DatabaseException {
        int activeConnections = dataSource.getActive();
        logger.logDebug(LOGFROM, "idle/active/maxactive " + dataSource.getIdle() +"/" + activeConnections + "/" + maxActiveConnections);
        if ((activeConnections+1) >= maxActiveConnections) {
            logger.logWarning(LOGFROM, "reached maximum number of active connections: " + maxActiveConnections);
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

}
