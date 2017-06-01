package com.hutoma.api.common;

import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;

import org.glassfish.jersey.client.JerseyClient;

import java.util.UUID;
import javax.inject.Inject;

/**
 * AI Service Status Logger.
 */
public class AiServiceStatusLogger extends CentralLogger {

    // Log chat iterations every 10 seconds
    private static final int SERVICESTATUS_LOGGING_CADENCE = 8000;

    private static final String APP_ID = "API-servicesStatus-v1";

    private static final String AIENGINE = "AIEngine";
    private static final String AIID = "AIID";
    private static final String DEVID = "DEVID";
    private static final String STATUS = "Status";
    private static final String ERROR = "TrainingError";
    private static final String TRAININGPROGRESS = "TrainingProgress";
    private static final String AICOUNT = "AiCount";
    private static final String AICOUNTUPDATED = "AiCountUpdated";
    private static final String SERVER = "Server";
    private static final String OPERATION = "Op";

    @Inject
    public AiServiceStatusLogger(final JerseyClient jerseyClient, final JsonSerializer serializer,
                                 final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getElasticSearchLoggingUrl(), SERVICESTATUS_LOGGING_CADENCE);
    }

    public static String logUuid(UUID uuid) {
        return (uuid == null) ? "null" : uuid.toString().substring(0, 7);
    }

    public void logStatusUpdate(final String logFrom, final AiStatus status) {
        LogParameters logParameters = new LogParameters("UpdateAIStatus") {{
            this.put(AIENGINE, status.getAiEngine());
            this.put(AIID, status.getAiid());
            this.put(DEVID, status.getDevId());
            this.put(STATUS, status.getTrainingStatus().value());
            this.put(ERROR, status.getTrainingError());
            this.put(TRAININGPROGRESS, status.getTrainingProgress());
            this.put(SERVER, status.getServerIdentifier());
            this.put("AIHash", status.getAiHash());
        }};
        String narrative = String.format("%s status update %s %d%% on ai %s from %s",
                logParameters.get(AIENGINE),
                logParameters.get(STATUS),
                (int) (status.getTrainingProgress() * 100.0),
                logUuid(status.getAiid()),
                logParameters.get(SERVER));
        this.logUserInfoEvent(logFrom, narrative, null, new LogMap(logParameters));
    }

    public void logAffinityUpdate(final String logFrom, final BackendServerType updated,
                                  final ServerAffinity serverAffinity) {
        LogParameters logParameters = new LogParameters("Affinity") {{
            this.put(AIENGINE, updated);
            this.put("SessionID", serverAffinity.getServerSessionID());
            this.put(AICOUNT, serverAffinity.getAiList().size());
        }};
        String narrative = String.format("%s affinity list update with %s items",
                logParameters.get(AIENGINE),
                logParameters.get(AICOUNT));
        this.logUserInfoEvent(logFrom, narrative, null, new LogMap(logParameters));
    }

    public void logDbSyncComplete(final String logFrom, final BackendServerType serverType,
                                  final int itemsDatabase, final int itemsServerReg, final int itemsChangedStatus) {
        LogParameters logParameters = new LogParameters("DbSyncStatus") {{
            this.put("AiCountDatabase", itemsDatabase);
            this.put("AiCountServer", itemsServerReg);
            this.put(AICOUNTUPDATED, itemsChangedStatus);
            this.put(AIENGINE, serverType);
        }};
        String narrative = String.format("%s server db-sync complete. %s items updated.",
                logParameters.get(AIENGINE),
                logParameters.get(AICOUNTUPDATED).toString());
        this.logUserInfoEvent(logFrom, narrative, null, new LogMap(logParameters));
    }

    public void logDbSyncUnknownAi(String logFrom, BackendServerType serverType, ServerAiEntry aiEntry) {
        LogParameters logParameters = new LogParameters("DbSyncStatus") {{
            this.put(AIENGINE, serverType.value());
            this.put(AIID, aiEntry.getAiid());
            this.put("BackendStatus", aiEntry.getTrainingStatus().toString());
        }};
        this.logUserWarnEvent(logFrom, String.format("%s reports ai %s that is unknown to us",
                logParameters.get(AIENGINE),
                logUuid(aiEntry.getAiid())),
                null, new LogMap(logParameters));
    }

    public void logDebugQueueAction(String logFrom, String operation, BackendServerType serverType,
                                    UUID aiid, UUID devid, String serverIdentifier) {
        LogMap logMap = LogMap.map("Action", "Queue")
                .put(OPERATION, operation)
                .put(AIENGINE, serverType.value())
                .put(DEVID, devid)
                .put(AIID, aiid);
        this.logDebug(logFrom,
                String.format("Processing %s %s ai %s on %s",
                        serverType.value(), operation, logUuid(aiid), serverIdentifier),
                logMap);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
