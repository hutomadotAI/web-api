package com.hutoma.api.common;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Tools {
    private SecureRandom secureRandom = new SecureRandom();

    public String getCallerMethod(final int depth) {
        StackTraceElement[] elements = new Throwable().fillInStackTrace().getStackTrace();
        if (depth >= elements.length) {
            return elements[elements.length - 1].getMethodName();
        }
        return elements[depth].getMethodName();
    }

    public String getCallerMethod() {
        return getCallerMethod(2);
    }

    public UUID createNewRandomUUID() {
        return java.util.UUID.randomUUID();
    }

    /**
     * Generates random hex string from a SecureRandom object.
    */
    public String generateRandomHexString(int length) {
        byte[] bytesToFill = new byte[length / 2];
        secureRandom.nextBytes(bytesToFill);
        String hexString = Hex.encodeHexString(bytesToFill);
        return hexString;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void threadSleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
