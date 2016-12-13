package com.hutoma.api.common;

import com.hutoma.api.common.Tools;

import java.util.UUID;

/**
 * Created by David MG on 08/08/2016.
 */
public class FakeTimerTools extends Tools {

    long timer;

    public FakeTimerTools() {
        timer = 0;
    }

    @Override
    public UUID createNewRandomUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public long getTimestamp() {
        return timer;
    }

    @Override
    public void threadSleep(long milliseconds) {
        timer += milliseconds;
    }
}
