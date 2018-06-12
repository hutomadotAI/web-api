package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiIntent;

import java.util.List;

/**
 * Conditional intent to execute when another intent is fulfilled
 */
public class IntentOutConditional {

    @SerializedName("conditions")
    private List<IntentVariableCondition> conditions;
    @SerializedName("intent_to_execute")
    private String intentToExecute;

    private transient ApiIntent intent;

    public IntentOutConditional(final ApiIntent intent, final List<IntentVariableCondition> conditions) {
        this.intent = intent;
        this.conditions = conditions;
        this.intentToExecute = intent.getIntentName();
    }

    public ApiIntent getIntent() {
        return this.intent;
    }

    public List<IntentVariableCondition> getConditions() {
        return this.conditions;
    }
}
