package com.hutoma.api.containers.sub;

import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Chat state.
 */
public class ChatState {
    private DateTime timestamp;
    private UUID lockedAiid;
    private String topic;

    public ChatState(final DateTime timestamp, final String topic, final UUID lockedAiid) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.lockedAiid = lockedAiid;
    }

    public static ChatState getEmpty() {
        return new ChatState(null, null, null);
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
}
