package com.hutoma.api.connectors.db;

public interface IDatabaseConfig {
    String getDatabaseConnectionString();
    int getDatabaseConnectionPoolMinimumSize();
    int getDatabaseConnectionPoolMaximumSize();
}
