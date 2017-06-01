package com.hutoma.api.containers.sub;

import org.joda.time.DateTime;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Chat state.
 */
public class ChatState {
    private DateTime timestamp;
    private UUID lockedAiid;
    private String topic;
    private HashMap<String, String> entityValues;

    public ChatState(final DateTime timestamp, final String topic, final UUID lockedAiid,
                         final HashMap<String, String> entityValues) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.lockedAiid = lockedAiid;
        this.entityValues = entityValues;
    }

    public static ChatState getEmpty() {
        return new ChatState(null, null, null, new HashMap<>());
    }

    public DateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getLockedAiid() {
        return this.lockedAiid;
    }

    public void setLockedAiid(final UUID lockedAiid) {
        this.lockedAiid = lockedAiid;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getEntityValue(String entityName) {
        return this.entityValues.getOrDefault(entityName, null);
    }

    public HashMap<String, String> getEntityValues() {
        return this.entityValues;
    }

    public void setEntityValue(String entityName, String value) {
        this.entityValues.put(entityName, value);
    }
}
