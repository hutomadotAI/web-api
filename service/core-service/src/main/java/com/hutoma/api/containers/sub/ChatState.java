package com.hutoma.api.containers.sub;

import com.hutoma.api.connectors.chat.AIChatServices;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.UUID;

/**
 * Chat state.
 */
public class ChatState {
    private DateTime timestamp;
    private UUID lockedAiid;
    private String topic;
    private String history;
    private HashMap<String, String> entityValues;
    private double confidenceThreshold;
    private ChatHandoverTarget chatTarget;
    private AIChatServices aiChatServices;

    public ChatState(final DateTime timestamp, final String topic, final String history, final UUID lockedAiid,
                     final HashMap<String, String> entityValues, final double confidenceThreshold,
                     final ChatHandoverTarget chatTarget) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.history = history;
        this.lockedAiid = lockedAiid;
        this.entityValues = entityValues;
        this.confidenceThreshold = confidenceThreshold;
        this.chatTarget = chatTarget;
    }

    public static ChatState getEmpty() {
        return new ChatState(
                null, null, null, null, new HashMap<>(), 0.0d, ChatHandoverTarget.Ai
        );
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

    public String getHistory() {
        return this.history;
    }

    public void setHistory(final String history) {
        this.history = history;
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

    public void setConfidenceThreshold(final double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public double getConfidenceThreshold() {
        return this.confidenceThreshold;
    }

    public ChatHandoverTarget getChatTarget() {
        return this.chatTarget;
    }

    public void setChatTarget(final ChatHandoverTarget target) {
        this.chatTarget = target;
    }

    public void setAiChatServices(final AIChatServices aiChatServices) {
        this.aiChatServices = aiChatServices;
    }

    public AIChatServices getAiChatServices() {
        return this.aiChatServices;
    }
}
