package com.hutoma.api.connectors;

import java.util.UUID;

/**
 * Created by paul on 22/05/17.
 */
public final class AiDevId  {
    public UUID dev;
    public UUID ai;

    public AiDevId(UUID devid, UUID aiid) {
        this.dev = devid;
        this.ai = aiid;
    }
}
