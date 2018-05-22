package com.hutoma.api.containers.sub;

public class IntentVariableCondition {
    private String variable;
    private String value;
    private IntentConditionOperator operator;

    public IntentVariableCondition(final String variable, final IntentConditionOperator operator, final String value) {
        this.variable = variable;
        this.operator = operator;
        this.value = value;
    }

    public String getVariable() {
        return this.variable;
    }

    public String getValue() {
        return this.value;
    }

    public IntentConditionOperator getOperator() {
        return this.operator;
    }
}