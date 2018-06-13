package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Conditional intent to execute when another intent is fulfilled
 */
public class IntentOutConditional {

    @SerializedName("conditions")
    private List<IntentVariableCondition> conditions;
    @SerializedName("intent_to_execute")
    private String intentName;

    public IntentOutConditional(final String intentName, final List<IntentVariableCondition> conditions) {
        this.conditions = conditions;
        this.intentName = intentName;
    }

    public List<IntentVariableCondition> getConditions() {
        return this.conditions;
    }

    public String getIntentName() {
        return this.intentName;
    }
}
