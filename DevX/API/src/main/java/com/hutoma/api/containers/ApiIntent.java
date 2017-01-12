package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.IntentVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntent extends ApiResult {

    private final List<IntentVariable> variables = new ArrayList<>();
    @SerializedName("intentName")
    private final String intentName;
    @SerializedName("topicIn")
    private final String topicIn;
    @SerializedName("topicOut")
    private final String topicOut;
    private List<String> responses = new ArrayList<>();
    @SerializedName("userSays")
    private List<String> userSays = new ArrayList<>();

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

    public ApiIntent addVariable(IntentVariable variable) {
        this.variables.add(variable);
        return this;
    }

    public ApiIntent addResponse(String response) {
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
}
