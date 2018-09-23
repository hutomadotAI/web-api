package com.hutoma.api.connectors.db;

import com.hutoma.api.common.Pair;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseBackends extends Database {

    @Inject
    public DatabaseBackends(final ILogger logger,
                            final Provider<DatabaseCall> callProvider,
                            final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    /***
     * Interpret a row from the ai_status table
     * @param rs resultset
     * @return a pair of (type, update)
     * @throws DatabaseException
     */
    static Pair<ServiceIdentity, BackendEngineStatus> getBackendEngineStatus(final ResultSet rs)
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
            SupportedLanguage language = SupportedLanguage.get(rs.getString("server_language"));
            String serverVersion = rs.getString("server_version");

            // queue status
            QueueAction action = QueueAction.forValue(rs.getString("queue_action"));
            String serverIdentifier = rs.getString("server_endpoint");
            java.sql.Timestamp updateTimeObject = rs.getTimestamp("update_time");
            DateTime updateTime = (updateTimeObject == null) ? null : new DateTime(updateTimeObject);

            BackendEngineStatus status = new BackendEngineStatus(aiid, trainingStatus, trainingError,
                    trainingProgress, action, serverIdentifier, updateTime);
            ServiceIdentity serviceIdentity = new ServiceIdentity(serverType, language, serverVersion);
            return new Pair<>(serviceIdentity, status);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Calls the database to get the status of all back-end servers for the AI
     * @param aiIdentity ai
     * @param call a database call in transaction
     * @return BackendStatus
     * @throws DatabaseException
     * @throws SQLException
     */
    static BackendStatus getBackendStatus(final AiIdentity aiIdentity, final DatabaseCall call)
            throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAiStatus", 4)
                .add(aiIdentity.getAiid())
                .add(aiIdentity.getDevId())
                .add(aiIdentity.getLanguage().toString())
                .add(aiIdentity.getServerVersion())
                .executeQuery();

        BackendStatus status = new BackendStatus();
        while (rs.next()) {
            Pair<ServiceIdentity, BackendEngineStatus> update = getBackendEngineStatus(rs);
            if (update != null) {
                status.setEngineStatus(update.getA().getServerType(), update.getB());
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
    static Map<UUID, Map<String, BackendStatus>> getAllAIsStatusHashMap(
            final UUID devid,
            final DatabaseCall call) throws DatabaseException, SQLException {
        ResultSet rs = call.initialise("getAIsStatus", 1)
                .add(devid)
                .executeQuery();

        HashMap<UUID, Map<String, BackendStatus>> statuses = new HashMap<>();
        while (rs.next()) {
            UUID aiid = UUID.fromString(rs.getString("aiid"));
            Pair<ServiceIdentity, BackendEngineStatus> serverStatus = getBackendEngineStatus(rs);
            if (serverStatus != null) {
                String version = serverStatus.getA().getVersion();

                if (!statuses.containsKey(aiid)) {
                    statuses.put(aiid, new HashMap<>());
                }
                BackendStatus status = new BackendStatus();
                status.setEngineStatus(serverStatus.getA().getServerType(), serverStatus.getB());
                statuses.get(aiid).put(version, status);
            }
        }
        return statuses;
    }
}
