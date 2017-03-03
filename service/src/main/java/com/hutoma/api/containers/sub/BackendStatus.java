package com.hutoma.api.containers.sub;

import java.util.HashMap;

/**
 * Created by David MG on 03/01/2017.
 */
public class BackendStatus {

    private HashMap<BackendServerType, BackendEngineStatus> engines;

    /***
     * Initialising BackendStatus effectively sets all engines to state AI_UNDEFINED,
     * which means that the AI has no training file
     */
    public BackendStatus() {
        this.engines = new HashMap<>();
    }

    public BackendEngineStatus getEngineStatus(BackendServerType engine) {
        BackendEngineStatus status = this.engines.get(engine);
        return (status == null) ? new BackendEngineStatus() : status;
    }

    /***
     * For a specific engine, change the training status to the one provided
     * without affecting the other parameters
     * @param engine
     * @param trainingStatus
     */
    public void updateEngineStatus(BackendServerType engine, TrainingStatus trainingStatus) {
        this.engines.computeIfAbsent(engine, key -> new BackendEngineStatus())
                .setTrainingStatus(trainingStatus);
    }

    public void setEngineStatus(AiStatus aiStatus) {
        this.engines.put(aiStatus.getAiEngine(), new BackendEngineStatus(aiStatus));
    }

}
