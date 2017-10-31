package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.containers.sub.UserInfo;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseUser extends Database {

    @Inject
    public DatabaseUser(final ILogger logger, final Provider<DatabaseCall> callProvider,
                    final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    public boolean createDev(final String username, final String email, final String password,
                             final String passwordSalt, final String firstName, final String lastName,
                             final String devToken, final int planId, final String devId)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("addUser", 9)
                    .add(username)
                    .add(email)
                    .add(password)
                    .add(passwordSalt)
                    .add(firstName)
                    .add(lastName)
                    .add(devToken)
                    .add(planId)
                    .add(devId);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Gets the user information.
     * @param username the username
     * @return the user information
     * @throws DatabaseException
     */
    public UserInfo getUser(final String username) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getUserDetails", 1).add(username);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new UserInfo(
                            rs.getString("first_name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            new DateTime(rs.getTimestamp("created")),
                            rs.getBoolean("valid"),
                            rs.getBoolean("internal"),
                            rs.getString("password"),
                            rs.getString("password_salt"),
                            rs.getString("dev_id"),
                            rs.getString("attempt"),
                            rs.getInt("id"),
                            rs.getString("dev_token"));
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateUserPassword(final int userId, final String password, final String passwordSalt)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateUserPassword", 3)
                    .add(userId).add(password).add(passwordSalt);
            return call.executeUpdate() > 0;
        }
    }

    public boolean isPasswordResetTokenValid(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("isPasswordResetTokenValid", 1).add(token);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next();
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public int getUserIdForResetToken(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getUserIdForResetToken", 1).add(token);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt("uid");
                }
                return -1;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deletePasswordResetToken(final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deletePasswordResetToken", 1).add(token);
            return call.executeUpdate() > 0;
        }
    }

    public boolean insertPasswordResetToken(final int userId, final String token) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("insertResetToken", 2).add(token).add(userId);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Gets whether the user already exists or not.
     * @param username   the username to check
     * @param checkEmail whether to check the email field as well or not
     * @return whether the user already exists or not
     * @throws DatabaseException
     */
    public boolean userExists(final String username, final boolean checkEmail) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("userExists", 2).add(username).add(checkEmail);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next();
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    /**
     * Updates the user login attempts.
     * @param devId    the developer id
     * @param attempts the number of attempts, or a 'magic code' (this comes from the PHP code)
     * @return whether the update succeeded or not
     * @throws DatabaseException
     */
    public boolean updateUserLoginAttempts(final String devId, final String attempts) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateUserLoginAttempts", 2).add(devId).add(attempts);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Redeems an invite code for user registration.
     * @param code     the invite code.
     * @param username the registering user.
     * @return true if successful, otherwise false.
     * @throws DatabaseException database exception.
     */
    public boolean redeemInviteCode(final String code, final String username) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("redeemInviteCode", 2).add(code).add(username);
            return call.executeUpdate() > 0;
        }
    }

    /**
     * Determines whether a specified invite code is valid.
     * @param code the invite code.
     * @return true if the code is valid, otherwise false.
     * @throws DatabaseException database exception.
     * @throws SQLException      sql exception.
     */
    public boolean inviteCodeValid(final String code) throws DatabaseException, SQLException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("existsInviteCode", 1).add(code);

            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }

        return false;
    }

    /**
     * Gets the developer plan for the given developer Id.
     * @param devId the developer id
     * @return the plan, or null if there is no developer Id or not plan associated to it
     * @throws DatabaseException database exception
     */
    public DevPlan getDevPlan(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDevPlan", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new DevPlan(rs.getInt("maxai"), rs.getInt("monthlycalls"),
                            rs.getLong("maxmem"), rs.getInt("maxtraining"));
                }
                return null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
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
}
