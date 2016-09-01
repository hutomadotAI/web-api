package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.sql.*;

/**
 * Created by David MG on 30/08/2016.
 */
public class DatabaseCall implements AutoCloseable {

    Config config;

    Connection connection;
    PreparedStatement statement;
    int paramCount;
    int paramSetIndex;
    String callName;

    @Inject
    public DatabaseCall(Config config) {
        this.config = config;
        this.statement = null;
        this.connection = null;
    }

    private Connection getConnection() throws Database.DatabaseException {
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = config.getDatabaseConnectionString();
            Class.forName(myDriver);
            return DriverManager.getConnection(myUrl);
        } catch (Exception e) {
            throw new Database.DatabaseException(e);
        }
    }

    private void checkPosition() throws Database.DatabaseException {
        if (paramSetIndex>=paramCount) {
            throw new Database.DatabaseException(new Exception("too many parameters added in call " + callName));
        }
    }

    private void checkParamsSet() throws Database.DatabaseException {
        if (paramSetIndex!=paramCount) {
            throw new Database.DatabaseException(new Exception("not enough parameters added in call " + callName));
        }
    }

    ResultSet executeQuery() throws Database.DatabaseException {
        checkParamsSet();
        try {
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

    int executeUpdate() throws Database.DatabaseException {
        checkParamsSet();
        try {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
    }

    DatabaseCall initialise(String storedProcedureName, int numberOfParams) throws Database.DatabaseException {

        this.paramCount = numberOfParams;
        this.paramSetIndex = 0;
        this.callName = storedProcedureName;

        // build the stored procedure string
        StringBuilder sb = new StringBuilder("CALL ");
        sb.append(storedProcedureName);
        sb.append("(");
        for(int i=0; i<numberOfParams; i++) {
            if (i>0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(")");

        // prepare the statement
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sb.toString());
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(String param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setString(++paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(int param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setInt(++paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(long param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setLong(++paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(boolean param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setBoolean(++paramSetIndex, param);
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    DatabaseCall add(double param) throws Database.DatabaseException {
        checkPosition();
        try {
            statement.setDouble(++paramSetIndex, param);
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
            statement.setDate(++paramSetIndex, new java.sql.Date(param.getMillis()));
        } catch (SQLException e) {
            throw new Database.DatabaseException(e);
        }
        return this;
    }

    @Override
    public void close() {
        try {
            if ((null != statement) && (!statement.isClosed())) {
                statement.close();
            }
        } catch (SQLException e) {
        }
        statement = null;
        if (null!=connection) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
            connection = null;
        }
    }
}
