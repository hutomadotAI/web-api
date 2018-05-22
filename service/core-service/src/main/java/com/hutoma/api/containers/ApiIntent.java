package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntent extends ApiResult {

    @SerializedName("variables")
    private final List<IntentVariable> variables = new ArrayList<>();
    @SerializedName("intent_name")
    private final String intentName;
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

    public ApiIntent(String intentName, String topicIn, String topicOut) {
        this.intentName = intentName;
        this.topicIn = topicIn;
        this.topicOut = topicOut;
    }

    public String getIntentName() {
        return this.intentName;
    }

    public ApiIntent addUserSays(String says) {
        this.userSays.add(says);
        return this;
    }

    public ApiIntent addVariable(final IntentVariable variable) {
        this.variables.add(variable);
        return this;
    }

    public ApiIntent addResponse(final String response) {
        this.responses.add(response);
        return this;
    }

    public List<IntentVariable> getVariables() {
        return this.variables;
    }

    public List<String> getUserSays() {
        return this.userSays;
    }

    public void setUserSays(final List<String> userSays) {
        this.userSays = userSays;
    }

    public List<String> getResponses() {
        return this.responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public String getTopicIn() {
        return this.topicIn;
    }

    public String getTopicOut() {
        return this.topicOut;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }

    public void setWebHook(final WebHook webHook) {
        this.webHook = webHook;
    }

    public void setLastUpdated(final DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getContextIn() {
        return this.contextIn;
    }

    public Map<String, String> getContextOut() {
        return this.contextOut;
    }

    public void setContextIn(final Map<String, String> context) {
        this.contextIn = context;
    }

    public void setContextOut(final Map<String, String> context) {
        this.contextOut = context;
    }
}
