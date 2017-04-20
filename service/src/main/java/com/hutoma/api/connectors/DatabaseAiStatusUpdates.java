package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerEndpointTrainingSlots;
import com.hutoma.api.containers.sub.TrainingStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseAiStatusUpdates extends Database {

    protected static final String LOGFROM = "db_status";

    private final AiServiceStatusLogger aiServicesLogger;

    @Inject
    public DatabaseAiStatusUpdates(final AiServiceStatusLogger logger,
                                   final Provider<DatabaseCall> callProvider,
                                   final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
        this.aiServicesLogger = logger;
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

    /***
     * Gets a summary of training slot status
     * i.e. for each individual server endpoint a count of active used training slots
     * and a count of used training slots that have not been updated in a while
     * @param serverType
     * @return
     * @throws DatabaseException
     */
    public List<ServerEndpointTrainingSlots> getQueueSlotCounts(BackendServerType serverType) throws DatabaseException {

        List<ServerEndpointTrainingSlots> slots = new ArrayList<>();
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            ResultSet rs = transaction.getDatabaseCall().initialise("queueCountSlots", 2)
                    .add(serverType.value())
                    .add(TrainingStatus.AI_TRAINING.value())
                    .executeQuery();

            while (rs.next()) {
                String endpoint = rs.getString("server_endpoint");
                int training = rs.getInt("training");
                int lapsed = rs.getInt("lapsed");
                slots.add(new ServerEndpointTrainingSlots(endpoint, training, lapsed));
            }

            transaction.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return slots;
    }

    /***
     * Takes the next queued item off the queue and returns it
     * @param serverType
     * @return
     * @throws DatabaseException
     */
    public BackendEngineStatus queueTakeNext(BackendServerType serverType) throws DatabaseException {

        BackendEngineStatus backendEngineStatus = null;
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            ResultSet rs = transaction.getDatabaseCall().initialise("queueTakeNext", 1)
                    .add(serverType.value())
                    .executeQuery();

            if (rs.next()) {
                backendEngineStatus = this.getBackendEngineStatus(rs).getB();
            }

            transaction.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return backendEngineStatus;
    }

    /***
     * Gets the full status of an item in the status table
     * Including its devid and whether the AI was deleted or not
     * @param serverType
     * @param aiid
     * @return
     * @throws DatabaseException
     */
    public BackendEngineStatus getAiQueueStatus(BackendServerType serverType, UUID aiid) throws DatabaseException {

        BackendEngineStatus backendEngineStatus = null;
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            ResultSet rs = transaction.getDatabaseCall().initialise("getAIQueueStatus", 2)
                    .add(serverType.value())
                    .add(aiid)
                    .executeQuery();
            if (rs.next()) {
                backendEngineStatus = getBackendEngineStatus(rs).getB();
                // also read the devid and the deleted flag from the ai table
                backendEngineStatus.setDevId(rs.getString("dev_id"));
                backendEngineStatus.setDeleted(rs.getBoolean("deleted"));
            }
            // if all goes well, commit
            transaction.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return backendEngineStatus;
    }

    public void deleteAiStatus(BackendServerType serverType, UUID aiid) throws DatabaseException {
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            transaction.getDatabaseCall().initialise("deleteAIStatus", 2)
                    .add(serverType.value())
                    .add(aiid)
                    .executeUpdate();
            // if all goes well, commit
            transaction.commit();
        }
    }

    /***
     * Compares the AI list reported by a registering server to the one in our database
     * Logs any differences and changes the statuses in the database to reflect
     * what was sent by the backend server
     * @param jsonSerializer
     * @param serverType which server are we dealing with?
     * @param serverReported aiid->entry map
     * @param excludeBots ignore any mention of bots on this list
     * @throws DatabaseException
     */
    public void synchroniseDBStatuses(final JsonSerializer jsonSerializer,
                                      final BackendServerType serverType,
                                      final Map<UUID, ServerAiEntry> serverReported,
                                      final Set<UUID> excludeBots)
            throws DatabaseException {

        int itemsDatabase = 0;
        int itemsServerReg = serverReported.size();
        int itemsChangedStatus = 0;

        // open a transaction to hold the statuses while we update
        // this is repeatable read so we are effectively locking the AI table
        // while this operation is underway
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            // read the status json for all the servers
            ResultSet rs = transaction.getDatabaseCall().initialise("getAIsServerStatus", 1)
                    .add(serverType.value())
                    .executeQuery();

            while (rs.next()) {

                // copies of parsed UUIDs for logging purposes if things go wrong
                UUID logAiid = null;
                UUID logDevid = null;
                itemsDatabase++;

                try {
                    // parse the data from the db rows
                    UUID aiid = UUID.fromString(rs.getString("aiid"));
                    logAiid = aiid;

                    // if we have an exclusion list and this bot is on it
                    if ((excludeBots == null) || !excludeBots.contains(aiid)) {

                        UUID devid = UUID.fromString(rs.getString("dev_id"));
                        logDevid = devid;

                        // get the status for the ai that we have in the db
                        TrainingStatus statusInDb = TrainingStatus.forValue(
                                rs.getString("training_status"));
                        // no status is the equivalent of UNDEFINED
                        statusInDb = (statusInDb == null) ? TrainingStatus.AI_UNDEFINED : statusInDb;

                        // read and remove the server entry for this AI
                        ServerAiEntry serverEntry = serverReported.remove(aiid);
                        TrainingStatus statusOnBackend = null;

                        // if there is no entry then log the missing info
                        // and later set our status to UNDEFINED
                        if (serverEntry == null) {
                            TrainingStatus finalStatusInDb = statusInDb;
                            this.logger.logUserWarnEvent(LOGFROM, String.format("%s did not report ai %s",
                                    serverType.toString(), aiid.toString()), null,
                                    LogMap.map("AIEngine", serverType.toString())
                                            .put("AIID", aiid.toString())
                                            .put("DEVID", devid.toString())
                                            .put("ApiStatus", finalStatusInDb.toString()));
                        } else {
                            statusOnBackend = serverEntry.getTrainingStatus();
                        }

                        // no status is the equivalent of UNDEFINED
                        statusOnBackend = (statusOnBackend == null) ? TrainingStatus.AI_UNDEFINED : statusOnBackend;

                        // if the status is not what we are expecting
                        if (statusInDb != statusOnBackend) {
                            itemsChangedStatus++;
                            // log the difference
                            TrainingStatus finalStatusOnBackend = statusOnBackend;
                            TrainingStatus finalStatusInDb1 = statusInDb;
                            this.logger.logUserWarnEvent(LOGFROM,
                                    String.format("%s status mismatch. Updating from %s to %s for ai %s",
                                            serverType.value(), statusInDb.toString(),
                                            statusOnBackend.toString(), aiid.toString()),
                                    null, LogMap.map("AIEngine", serverType.toString())
                                            .put("AIID", aiid.toString())
                                            .put("DEVID", devid.toString())
                                            .put("ApiStatus", finalStatusInDb1.toString())
                                            .put("BackendStatus", finalStatusOnBackend.toString()));

                            // keep everything but update the status
                            transaction.getDatabaseCall().initialise("updateAiStatus", 6)
                                    .add(serverType.value())
                                    .add(aiid)
                                    .add(statusOnBackend.value())
                                    .add(rs.getString("server_endpoint"))
                                    .add(rs.getDouble("training_progress"))
                                    .add(rs.getDouble("training_error"))
                                    .executeUpdate();
                        }
                    }
                } catch (IllegalArgumentException | JsonParseException e) {
                    // a single ai entry in the database is corrupt
                    this.logger.logUserExceptionEvent(LOGFROM, String.format("Bad db entry for aiid=%s devid=%s",
                            (logAiid == null) ? "(bad)" : logAiid.toString(),
                            (logDevid == null) ? "(bad)" : logDevid.toString()), null,
                            e);
                }
            }

            // anything that we have left is a server reported by the backend
            // that we have no entry for in our database
            serverReported.values().forEach(remaining -> {
                // log it
                this.aiServicesLogger.logDbSyncUnknownAi(LOGFROM, serverType, remaining);
            });

            // log completion
            this.aiServicesLogger.logDbSyncComplete(LOGFROM, serverType,
                    itemsDatabase, itemsServerReg, itemsChangedStatus);

            // if all goes well, commit
            transaction.commit();

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Log the controller state to the database
     * so that it can be picked up by monitoring tools
     * @param serverType
     * @param serverCount how many verified servers
     * @param totalTrainingCapacity how many total training slots
     * @param availableTrainingSlots how many slots are available for use
     * @param totalChatCapacity how many total chat slots
     * @throws DatabaseException
     */
    public void updateControllerState(final BackendServerType serverType, final int serverCount,
                                      final int totalTrainingCapacity, final int availableTrainingSlots,
                                      final int totalChatCapacity) throws DatabaseException {

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {
            transaction.getDatabaseCall().initialise("updateControllerState", 5)
                    .add(serverType.value())
                    .add(serverCount)
                    .add(totalTrainingCapacity)
                    .add(availableTrainingSlots)
                    .add(totalChatCapacity)
                    .executeUpdate();
            transaction.commit();
        }
    }
}
