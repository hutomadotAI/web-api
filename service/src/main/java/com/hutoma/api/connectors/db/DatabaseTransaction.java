package com.hutoma.api.connectors.db;

import com.hutoma.api.common.ILogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 05/10/2016.
 */
public class DatabaseTransaction implements AutoCloseable {

    private static final String LOGFROM = "dbtransaction";
    private final DatabaseConnectionPool pool;
    private final Provider<TransactionalDatabaseCall> callprovider;
    private final ArrayList<TransactionalDatabaseCall> openCalls = new ArrayList<>();
    private final ILogger logger;
    private Connection transactionConnection;

    @Inject
    public DatabaseTransaction(ILogger logger, DatabaseConnectionPool pool,
                               Provider<TransactionalDatabaseCall> callprovider) {
        this.pool = pool;
        this.callprovider = callprovider;
        this.logger = logger;
    }

    /***
     * Gets a DatabaseCall (wrapper for a single statetemnt) and sets it up for this transaction
     * @return
     * @throws DatabaseException
     */
    public DatabaseCall getDatabaseCall() throws DatabaseException {
        TransactionalDatabaseCall call = this.callprovider.get().setTransactionConnection(getTransactionConnection());
        // keep a record of this call as part of the transaction
        this.openCalls.add(call);
        return call;
    }

    /***
     * Commit the transaction if all has gone well
     * @throws DatabaseException
     */
    public void commit() throws DatabaseException {
        // if we made a DB call at all
        if (null != this.transactionConnection) {
            try {
                // commit, close and forget the connection
                this.transactionConnection.commit();
                this.transactionConnection.close();
                this.transactionConnection = null;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
    }

    /***
     * Rollback and cleanup
     * @throws DatabaseException
     */
    public void rollback() throws DatabaseException {
        if (null != this.transactionConnection) {
            try {
                // rollback, close and forget the connection
                this.transactionConnection.rollback();
                this.transactionConnection.close();
                this.transactionConnection = null;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
    }

    @Override
    /***
     * Cleanup each call and finally the connection.
     * If we haven't committed or rolled back then we roll back here.
     * This method is normally called as part of auto-close
     */
    public void close() {
        this.openCalls.forEach((call) -> call.close());
        try {
            rollback();
        } catch (DatabaseException e) {
            this.logger.logError(LOGFROM, "transaction rollback failed: " + e.toString());
        }
    }

    /***
     * Get the connection to be used for this transaction. If this is the first call then this method
     * borrows the connection from a pool and sets it up for a transaction
     * @return an open connection configured for a transaction
     * @throws DatabaseException
     */
    private Connection getTransactionConnection() throws DatabaseException {
        if (null == this.transactionConnection) {
            this.transactionConnection = this.pool.borrowConnection();
            // remove auto-commit so that we can commit explicitly
            try {
                this.transactionConnection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
        return this.transactionConnection;
    }
}
