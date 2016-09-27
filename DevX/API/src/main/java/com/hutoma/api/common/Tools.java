package com.hutoma.api.common;

import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Tools {

    public UUID createNewRandomUUID() {
        return java.util.UUID.randomUUID();
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void threadSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

}
