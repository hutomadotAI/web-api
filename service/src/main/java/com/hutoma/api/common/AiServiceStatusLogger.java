package com.hutoma.api.common;

import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerAffinity;

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

    @Inject
    public AiServiceStatusLogger(final JerseyClient jerseyClient, final JsonSerializer serializer,
                                 final Config config) {
        super(jerseyClient, serializer);
        this.startLoggingScheduler(config.getLoggingServiceUrl(), SERVICESTATUS_LOGGING_CADENCE);
    }

    public void logStatusUpdate(final String logFrom, final AiStatus status) {
        LogParameters logParameters = new LogParameters("UpdateAIStatus") {{
            this.put(AIENGINE, status.getAiEngine());
            this.put(AIID, status.getAiid());
            this.put(DEVID, status.getDevId());
            this.put(STATUS, status.getTrainingStatus());
            this.put(ERROR, Double.toString(status.getTrainingError()));
            this.put(TRAININGPROGRESS, Double.toString(status.getTrainingProgress()));
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
            this.put(AICOUNT, Integer.toString(serverAffinity.getAiList().size()));
        }};
        String narrative = String.format("%s affinity list update with %s items",
                logParameters.get(AIENGINE),
                logParameters.get(AICOUNT));
        this.logUserTraceEvent(logFrom, narrative, null, logParameters);
    }

    public void logDbSyncComplete(final String logFrom, final BackendServerType serverType,
                                  final int itemsDatabase, final int itemsServerReg, final int itemsChangedStatus) {
        LogParameters logParameters = new LogParameters("DbSyncStatus") {{
            this.put("AiCountDatabase", Integer.toString(itemsDatabase));
            this.put("AiCountServer", Integer.toString(itemsServerReg));
            this.put(AICOUNTUPDATED, Integer.toString(itemsChangedStatus));
            this.put(AIENGINE, serverType);
        }};
        String narrative = String.format("%s server db-sync complete. %s items updated.",
                logParameters.get(AIENGINE),
                logParameters.get(AICOUNTUPDATED));
        this.logUserTraceEvent(logFrom, narrative, null, logParameters);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
