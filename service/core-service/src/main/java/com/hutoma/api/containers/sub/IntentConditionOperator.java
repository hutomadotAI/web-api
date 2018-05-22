package com.hutoma.api.containers.sub;

public enum IntentConditionOperator {
    ISSET("set"),
    NOT_SET("!set"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    BIGGER_THAN(">"),
    SMALLER_THAN("<");

    private String opString;

    IntentConditionOperator(final String opString) {
        this.opString = opString;
    }

    public String getOpString() {
        return this.opString;
    }
}