package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.containers.ApiAi;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Chat state.
 */
public class ChatState {

    // Timestamp of the chat interaction
    @SerializedName("timestamp")
    private DateTime timestamp;
    // AIID for the bot currently locked for the conversation (until it has a low scoring answer)
    @SerializedName("lockedAiid")
    private UUID lockedAiid;
    // Current topic - TODO: never really used, consider removing
    @SerializedName("topic")
    private String topic;
    // Conversation history - TODO: only used in WNET, consider removing
    @SerializedName("history")
    private String history;
    @SerializedName("confidenceThreshold")
    private double confidenceThreshold;
    // Chat target (whether it's been handed over to another system or not)
    @SerializedName("chatTarget")
    private ChatHandoverTarget chatTarget;
    // Timestamp for when we should reset the handover and pass the control back to the AI
    @SerializedName("resetHandoverTime")
    private DateTime resetHandoverTime;
    // Number of low scoring answers in a row
    @SerializedName("badAnswersCount")
    private int badAnswersCount;
    // Current context for the conversation
    @SerializedName("chatContext")
    private ChatContext chatContext;
    // List of intents that are currently in flight
    @SerializedName("currentIntents")
    private List<MemoryIntent> currentIntents;
    @SerializedName("restart_chat_workflow")
    private boolean restartChatWorkflow;
    @SerializedName("in_intent_loop")
    private boolean inIntentLoop;
    @SerializedName("webhook_sessions")
    private List<WebHookSession> webhookSessions = new ArrayList<>();
    @SerializedName("integration_data")
    private IntegrationData integrationData;
    @SerializedName("intent_score")
    private double intentScore;

    // ChatServices service
    private transient AIChatServices aiChatServices;

    // We want to carry this structure around for convenience for the handlers (and avoid
    // having to re-load this from DB multiple times)
    private transient ApiAi ai;

    // Store the DevId for operations where this is not provided explicitly
    private transient UUID devId;

    private transient UUID chatId;
    private transient String hashedChatId;

    // Keep the potential entity values in memory for this loop
    private transient Map<String, List<String>> candidateValues;

    public ChatState(final DateTime timestamp, final String topic, final String history, final UUID lockedAiid,
                     final double confidenceThreshold, final ChatHandoverTarget chatTarget,
                     final ApiAi ai, final ChatContext chatContext) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.history = history;
        this.lockedAiid = lockedAiid;
        this.confidenceThreshold = confidenceThreshold;
        this.chatTarget = chatTarget;
        this.ai = ai;
        this.chatContext = chatContext;
        this.currentIntents = new ArrayList<>();
        this.candidateValues = new HashMap<>();
    }

    public static ChatState getEmpty() {
        return new ChatState(
                null, null, null, null, 0.0d, ChatHandoverTarget.Ai,
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

    public Map<String, List<String>> getCandidateValues() {
        return this.candidateValues;
    }

    public void clearFromCurrentIntents(final List<MemoryIntent> intentsToRemove) {
        this.currentIntents.removeAll(intentsToRemove);
    }

    public void restartChatWorkflow(final boolean restartChatWorkflow) {
        this.restartChatWorkflow = restartChatWorkflow;
    }

    public boolean isRestartChatWorkflow() {
        return this.restartChatWorkflow;
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

    public boolean isInIntentLoop() {
        return this.inIntentLoop;
    }

    public void setInIntentLoop(final boolean inIntentLoop) {
        this.inIntentLoop = inIntentLoop;
    }

    public List<WebHookSession> getWebhookSessions() {
        return this.webhookSessions;
    }

    public void setWebhookSessions(final List<WebHookSession> webhookSessions) {
        this.webhookSessions = webhookSessions;
    }

    public void setIntegrationData(final IntegrationData integrationData) {
        this.integrationData = integrationData;
    }

    public IntegrationData getIntegrationData() {
        return this.integrationData;
    }

    public UUID getDevId() {
        return this.devId;
    }

    public void setDevId(final UUID devId) {
        this.devId = devId;
    }

    public String getHashedChatId() {
        return this.hashedChatId;
    }

    public void setChatId(final UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getChatId() {
        return this.chatId;
    }

    public void setHashedChatId(final String hashedChatId) {
        this.hashedChatId = hashedChatId;
    }

    public double getIntentScore() {
        return this.intentScore;
    }

    public void setIntentScore(final double intentScore) {
        this.intentScore = intentScore;
    }
}
