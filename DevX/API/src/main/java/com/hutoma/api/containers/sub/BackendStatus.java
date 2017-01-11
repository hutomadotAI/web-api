package com.hutoma.api.containers.sub;

import java.util.HashMap;

/**
 * Created by David MG on 03/01/2017.
 */
public class BackendStatus {

    HashMap<String, BackendEngineStatus> engines;

    /***
     * Initialising BackendStatus effectively sets all engines to state AI_UNDEFINED,
     * which means that the AI has no training file
     */
    public BackendStatus() {
        this.engines = new HashMap<>();
    }

    public BackendEngineStatus getEngineStatus(String engine) {
        BackendEngineStatus status = this.engines.get(engine);
        return (status == null) ? new BackendEngineStatus() : status;
    }

    public void setEngineStatus(AiStatus aiStatus) {
        this.engines.put(aiStatus.getAiEngine(), new BackendEngineStatus(aiStatus));
    }

}
