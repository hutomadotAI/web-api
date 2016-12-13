package com.hutoma.api.common;

import java.util.UUID;

/**
 * Created by David MG on 08/08/2016.
 */
public class FakeTimerTools extends Tools {

    long timer;

    public FakeTimerTools() {
        this.timer = 0;
    }

    @Override
    public UUID createNewRandomUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public long getTimestamp() {
        return this.timer;
    }
}
