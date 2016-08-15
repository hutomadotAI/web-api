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

    /**
     * Returns the same string with anything over char 127 or below char 32 removed
     * Also, ()[]<> removed and quotes replaces by backslash-quotes
     * Whitespaces are deduped and the string is trimmed of leading and trailing whitespaces.
     * @param input   abc[]<>()  abc
     * @return abc abc
     */
    public String textSanitizer(String input) {
        // null check, fast bail
        if (null==input) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int n=input.length();
        boolean lastCharWasSpace = true;
        char c;
        for (int i=0; i<n; i++) {
            c = input.charAt(i);
            // all whitespaces
            if (Character.isWhitespace(c)) {
                if (!lastCharWasSpace) {
                    sb.append(' ');
                    lastCharWasSpace = true;
                }
            } else {
                // ignore out of range characters
                if ((c>=32) && (c<128)) {
                    switch(c) {
                        // characters to escape
                        case '\'':
                        case '\"':
                            sb.append('\\').append(c);
                            lastCharWasSpace = false;
                            break;
                        // characters to omit
                        case '(':
                        case ')':
                        case '[':
                        case ']':
                        case '<':
                        case '>':
                            break;
                        // characters to retain unchanged
                        default:
                            sb.append(c);
                            lastCharWasSpace = false;
                    }
                }
            }
        }
        // removed trailing space if present
        if ((lastCharWasSpace) && (sb.length()>0)) {
            sb.setLength((sb.length()-1));
        }
        return sb.toString();
    }
}
