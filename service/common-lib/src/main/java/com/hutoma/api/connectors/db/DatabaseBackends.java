package com.hutoma.api.connectors.db;

import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseBackends extends Database {

    @Inject
    public DatabaseBackends(final ILogger logger, final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    /***
     * Interpret a row from the ai_status table
     * @param rs resultset
     * @return a pair of (type, update)
     * @throws DatabaseException
     */
    static Pair<BackendServerType, BackendEngineStatus> getBackendEngineStatus(ResultSet rs)
            throws DatabaseException {

        try {
            BackendServerType serverType = BackendServerType.forValue(rs.getString("server_type"));
            // If we can't map the server type it means it's either about a service we don't support anymore
            // or there is some error in the db. In any case we can just ignore it.
            if (serverType == null) {
                return null;
            }
            double trainingProgress = rs.getDouble("training_progress");
            double trainingError = rs.getDouble("training_error");
            TrainingStatus trainingStatus = TrainingStatus.forValue(rs.getString("training_status"));
            if (trainingStatus == null) {
                throw new DatabaseException("bad trainingstatus");
            }
            UUID aiid = UUID.fromString(rs.getString("aiid"));

            // queue status
            QueueAction action = QueueAction.forValue(rs.getString("queue_action"));
            String serverIdentifier = rs.getString("server_endpoint");
            java.sql.Timestamp updateTimeObject = rs.getTimestamp("update_time");
            DateTime updateTime = (updateTimeObject == null) ? null : new DateTime(updateTimeObject);

            BackendEngineStatus status = new BackendEngineStatus(aiid, trainingStatus, trainingError,
                    trainingProgress, action, serverIdentifier, updateTime);
            return new Pair<>(serverType, status);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Calls the database to get the status of all back-end servers for the AI
     * @param devId dev
     * @param aiid ai
     * @param call a database call in transaction
     * @return BackendStatus
     * @throws DatabaseException
     * @throws SQLException
     */
    static BackendStatus getBackendStatus(final UUID devId, final UUID aiid, final DatabaseCall call)
            throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAiStatus", 2)
                .add(aiid).add(devId).executeQuery();

        BackendStatus status = new BackendStatus();
        while (rs.next()) {
            Pair<BackendServerType, BackendEngineStatus> update = getBackendEngineStatus(rs);
            if (update != null) {
                status.setEngineStatus(update.getA(), update.getB());
            }
        }

        return status;
    }

    /***
     * Reads the whole status table for a dev and returns a hashmap of BackendStatuses
     * @param devid the dev
     * @param call a database call in transaction
     * @return hashmap
     * @throws DatabaseException
     */
    static HashMap<UUID, BackendStatus> getAllAIsStatusHashMap(
            final UUID devid,
            final DatabaseCall call) throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAIsStatus", 1)
                .add(devid)
                .executeQuery();

        HashMap<UUID, BackendStatus> statuses = new HashMap<>();
        while (rs.next()) {
            UUID aiid = UUID.fromString(rs.getString("aiid"));
            Pair<BackendServerType, BackendEngineStatus> serverStatus =
                    getBackendEngineStatus(rs);
            if (serverStatus != null) {
                statuses.computeIfAbsent(aiid, x -> new BackendStatus())
                        .setEngineStatus(serverStatus.getA(), serverStatus.getB());
            }
        }
        return statuses;
    }
}
