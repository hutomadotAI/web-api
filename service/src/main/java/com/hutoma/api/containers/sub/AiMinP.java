package com.hutoma.api.containers.sub;

import com.hutoma.api.containers.AiDevId;

import java.util.UUID;

/**
 * Created by pedrotei on 19/06/17.
 */
public class AiMinP extends AiDevId {

    private final double minP;

    public AiMinP(final UUID devId, final UUID aiid, final double minP) {
        super(devId, aiid);
        this.minP = minP;
    }

    public double getMinP() {
        return this.minP;
    }
}
