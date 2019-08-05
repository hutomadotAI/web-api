package com.hutoma.api.common;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public static String getHashedDigest(final byte[] bytes) {
        String hashedDigest = null;
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            shaDigest.update(generateSalt());
            byte[] digestedBytes = shaDigest.digest(bytes);
            return Hex.encodeHexString(digestedBytes);
        } catch (NoSuchAlgorithmException ex) {
            // Every implementation of the Java platform is required to support MD5, SHA-1 and SHA-256
            // https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
        }
        return hashedDigest;
    }

    public static String getHashedDigest(final String string) {
        if (string == null) {
            return null;
        }
        return getHashedDigest(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String getHashedDigest(final UUID uuid) {
        String result = null;
        if (uuid != null) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            result = getHashedDigest(bb.array());
        }
        return result;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void threadSleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    public static List<String> getListFromMultipeValuedParam(final List<String> values) {
        // JAX-RS doesnt's support params with multiple values comma separated
        List<String> list = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            values.forEach(x -> {
                if (!x.isEmpty()) {
                    list.addAll(Arrays.asList(x.split(",")));
                }
            });
        }
        return list;
    }

    public static boolean isNumber(final String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int sz = text.length();
        for (int i = 0; i < sz; ++i) {
            char theChar = text.charAt(i);
            if (!Character.isDigit(theChar) && theChar != '.') {
                if (theChar == '-' && i == 0) { // allow negative numbers
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public static String getCsvFriendlyField(final String text) {
        final String textQualifier = "\"";
        final String textQualifierEscaped = "\"\"";

        // Check if it's a number
        if (isNumber(text)) {
            return text;
        }

        // Check if it's a date
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setLenient(false);
        try {
            Date date = df.parse(text);
            SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd HH:mm:ss");
            return sdf.format(date);
        } catch (ParseException e) {
            // nothing to do, it's just not a date
        }

        StringBuilder sb = new StringBuilder();
        sb.append(textQualifier);
        sb.append(text.replace(textQualifier, textQualifierEscaped));
        sb.append(textQualifier);
        return sb.toString();
    }

    public static double toOneDecimalPlace(double input) {
        return Math.round(input * 10.0d) / 10.0d;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
