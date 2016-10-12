package com.hutoma.api.connectors;

import com.hutoma.api.containers.sub.TrainingStatus;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import com.hutoma.api.common.Config;
import com.hutoma.api.containers.sub.TrainingStatus;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by David MG on 30/08/2016.
 */
public class DatabaseCall implements AutoCloseable {

    Connection connection;
    PreparedStatement statement;
    int paramCount;
    int paramSetIndex;
    String callName;
    DatabaseConnectionPool pool;

    @Inject
    public DatabaseCall(DatabaseConnectionPool pool) {
        this.pool = pool;
        this.statement = null;
        this.connection = null;
    }

    private Connection getConnection() throws Database.DatabaseException {
        return this.pool.borrowConnection();
    }

    private void checkPosition() throws Database.DatabaseException {
        if (this.paramSetIndex >= this.paramCount) {
            throw new Database.DatabaseException(new Exception("too many parameters added in call " + this.callName));
        }
    }

    private void checkParamsSet() throws Database.DatabaseException {
        if (this.paramSetIndex != this.paramCount) {
            throw new Database.DatabaseException(new Exception("not enough parameters added in call " + this.callName));
        }
    }

    ResultSet executeQuery() throws Database.DatabaseException {
        checkParamsSet();
        try {
            return this.statement.executeQuery();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

    int executeUpdate() throws Database.DatabaseException {
        checkParamsSet();
        try {
            return this.statement.executeUpdate();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

    @SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            justification = "Statement is dynamically built from a stored procedure name and uses parameterization")
    DatabaseCall initialise(String storedProcedureName, int numberOfParams) throws Database.DatabaseException {

        this.paramCount = numberOfParams;
        this.paramSetIndex = 0;
        this.callName = storedProcedureName;

        // build the stored procedure string
        StringBuilder sb = new StringBuilder("CALL ");
        sb.append(storedProcedureName);
        sb.append("(");
        for (int i = 0; i < numberOfParams; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(")");

        // prepare the statement
        try {
            this.connection = getConnection();
            this.statement = this.connection.prepareStatement(sb.toString());
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(String param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setString(++this.paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(int param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setInt(++this.paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(long param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setLong(++this.paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(boolean param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setBoolean(++this.paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(double param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setDouble(++this.paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(TrainingStatus.trainingStatus param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setString(++paramSetIndex, String.valueOf(param));
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall addTimestamp() throws Database.DatabaseException {
        return add(DateTime.now());
    }

    DatabaseCall add(DateTime param) throws Database.DatabaseException {
        checkPosition();
        try {
            this.statement.setDate(++this.paramSetIndex, new java.sql.Date(param.getMillis()));
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(UUID param) throws Database.DatabaseException {
        return this.add(param.toString());
    }

    @Override
    public void close() {
        try {
            if ((null != this.statement) && (!this.statement.isClosed())) {
                this.statement.close();
            }
        } catch (SQLException e) {
        }
        this.statement = null;
        if (null != this.connection) {
            try {
                this.connection.close();
            } catch (SQLException e) {
            }
            this.connection = null;
        }
    }
}
