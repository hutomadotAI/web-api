package com.hutoma.api.containers.sub;

import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 16/08/2016.
 */
public class ChatResult {

    private String topic_in = "";
    private double score;
    private String query = "";
    private String answer = "";
    private String history = "";
    private double elapsed_time;
    private String action;
    private String context;
    private String topic_out = "";
    private UUID chatId;
    private List<MemoryIntent> intents;
    private transient boolean resetConversation;

    public ChatResult() {
    }

    /***
     * Constructor that takes a ChatResult and copies only the fields
     * that we want to pass to the API caller
     * @param source
     */
    public ChatResult(final ChatResult source) {
        this.score = source.score;
        this.answer = source.answer;
        this.elapsed_time = source.elapsed_time;
        this.topic_out = source.topic_out;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setQuery(String query) {
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

    public String getTopic_out() {
        return this.topic_out;
    }

    public void setTopic_out(String topicOut) {
        this.topic_out = topicOut;
    }

    public double getElapsedTime() {
        return this.elapsed_time;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsed_time = elapsedTime;
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

    public boolean isResetConversation() {
        return this.resetConversation;
    }

    public void setResetConversation(final boolean resetConversation) {
        this.resetConversation = resetConversation;
    }
}
