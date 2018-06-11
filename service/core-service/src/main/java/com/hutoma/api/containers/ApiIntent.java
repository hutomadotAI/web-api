package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.IntentVariableCondition;
import com.hutoma.api.containers.sub.WebHook;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Response for an Intent.
 */
public class ApiIntent extends ApiResult {

    @SerializedName("variables")
    private final List<IntentVariable> variables = new ArrayList<>();
    @SerializedName("intent_name")
    private String intentName;
    @SerializedName("topic_in")
    private final String topicIn;
    @SerializedName("topic_out")
    private final String topicOut;
    @SerializedName("responses")
    private List<String> responses = new ArrayList<>();
    @SerializedName("user_says")
    private List<String> userSays = new ArrayList<>();
    @SerializedName("webhook")
    private WebHook webHook;
    @SerializedName("last_updated")
    private DateTime lastUpdated;
    @SerializedName("context_in")
    private Map<String, String> contextIn = new HashMap<>();
    @SerializedName("context_out")
    private Map<String, String> contextOut = new HashMap<>();
    @SerializedName("conditions_in")
    private List<IntentVariableCondition> conditionsIn = new ArrayList<>();
    @SerializedName("conditions_default_response")
    private String conditionsFallthroughMessage;
    @SerializedName("reset_context_on_exit")
    private boolean resetContextOnExit;

    /**
     * Ctor.
     * @param intentName the intent name
     * @param topicIn    the input topic
     * @param topicOut   the output topic
     */
    public ApiIntent(String intentName, String topicIn, String topicOut) {
        this.intentName = intentName;
        this.topicIn = topicIn;
        this.topicOut = topicOut;
    }

    /**
     * Gets the intent name.
     * @return the intent name
     */
    public String getIntentName() {
        return this.intentName;
    }

    /**
     * Sets the intent name
     * @param name the new name
     */
    public void setIntentName(final String name) {
        this.intentName = name;
    }

    /**
     * Adds a "user says" expression
     * @param says the expression
     * @return the ApiIntent for chaining
     */
    public ApiIntent addUserSays(String says) {
        this.userSays.add(says);
        return this;
    }

    /**
     * Adds a variable.
     * @param variable the variable
     * @return the ApiIntent for chaining
     */
    public ApiIntent addVariable(final IntentVariable variable) {
        this.variables.add(variable);
        return this;
    }

    /**
     * Adds a response.
     * @param response the response
     * @return the ApiIntent for chaining
     */
    public ApiIntent addResponse(final String response) {
        this.responses.add(response);
        return this;
    }

    /**
     * Gets the list of variables.
     * @return the list of variables
     */
    public List<IntentVariable> getVariables() {
        return this.variables;
    }

    /**
     * Gets the "user says" expressions.
     * @return the expressions
     */
    public List<String> getUserSays() {
        return this.userSays;
    }

    /**
     * Sets the "user says" expressions
     * @param userSays the expressions
     */
    public void setUserSays(final List<String> userSays) {
        this.userSays = userSays;
    }

    /**
     * Gets the responses.
     * @return the responses
     */
    public List<String> getResponses() {
        return this.responses;
    }

    /**
     * Sets the responses.
     * @param responses the responses
     */
    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    /**
     * Gets the input topic.
     * @return the input topic
     */
    public String getTopicIn() {
        return this.topicIn;
    }

    /**
     * Sets the output topic.
     * @return the output topic
     */
    public String getTopicOut() {
        return this.topicOut;
    }

    /**
     * Gets the webhook.
     * @return the webhook
     */
    public WebHook getWebHook() {
        return this.webHook;
    }

    /**
     * Sets the webhook.
     * @param webHook the webhook
     */
    public void setWebHook(final WebHook webHook) {
        this.webHook = webHook;
    }

    /**
     * Sets the time the intent was last updated.
     * @param lastUpdated the time of last update
     */
    public void setLastUpdated(final DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Gets the input context.
     * @return the input context
     */
    public Map<String, String> getContextIn() {
        return this.contextIn;
    }

    /**
     * Sets the output context.
     * @return the output context
     */
    public Map<String, String> getContextOut() {
        return this.contextOut;
    }

    /**
     * Sets the input context.
     * @param context the input context
     */
    public void setContextIn(final Map<String, String> context) {
        this.contextIn = context;
    }

    /**
     * Sets the output context.
     * @param context the output context
     */
    public void setContextOut(final Map<String, String> context) {
        this.contextOut = context;
    }

    /**
     * Sets the input conditions.
     * @param conditionsIn the input conditions
     */
    public void setConditionsIn(final List<IntentVariableCondition> conditionsIn) {
        this.conditionsIn = conditionsIn;
    }

    /**
     * Gets the input conditions.
     * @return the input conditions
     */
    public List<IntentVariableCondition> getConditionsIn() {
        return this.conditionsIn;
    }

    /**
     * Gets the message to use as a fallthrough if conditions are not met.
     * @return the fallthrough message
     */
    public String getConditionsFallthroughMessage() {
        return this.conditionsFallthroughMessage;
    }

    /**
     * Sets the message to use as a fallthrough if conditions are not met.
     * @param conditionsFallthroughMessage the fallthrough message
     */
    public void setConditionsFallthroughMessage(final String conditionsFallthroughMessage) {
        this.conditionsFallthroughMessage = conditionsFallthroughMessage;
    }

    /**
     * Sets whether the intent should reset the context upon successful fulfillment
     * @param resetContextOnExit whether should reset the context or not
     */
    public void setResetContextOnExit(final boolean resetContextOnExit) {
        this.resetContextOnExit = resetContextOnExit;
    }

    /**
     * Gets whether the intent should reset the context upon successful fulfillment
     * @return whether should reset the context or not
     */
    public boolean getResetContextOnExit() {
        return this.resetContextOnExit;
    }
}
