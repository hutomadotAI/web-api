package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.IntentVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntent extends ApiResult {

    private final List<IntentVariable> variables = new ArrayList<>();
    private final List<String> responses = new ArrayList<>();
    private final String intent_name;
    private final String topic_in;
    private final String topic_out;
    private List<String> user_says = new ArrayList<>();

    public ApiIntent(String intent_name, String topic_in, String topic_out) {
        this.intent_name = intent_name;
        this.topic_in = topic_in;
        this.topic_out = topic_out;
    }

    public String getIntentName() {
        return this.intent_name;
    }

    public ApiIntent addUserSays(String says) {
        this.user_says.add(says);
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
        return this.user_says;
    }

    public void setUserSays(final List<String> userSays) {
        this.user_says = userSays;
    }
}
