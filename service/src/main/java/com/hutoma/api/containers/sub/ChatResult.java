package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 16/08/2016.
 */
public class ChatResult {

    @SerializedName("topicIn")
    private String topicIn = "";
    private double score;
    @SerializedName("query")
    private String query;
    private String answer = "";
    private String history = "";
    @SerializedName("elapsedTime")
    private double elapsedTime;
    private String action;
    private String context;
    @SerializedName("topic_out")
    private String topicOut = "";
    private UUID chatId;
    private List<MemoryIntent> intents;

    // result of a webhook call if one was made
    private transient WebHookResponse webHookResponse;

    // the actual bot ID that provided the result
    private transient UUID aiid;
    private transient boolean resetConversation;

    public ChatResult(final String query) {
        this.query = query;
    }

    /***
     * Constructor that takes a ChatResult and copies only the fields
     * that we want to pass to the API caller
     * @param source
     */
    public ChatResult(final ChatResult source) {
        this.topicIn = source.topicIn;
        this.score = source.score;
        this.query = source.query;
        this.answer = source.answer;
        this.history = source.history;
        this.elapsedTime = source.elapsedTime;
        this.action = source.action;
        this.context = source.context;
        this.topicOut = source.topicOut;
        this.chatId = source.chatId;
        this.aiid = source.aiid;
    }

    public ChatResult(final UUID chatId, final double score, final String query, final String answer,
                      final double elapsedTime, final WebHookResponse webHookResponse) {
        this.score = score;
        this.query = query;
        this.answer = answer;
        this.elapsedTime = elapsedTime;
        this.chatId = chatId;
        this.webHookResponse = webHookResponse;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public final String getQuery() {
        return this.query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getTopicOut() {
        return this.topicOut;
    }

    public void setTopicOut(String topicOut) {
        this.topicOut = topicOut;
    }

    public double getElapsedTime() {
        return this.elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public UUID getChatId() {
        return this.chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public List<MemoryIntent> getIntents() {
        return this.intents;
    }

    public void setIntents(List<MemoryIntent> intents) {
        this.intents = intents;
    }

    public String getHistory() {
        return this.history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public void setAiid(UUID aiid) {
        this.aiid = aiid;
    }

    public void setWebHookResponse(final WebHookResponse webHookResponse) {
        this.webHookResponse = webHookResponse;
    }

    public boolean isResetConversation() {
        return this.resetConversation;
    }

    public void setResetConversation(final boolean resetConversation) {
        this.resetConversation = resetConversation;
    }

    public WebHookResponse getWebhookResponse() {
        return this.webHookResponse;
    }
}
