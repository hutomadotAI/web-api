package com.hutoma.api.connectors.db;

import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by David MG on 05/10/2016.
 */
public class DatabaseTransaction implements AutoCloseable {

    private final String LOGFROM = "dbtransaction";
    private DatabaseConnectionPool pool;
    private Provider<TransactionalDatabaseCall> callprovider;
    private Connection transactionConnection;
    private ArrayList<TransactionalDatabaseCall> openCalls = new ArrayList<>();
    private Logger logger;

    @Inject
    public DatabaseTransaction(Logger logger, DatabaseConnectionPool pool, Provider<TransactionalDatabaseCall> callprovider) {
        this.pool = pool;
        this.callprovider = callprovider;
        this.logger = logger;
    }

    /***
     * Get the connection to be used for this transaction. If this is the first call then this method
     * borrows the connection from a pool and sets it up for a transaction
     * @return an open connection configured for a transaction
     * @throws Database.DatabaseException
     */
    private Connection getTransactionConnection() throws Database.DatabaseException {
        if (null == this.transactionConnection) {
            this.transactionConnection = this.pool.borrowConnection();
            // remove auto-commit so that we can commit explicitly
            try {
                this.transactionConnection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new Database.DatabaseException(e);
            }
        }
        return this.transactionConnection;
    }

    /***
     * Gets a DatabaseCall (wrapper for a single statetemnt) and sets it up for this transaction
     * @return
     * @throws Database.DatabaseException
     */
    public DatabaseCall getDatabaseCall() throws Database.DatabaseException {
        TransactionalDatabaseCall call = this.callprovider.get().setTransactionConnection(getTransactionConnection());
        // keep a record of this call as part of the transaction
        this.openCalls.add(call);
        return call;
    }

    /***
     * Commit the transaction if all has gone well
     * @throws Database.DatabaseException
     */
    public void commit() throws Database.DatabaseException {
        // if we made a DB call at all
        if (null != this.transactionConnection) {
            try {
                // commit, close and forget the connection
                this.transactionConnection.commit();
                this.transactionConnection.close();
                this.transactionConnection = null;
            } catch (SQLException e) {
                throw new Database.DatabaseException(e);
            }
        }
    }

    /***
     * Rollback and cleanup
     * @throws Database.DatabaseException
     */
    public void rollback() throws Database.DatabaseException {
        if (null != this.transactionConnection) {
            try {
                // rollback, close and forget the connection
                this.transactionConnection.rollback();
                this.transactionConnection.close();
                this.transactionConnection = null;
            } catch (SQLException e) {
                throw new Database.DatabaseException(e);
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
        } catch (Database.DatabaseException e) {
            this.logger.logError(this.LOGFROM, "transaction rollback failed: " + e.toString());
        }
    }
}
