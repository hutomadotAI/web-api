package com.hutoma.api.connectors.db;

import com.hutoma.api.containers.sub.UserInfo;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseUser extends Database {

    @Inject
    public DatabaseUser(final ILogger logger, final Provider<DatabaseCall> callProvider,
                    final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    public boolean createDev(final String devToken, final int planId, final String devId)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addUser", 3)
                    .add(devToken)
                    .add(planId)
                    .add(devId);
            return call.executeUpdate() > 0;
        }
    }

    /***
     * Delete a developer from the database and remove the developer's AIs
     * @param devid
     * @return true if the user was found and deleted, false if no user was found
     * @throws DatabaseException
     */
    public boolean deleteDev(final UUID devid) throws DatabaseException {

        int updateCount = 0;
        // start a transaction
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            // delete the user's AIs and status for those AIs
            transaction.getDatabaseCall().initialise("deleteAllAIs", 1)
                    .add(devid).executeUpdate();
            // delete the user
            updateCount = transaction.getDatabaseCall().initialise("deleteUser", 1)
                    .add(devid).executeUpdate();
            // if all goes well, commit
            transaction.commit();
        }
        return updateCount > 0;
    }

    public String getDevToken(final UUID devid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevTokenFromDevID", 1).add(devid);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("dev_token");
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public UserInfo getUserFromDevId(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getUserFromDevId", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return getUserInfoFromRs(rs);
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    /**
     * Gets a list of all the users
     * @return list of users
     * @throws DatabaseException if a db exception occurs
     */
    public List<UserInfo> getAllUsers() throws DatabaseException {
        List<UserInfo> users = new ArrayList<>();
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getAllUsers", 0);
            final ResultSet rs = call.executeQuery();
            try {
                while (rs.next()) {
                    users.add(getUserInfoFromRs(rs));
                }
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
        return users;
    }

    public boolean updateUserDevToken(final UUID devId, final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateDevToken", 2).add(devId.toString()).add(token);
            return call.executeUpdate() > 0;
        }
    }

    private UserInfo getUserInfoFromRs(final ResultSet rs) throws SQLException {
        return new UserInfo(
                new DateTime(rs.getTimestamp("created")),
                rs.getBoolean("valid"),
                rs.getBoolean("internal"),
                rs.getString("dev_id"),
                rs.getInt("id"),
                rs.getString("dev_token"),
                rs.getInt("plan_id"));
    }
}
