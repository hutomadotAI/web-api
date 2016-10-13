package com.hutoma.api.connectors.db;

import com.hutoma.api.common.Config;
import com.hutoma.api.connectors.Database;

import javax.inject.Inject;
import java.sql.Connection;

/**
 * Created by David MG on 05/10/2016.
 */
public class TransactionalDatabaseCall extends DatabaseCall {

    Connection transactionConnection = null;

    @Inject
    public TransactionalDatabaseCall(Config config, DatabaseConnectionPool pool) {
        super(config, pool);
    }

    TransactionalDatabaseCall setTransactionConnection(Connection transactionConnection) {
        this.transactionConnection = transactionConnection;
        return this;
    }

    @Override
    protected Connection getConnection() throws Database.DatabaseException {
        return this.transactionConnection;
    }

    @Override
    protected void closeConnection() {
        // allow the transaction to close the connection
    }
}
