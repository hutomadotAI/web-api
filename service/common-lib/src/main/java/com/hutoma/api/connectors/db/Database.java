package com.hutoma.api.connectors.db;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    protected static final String LOGFROM = "database";

    protected final ILogger logger;
    protected final Provider<DatabaseCall> callProvider;
    protected final Provider<DatabaseTransaction> transactionProvider;

    @Inject
    public Database(final ILogger logger, final Provider<DatabaseCall> callProvider,
                    final Provider<DatabaseTransaction> transactionProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
        this.transactionProvider = transactionProvider;
    }

    /***
     * Truncate strings if they are too long for the database field
     * @param field source data
     * @param maxLength
     * @return
     */
    static String limitSize(final String field, final int maxLength) {
        return ((field == null) || (field.length() <= maxLength)) ? field : field.substring(0, maxLength);
    }


    public RateLimitStatus checkRateLimit(final UUID devId, final String rateKey, final double burst,
                                          final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rateLimitCheck", 4)
                    .add(devId).add(rateKey).add(burst).add(frequency);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(
                            rs.getBoolean("rate_limit"),
                            rs.getFloat("tokens"),
                            rs.getBoolean("valid"));
                }
                throw new DatabaseException(
                        new Exception("stored proc should have returned a row but it returned none"));
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
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

    public boolean updateAIStatus(final AiStatus status)
            throws DatabaseException {
        return updateAIStatus(status.getAiEngine(), status.getAiid(), status.getTrainingStatus(),
                status.getServerIdentifier(), status.getTrainingProgress(), status.getTrainingError());
    }

    public boolean updateAIStatus(BackendServerType serverType, UUID aiid, TrainingStatus trainingStatus,
                                  String endpoint, double trainingProgress, double trainingError)
            throws DatabaseException {

        // open a transaction since this is a read/modify/write operation and we need consistency
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            transaction.getDatabaseCall().initialise("updateAiStatus", 6)
                    .add(serverType.value())
                    .add(aiid)
                    .add(trainingStatus)
                    .add(endpoint)
                    .add(trainingProgress)
                    .add(trainingError)
                    .executeUpdate();

            // if all goes well, commit
            transaction.commit();
        }

        // flag success
        return true;

    }

    /***
     * Update the queue status for(servertype, aiid) without affecting AI status values
     * @param serverType
     * @param aiid
     * @param setQueued
     * @param queueOffsetSeconds
     * @param action
     * @throws DatabaseException
     */
    public void queueUpdate(BackendServerType serverType, UUID aiid, boolean setQueued,
                            int queueOffsetSeconds, QueueAction action) throws DatabaseException {

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            transaction.getDatabaseCall().initialise("queueUpdate", 5)
                    .add(serverType.value())
                    .add(aiid)
                    .add(setQueued)
                    .add(queueOffsetSeconds)
                    .add(action.value())
                    .executeUpdate();

            // if all goes well, commit
            transaction.commit();
        }
    }
}
