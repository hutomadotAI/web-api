package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;

import java.sql.Connection;
import javax.inject.Inject;

/**
 * Created by David MG on 05/10/2016.
 */
public class TransactionalDatabaseCall extends DatabaseCall {

    private Connection transactionConnection = null;

    @Inject
    public TransactionalDatabaseCall(ILogger logger, IDatabaseConfig config, DatabaseConnectionPool pool) {
        super(logger, config, pool);
    }

    TransactionalDatabaseCall setTransactionConnection(Connection transactionConnection) {
        this.transactionConnection = transactionConnection;
        return this;
    }

    @Override
    protected Connection getConnection() throws DatabaseException {
        return this.transactionConnection;
    }

    @Override
    protected void closeConnection() {
        // allow the transaction to close the connection
    }
}
