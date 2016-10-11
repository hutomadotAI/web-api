package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.IntentVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 05/10/2016.
 */
public class ApiIntent extends ApiResult {

    final private ArrayList<IntentVariable> variables = new ArrayList<>();
    final private ArrayList<String> user_says = new ArrayList<>();
    final private ArrayList<String> responses = new ArrayList<>();
    private String intent_name;
    private String topic_in;
    private String topic_out;

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
}
