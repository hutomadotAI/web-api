package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class to hold the result of a chat iteration.
 */
public class ChatResult {

    /**
     * Section for publicly exposed JSON fields
     */
    @SerializedName("topicIn")
    private String topicIn = "";
    @SerializedName("score")
    private double score;
    @SerializedName("query")
    private String query;
    @SerializedName("answer")
    private String answer = "";
    @SerializedName("history")
    private String history = "";
    @SerializedName("elapsedTime")
    private double elapsedTime;
    @SerializedName("action")
    private String action;
    @SerializedName("context")
    private Map<String, String> context;
    @SerializedName("topic_out")
    private String topicOut = "";
    @SerializedName("chatId")
    private UUID chatId;
    @SerializedName("intents")
    private List<MemoryIntent> intents;
    @SerializedName("chatTarget")
    private String chatTarget;
    @SerializedName("timestamp")
    private long timestamp;

    /**
     * Section for transient fields (non-serializable to JSON)
     */
    // result of a webhook call if one was made
    private transient WebHookResponse webHookResponse;
    // the label of the intent variable that we are prompting (or null if we are not)
    private transient String promptForIntentVariable;
    // the actual bot ID that provided the result
    private transient UUID aiid;
    // whether to reset the conversation or not
    private transient boolean resetConversation;
    // holds the chat state
    private transient ChatState chatState;

    public ChatResult(final String query) {
        this.query = query;
    }

    /***
     * Constructor that takes a ChatResult and copies only the fields
     * that we want to pass to the API caller.
     * @param source the source object to copy from
     */
    private ChatResult(final ChatResult source) {
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
        this.chatTarget = source.chatTarget;
        this.webHookResponse = source.webHookResponse;
        this.chatState = source.chatState;
        this.timestamp = source.timestamp;
        this.context = source.context;
    }

    /**
     * Ctor.
     * @param chatId          the chat id
     * @param score           the response score
     * @param query           the initial query
     * @param answer          the response to send to the user
     * @param elapsedTime     elapsed time for the response generation
     * @param webHookResponse response from the webhook
     */
    public ChatResult(final UUID chatId, final double score, final String query, final String answer,
                      final double elapsedTime, final WebHookResponse webHookResponse) {
        this.score = score;
        this.query = query;
        this.answer = answer;
        this.elapsedTime = elapsedTime;
        this.chatId = chatId;
        this.webHookResponse = webHookResponse;
        this.chatState = null;
    }

    /**
     * Gets the answer.
     * @return the answer
     */
    public String getAnswer() {
        return this.answer;
    }

    /**
     * Sets the answer.
     * @param answer the answer
     */
    public void setAnswer(final String answer) {
        this.answer = answer;
    }

    /**
     * Gets the initial query.
     * @return the query
     */
    public final String getQuery() {
        return this.query;
    }

    /**
     * Sets the initial query.
     * @param query the query
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * Sets the chat context.
     * @param context the new context
     */
    public void setContext(final Map<String, String> context) {
        this.context = context;
    }

    /**
     * Gets the response score.
     * @return the score
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Sets the response score.
     * @param score the score
     */
    public void setScore(final double score) {
        this.score = score;
    }

    /**
     * Sets the input topic.
     * @param topicIn the input topic
     */
    public void setTopicIn(final String topicIn) {
        this.topicIn = topicIn;
    }

    /**
     * Gets the output topic.
     * @return the output topic
     */
    public String getTopicOut() {
        return this.topicOut;
    }

    /**
     * Sets the output topic.
     * @param topicOut the output topic
     */
    public void setTopicOut(final String topicOut) {
        this.topicOut = topicOut;
    }

    /**
     * Gets the elapse time.
     * @return the elapsed time
     */
    public double getElapsedTime() {
        return this.elapsedTime;
    }

    /**
     * Sets the elapsed time
     * @param elapsedTime the elapsed time
     */
    public void setElapsedTime(final double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Gets the chat session id.
     * @return the chat session id
     */
    public UUID getChatId() {
        return this.chatId;
    }

    /**
     * Sets the chat session id.
     * @param chatId the chat session id
     */
    public void setChatId(final UUID chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the list of intents in flight.
     * @return the list of intents in flight
     */
    public List<MemoryIntent> getIntents() {
        return this.intents;
    }

    /**
     * Sets the list of intents in flight.
     * @param intents the list of intents in flight
     */
    public void setIntents(final List<MemoryIntent> intents) {
        this.intents = intents;
    }

    /**
     * Gets the history.
     * @return the history
     */
    public String getHistory() {
        return this.history;
    }

    /**
     * Sets the history.
     * @param history the history
     */
    public void setHistory(final String history) {
        this.history = history;
    }

    /**
     * Gets the AI id.
     * @return the AI id
     */
    public UUID getAiid() {
        return this.aiid;
    }

    /**
     * Sets the AI id.
     * @param aiid the AI id
     */
    public void setAiid(final UUID aiid) {
        this.aiid = aiid;
    }

    /**
     * Sets the webhook response.
     * @param webHookResponse the webhook response
     */
    public void setWebHookResponse(final WebHookResponse webHookResponse) {
        this.webHookResponse = webHookResponse;
    }

    /**
     * Sets the reset conversation flag.
     * @param resetConversation whether to reset the conversation or not
     */
    public void setResetConversation(final boolean resetConversation) {
        this.resetConversation = resetConversation;
    }

    /**
     * Gets the webhook response.
     * @return the webhook response
     */
    public WebHookResponse getWebhookResponse() {
        return this.webHookResponse;
    }

    /**
     * Gets the prompt for requesting a variable from an intent in flight.
     * @return the prompt
     */
    public String getPromptForIntentVariable() {
        return this.promptForIntentVariable;
    }

    /**
     * Sets the prompt for requesting a variable from an intent in flight.
     * @param promptForIntentVariable the prompt
     */
    public void setPromptForIntentVariable(final String promptForIntentVariable) {
        this.promptForIntentVariable = promptForIntentVariable;
    }

    /**
     * Sets the timestamp of the chat call.
     * @param timestamp the timestamp
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the timestamp of the chat call.
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the current chat target @see com.hutoma.api.containers.sub.ChatHandoverTarget
     * @param chatTarget the new chat target
     */
    public void setChatTarget(final String chatTarget) {
        this.chatTarget = chatTarget;
    }

    /**
     * Gets the current chat target.
     * @return the current chat target
     */
    public String getChatTarget() {
        return this.chatTarget;
    }

    /***
     * Clone a version of the chat result that we can send back to the user.
     * @param source the source chat result
     * @return a new chat result with only the user-viewable fields
     */
    public static ChatResult getUserViewable(final ChatResult source) {
        ChatResult chatResult = new ChatResult(source);
        if (source.getIntents() != null) {
            chatResult.intents = source.getIntents().stream()
                    .map(MemoryIntent::getUserViewable)
                    .collect(Collectors.toList());
        }
        // Copy the context across - note the check of source.getChatState() is mostly for UTs as it shouldn't
        // occur in the normal chat flow
        if (source.getChatState() != null && source.getChatState().getChatContext() != null) {
            chatResult.setContext(source.getChatState().getChatContext().getVariablesAsStringMap());
        }
        return chatResult;
    }

    /**
     * Gets the current chat state.
     * @return the chat state
     */
    public ChatState getChatState() {
        return this.chatState;
    }

    /**
     * Sets the chat state.
     * @param chatState the new chat state
     */
    public void setChatState(final ChatState chatState) {
        this.chatState = chatState;
    }

    public Map<String, String> getContext() {
        return this.context;
    }
}