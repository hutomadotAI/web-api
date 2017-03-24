package com.hutoma.api.common;

import java.util.LinkedHashMap;
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
        this.map = otherMap == null ? new LinkedHashMap<>() : new LinkedHashMap<>(otherMap);
    }

    /**
     * Create a map with the (key,value) pair.
     * @param key   the key
     * @param value the value
     * @return the LogMap
     */
    public static LogMap map(final String key, final Object value) {
        return new LogMap((LogMap) null).put(key, value);
    }

    /**
     * Returns a copy of the current map with the (key, value) pair added.
     * @param key   the key
     * @param value the value
     * @return a copy of the current map with the (key, value) pair added
     */
    public LogMap put(final String key, final Object value) {
        LogMap newMap = new LogMap(this);
        newMap.get().put(key, value);
        return newMap;
    }

    /**
     * Adds a (key, value) pair to the current map.
     * @param key   the key
     * @param value the value
     */
    public void add(final String key, final Object value) {
        this.map.put(key, value);
    }

    /**
     * Gets the map.
     * @return the map
     */
    public Map<String, Object> get() {
        return this.map;
    }
}
