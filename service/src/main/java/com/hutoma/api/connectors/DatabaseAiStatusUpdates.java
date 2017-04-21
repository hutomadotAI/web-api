package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.TrainingStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
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
            ResultSet rs = transaction.getDatabaseCall().initialise("getAiStatusAll", 0)
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
                        BackendStatus statusBlockInDB = getBackendStatus(
                                rs.getString("backend_status"), jsonSerializer);
                        // get the status for the ai that we have in the db
                        TrainingStatus statusInDb = statusBlockInDB
                                .getEngineStatus(serverType)
                                .getTrainingStatus();
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
                                            serverType.toString(), statusInDb.toString(),
                                            statusOnBackend.toString(), aiid.toString()),
                                    null, LogMap.map("AIEngine", serverType.toString())
                                            .put("AIID", aiid.toString())
                                            .put("DEVID", devid.toString())
                                            .put("ApiStatus", finalStatusInDb1.toString())
                                            .put("BackendStatus", finalStatusOnBackend.toString()));

                            // keep everything but update the status
                            statusBlockInDB.updateEngineStatus(serverType, statusOnBackend);

                            // write the json block back to the AI table
                            transaction.getDatabaseCall().initialise("updateAiStatus", 3)
                                    .add(aiid)
                                    .add(devid)
                                    .add(jsonSerializer.serialize(statusBlockInDB))
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
}
