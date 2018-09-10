package com.hutoma.api.connectors;

import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David MG on 03/01/2017.
 */
public class BackendStatus {

    private HashMap<BackendServerType, BackendEngineStatus> engines;

    /***
     * Initialising BackendStatus effectively sets all engines to state AI_UNDEFINED
     */
    public BackendStatus() {
        this.engines = new HashMap<>();
    }

    public BackendEngineStatus getEngineStatus(final BackendServerType engine) {
        BackendEngineStatus status = this.engines.get(engine);
        return (status == null) ? new BackendEngineStatus() : status;
    }

    public boolean hasEngineStatus(final BackendServerType engine) {
        return this.engines.containsKey(engine);
    }

    public void setEngineStatus(final AiStatus aiStatus) {
        this.engines.put(aiStatus.getAiEngine(), new BackendEngineStatus(aiStatus));
    }

    public void setEngineStatus(final BackendServerType engine, final BackendEngineStatus status) {
        this.engines.put(engine, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<BackendServerType, BackendEngineStatus> entry : this.engines.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(entry.getKey().toString())
                    .append("=[TrainingStatus:")
                    .append(entry.getValue().getTrainingStatus().value())
                    .append(", QueueAction:")
                    .append(entry.getValue().getQueueAction().value())
                    .append(", TrainingProgress:")
                    .append(entry.getValue().getTrainingProgress())
                    .append(", TrainingError:")
                    .append(entry.getValue().getTrainingError())
                    .append("]");
        }
        return sb.toString();
    }
}
