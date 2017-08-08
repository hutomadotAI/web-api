package com.hutoma.api.common;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Tools {
    // Initialise SecureRandom once as this is potentially a lengthy operation
    private static SecureRandom secureRandom = new SecureRandom();

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
        return Hex.encodeHexString(bytesToFill);
    }

    public static String getHashedDigestFromUuid(final UUID uuid) {
        String result = null;
        if (uuid != null) {
            try {
                ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
                bb.putLong(uuid.getMostSignificantBits());
                bb.putLong(uuid.getLeastSignificantBits());
                MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
                byte[] resultArray = shaDigest.digest(bb.array());
                result = Hex.encodeHexString(resultArray);
            } catch (NoSuchAlgorithmException ex) {
                // Every implementation of the Java platform is required to support MD5, SHA-1 and SHA-256
                // https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
            }
        }
        return result;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void threadSleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
