package com.hutoma.api.logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper to handle maps for logs.
 */
public final class LogMap {

    private Map<String, Object> map = null;

    /**
     * Copy ctor.
     * @param other the other map to copy from
     */
    public LogMap(final LogMap other) {
        this(other == null ? null : other.get());
    }

    /**
     * Ctor.
     * @param otherMap the map to copy from
     */
    public LogMap(final Map<String, Object> otherMap) {

        this.map = otherMap == null ? new LinkedHashMap<>() : convertMapValues(otherMap);
    }

    /**
     * Clears the map.
     */
    public void clear() {
        this.map.clear();
    }

    private static LinkedHashMap<String, Object> convertMapValues(final Map<String, Object> otherMap) {
        if (otherMap == null) {
            return null;
        }
        LinkedHashMap<String, Object> convertedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : otherMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                convertedMap.put(entry.getKey(), convertMapValues((Map<String, Object>) entry.getValue()));
            } else {
                convertedMap.put(entry.getKey(), convertObject(entry.getValue()));
            }
        }
        return convertedMap;
    }

    /**
     * Create a map with the (key,value) pair.
     * @param key   the key
     * @param value the value
     * @return the LogMap
     */
    public static LogMap map(final String key, final Object value) {
        return new LogMap((LogMap) null).put(key, convertObject(value));
    }

    /**
     * Returns a copy of the current map with the (key, value) pair added.
     * @param key   the key
     * @param value the value
     * @return a copy of the current map with the (key, value) pair added
     */
    public LogMap put(final String key, final Object value) {
        LogMap newMap = new LogMap(this);
        newMap.get().put(key, convertObject(value));
        return newMap;
    }

    /**
     * If the object isn't a basic json serializable type, then return the string representation of it.
     * This is required for serialization mechanisms that don't handle complex types correctly.
     * @param obj the original object
     * @return the object or it's string representation, based on it's type
     */
    private static Object convertObject(final Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map || obj instanceof Integer || obj instanceof Boolean || obj instanceof String
                || obj instanceof Double || obj instanceof Float || obj instanceof List) {
            return obj;
        }
        return obj.toString();
    }

    /**
     * Adds a (key, value) pair to the current map.
     * @param key   the key
     * @param value the value
     */
    public void add(final String key, final Object value) {
        this.map.put(key, convertObject(value));
    }

    /**
     * Gets the map.
     * @return the map
     */
    public Map<String, Object> get() {
        return this.map;
    }
}
