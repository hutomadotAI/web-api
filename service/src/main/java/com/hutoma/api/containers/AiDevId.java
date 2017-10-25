package com.hutoma.api.containers;

import java.util.UUID;

/**
 * Created by paul on 22/05/17.
 */
public class AiDevId {
    private final UUID devId;
    private final UUID aiid;

    public AiDevId(final UUID devId, final UUID aiid) {
        this.devId = devId;
        this.aiid = aiid;
    }

    public UUID getDevId() {
        return this.devId;
    }

    public UUID getAiid() {
        return this.aiid;
    }
}
