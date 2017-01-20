package com.hutoma.api.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Tools {

    public boolean isStreamSmallerThan(final InputStream stream, final long size) throws IOException {
        final long skipped = stream.skip(size + 1);
        stream.reset();
        return skipped < size;
    }

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

    public long getTimestamp() {
        return System.currentTimeMillis();
    }
}
