package com.hutoma.api.connectors;

import com.hutoma.api.containers.sub.AiStatus;

import java.util.HashMap;

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

    public void setEngineStatus(final AiStatus aiStatus) {
        this.engines.put(aiStatus.getAiEngine(), new BackendEngineStatus(aiStatus));
    }

    public void setEngineStatus(final BackendServerType engine, final BackendEngineStatus status) {
        this.engines.put(engine, status);
    }
}