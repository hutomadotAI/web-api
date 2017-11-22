package com.hutoma.api.common;

import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by David MG on 08/08/2016.
 */
public class FakeTimerTools extends Tools {

    final static long TIMERSTART = 1000000L;
    Consumer<FakeTimerTools> behaviour;
    private long timer;

    private LinkedList<UUID> uuids;

    public FakeTimerTools() {
        this.timer = TIMERSTART;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.uuids = new LinkedList<>();
        for (int i = 0; i < 16; i++) {
            char n = Integer.toHexString(i).charAt(0);
            char o = uuid.toString().charAt(0);
            this.uuids.add(UUID.fromString(uuid.toString().replace(o, n)));
        }
    }

    @Override
    public UUID createNewRandomUUID() {
        synchronized (this) {
            UUID uuid = this.uuids.removeFirst();
            this.uuids.addLast(uuid);
            return uuid;
        }
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
