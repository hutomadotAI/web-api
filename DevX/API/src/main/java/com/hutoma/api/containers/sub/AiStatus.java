package com.hutoma.api.containers.sub;

import java.util.UUID;

/**
 * Created by pedrotei on 08/11/16.
 */
public class AiStatus {

    private final String aiid;
    private final String dev_id;
    private final String trainingStatus;

    public AiStatus(final String devId, final UUID aiid, final TrainingStatus trainingStatus) {
        this.dev_id = devId;
        this.aiid = aiid.toString();
        this.trainingStatus = trainingStatus.value();
    }

    public UUID getAiid() {
        return this.aiid == null ? null : UUID.fromString(this.aiid);
    }

    public String getDevId() {
        return this.dev_id;
    }

    public TrainingStatus getTrainingStatus() {
        return TrainingStatus.forValue(this.trainingStatus);
    }
}
