package com.hutoma.api.endpoints;

import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by paul on 19/07/17.
 */
public final class HeaderUtils {
    public static Map<String, String> getChatVariablesFromHeaders(MultivaluedMap<String, String> headers) {
        final String HEADER_PREFIX = "x-hutoma-context-";
        final int HEADER_PREFIX_LENGTH = HEADER_PREFIX.length();

        Map<String, String> chatHeaders = headers.entrySet().stream()
                // find headers that start with the prefix and have only one entry (not duplicated)
                .filter(entry -> entry.getKey().startsWith(HEADER_PREFIX) && entry.getValue().size() == 1)
                // strip the prefix from the header name
                .collect(Collectors.toMap(e -> {
                    String key = e.getKey();
                    String modifiedKey = key.substring(HEADER_PREFIX_LENGTH, key.length());
                    return modifiedKey;
                }, e-> {
                    String value = "INVALID_VALUE";
                    try {
                        // URL decode the first entry
                        value = URLDecoder.decode(e.getValue().get(0), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        // in practice this should never happen
                    }
                    return value;
                }));

        return chatHeaders;
    }
}
