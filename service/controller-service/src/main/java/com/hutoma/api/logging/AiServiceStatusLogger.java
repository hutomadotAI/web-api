package com.hutoma.api.logging;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;

import java.util.UUID;

/**
 * AI Service Status Logger.
 */
public class AiServiceStatusLogger extends CentralLogger {

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
    private static final String ENGINELANGUAGE = "Language";
    private static final String ENGINEVERSION = "Version";
    private static final String OPERATION = "Op";

    private static String logUuid(UUID uuid) {
        return (uuid == null) ? "null" : uuid.toString().substring(0, 7);
    }

    public void logStatusUpdate(final String logFrom, final AiStatus status, final boolean actioned) {
        LogParameters logParameters = new LogParameters("UpdateAIStatus") {{
            this.put(AIENGINE, status.getAiEngine().value());
            this.put(AIID, status.getAiid());
            this.put(DEVID, status.getDevId());
            this.put(STATUS, status.getTrainingStatus().value());
            this.put(ERROR, status.getTrainingError());
            this.put(TRAININGPROGRESS, status.getTrainingProgress());
            this.put(SERVER, status.getServerIdentifier());
            this.put("AIHash", status.getAiHash());
        }};
        String narrative = String.format("%s%s status update %s %d%% on ai %s from %s",
                (actioned ? "" : "IGNORED "),
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
            this.put(AIENGINE, serverType.value());
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

    public void logDebugQueueAction(final String logFrom,
                                    final String operation,
                                    final ServiceIdentity serviceIdentity,
                                    final UUID aiid,
                                    final UUID devid,
                                    final String serverIdentifier) {
        LogMap logMap = LogMap.map("Action", "Queue")
                .put(OPERATION, operation)
                .put(AIENGINE, serviceIdentity.getServerType().value())
                .put(ENGINELANGUAGE, serviceIdentity.getLanguage().toString())
                .put(ENGINEVERSION, serviceIdentity.getVersion())
                .put(DEVID, devid)
                .put(AIID, aiid);
        this.logDebug(logFrom,
                String.format("Processing %s %s ai %s on %s",
                        serviceIdentity.toString(), operation, logUuid(aiid), serverIdentifier),
                logMap);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
