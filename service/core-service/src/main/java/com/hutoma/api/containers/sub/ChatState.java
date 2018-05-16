package com.hutoma.api.containers.sub;

import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.containers.ApiAi;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Chat state.
 */
public class ChatState {

    // Timestamp of the chat interaction
    private DateTime timestamp;
    // AIID for the bot currently locked for the conversation (until it has a low scoring answer)
    private UUID lockedAiid;
    // Current topic - TODO: never really used, consider removing
    private String topic;
    // Conversation history - TODO: only used in WNET, consider removing
    private String history;
    private HashMap<String, String> entityValues;
    private double confidenceThreshold;
    // Chat target (whether it's been handed over to another system or not)
    private ChatHandoverTarget chatTarget;
    // ChatServices service
    private AIChatServices aiChatServices;
    // Timestamp for when we should reset the handover and pass the control back to the AI
    private DateTime resetHandoverTime;
    // Number of low scoring answers in a row
    private int badAnswersCount;
    // Current context for the conversation
    private ChatContext chatContext;
    // List of intents that are currently in flight
    private List<MemoryIntent> currentIntents;

    // We want to carry this structure around for convenience for the handlers (and avoid
    // having to re-load this from DB multiple times)
    private transient ApiAi ai;

    public ChatState(final DateTime timestamp, final String topic, final String history, final UUID lockedAiid,
                     final HashMap<String, String> entityValues, final double confidenceThreshold,
                     final ChatHandoverTarget chatTarget, final ApiAi ai, final ChatContext chatContext) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.history = history;
        this.lockedAiid = lockedAiid;
        this.entityValues = entityValues;
        this.confidenceThreshold = confidenceThreshold;
        this.chatTarget = chatTarget;
        this.ai = ai;
        this.chatContext = chatContext;
        this.currentIntents = new ArrayList<>();
    }

    public static ChatState getEmpty() {
        return new ChatState(
                null, null, null, null, new HashMap<>(), 0.0d, ChatHandoverTarget.Ai,
                null, new ChatContext()
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

    public void setAi(final ApiAi ai) {
        this.ai = ai;
    }

    public ApiAi getAi() {
        return this.ai;
    }

    public void setHandoverResetTime(final DateTime resetHandoverTime) {
        this.resetHandoverTime = resetHandoverTime;
    }

    public DateTime getResetHandoverTime() {
        return this.resetHandoverTime;
    }

    public int getBadAnswersCount() {
        return this.badAnswersCount;
    }

    public void setBadAnswersCount(final int badAnswersCount) {
        this.badAnswersCount = badAnswersCount;
    }

    public ChatContext getChatContext() {
        return this.chatContext;
    }

    public List<MemoryIntent> getCurrentIntents() {
        return this.currentIntents;
    }

    public void setCurrentIntents(final List<MemoryIntent> currentIntents) {
        this.currentIntents = currentIntents;
    }

    public void clearFromCurrentIntents(final List<MemoryIntent> intentsToRemove) {
        this.currentIntents.removeAll(intentsToRemove);
    }

    public MemoryIntent getMemoryIntent(final String intentName) {
        Optional<MemoryIntent> opt = this.currentIntents.stream()
                .filter(x -> x.getName().equals(intentName)).findFirst();
        return opt.orElse(null);
    }

    public void updateMemoryIntent(final MemoryIntent intent) {
        for (int i = 0; i < this.currentIntents.size(); i++) {
            if (intent.getName().equals(this.currentIntents.get(i).getName())) {
                this.currentIntents.set(i, intent);
                return;
            }
        }
        // not found, add it
        this.currentIntents.add(intent);
    }
}
