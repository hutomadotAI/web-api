package com.hutoma.api.common;

import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;

import org.glassfish.jersey.client.JerseyClient;

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

    @Inject
    public AiServiceStatusLogger(final JerseyClient jerseyClient, final JsonSerializer serializer,
                                 final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getLoggingServiceUrl(), config.getElasticSearchLoggingUrl(),
                SERVICESTATUS_LOGGING_CADENCE);
    }

    public void logStatusUpdate(final String logFrom, final AiStatus status, final String serverIdentifier) {
        LogParameters logParameters = new LogParameters("UpdateAIStatus") {{
            this.put(AIENGINE, status.getAiEngine());
            this.put(AIID, status.getAiid());
            this.put(DEVID, status.getDevId());
            this.put(STATUS, status.getTrainingStatus());
            this.put(ERROR, status.getTrainingError());
            this.put(TRAININGPROGRESS, status.getTrainingProgress());
            this.put(SERVER, serverIdentifier);
            this.put("AIHash", status.getAiHash());
        }};
        String narrative = String.format("Update %s status %s progress %d%% on ai %s",
                logParameters.get(AIENGINE),
                logParameters.get(STATUS),
                (int) (status.getTrainingProgress() * 100.0),
                logParameters.get(AIID));
        this.logUserTraceEvent(logFrom, narrative, null, logParameters);
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
        this.logUserTraceEvent(logFrom, narrative, null, logParameters);
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
        this.logUserTraceEvent(logFrom, narrative, null, logParameters);
    }

    public void logDbSyncUnknownAi(String logFrom, BackendServerType serverType, ServerAiEntry aiEntry) {
        LogParameters logParameters = new LogParameters("DbSyncStatus") {{
            this.put(AIENGINE, serverType.toString());
            this.put(AIID, aiEntry.getAiid().toString());
            this.put("BackendStatus", aiEntry.getTrainingStatus().toString());
        }};
        this.logUserWarnEvent(logFrom, String.format("%s reports ai %s that is unknown to us",
                logParameters.get(AIENGINE),
                logParameters.get(AIID)),
                null, logParameters);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
