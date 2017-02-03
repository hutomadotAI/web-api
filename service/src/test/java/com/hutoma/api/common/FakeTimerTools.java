package com.hutoma.api.common;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by David MG on 08/08/2016.
 */
public class FakeTimerTools extends Tools {

    final static long TIMERSTART = 1000000L;
    Consumer<FakeTimerTools> behaviour;
    private long timer;

    public FakeTimerTools() {
        this.timer = TIMERSTART;
    }

    @Override
    public UUID createNewRandomUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public long getTimestamp() {
        return this.timer;
    }

    @Override
    public void threadSleep(long milliseconds) {
        this.timer += milliseconds;
        if (this.behaviour != null) {
            this.behaviour.accept(this);
        }
    }

    public long getElapsedTime() {
        return this.timer - TIMERSTART;
    }

    public void setBehaviour(Consumer<FakeTimerTools> behaviour) {
        this.behaviour = behaviour;
    }

}
