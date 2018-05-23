package com.hutoma.api.logic.chat;

import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.IntentConditionOperator;
import com.hutoma.api.containers.sub.IntentVariableCondition;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

public class ConditionEvaluator {

    private final List<IntentVariableCondition> conditions;
    private final NumberFormat numberFormat;
    private static final String NUMBER_MATCH_PATTERN = "^[-+]?\\d+(\\.\\d+)?$";

    public ConditionEvaluator(final List<IntentVariableCondition> conditions) {
        this.conditions = conditions;
        this.numberFormat = NumberFormat.getInstance();
    }

    public List<IntentVariableCondition> getConditions() {
        return this.conditions;
    }

    public boolean evaluate(final ChatContext chatContext) {

        for (IntentVariableCondition condition : this.conditions) {

            // shortcut - if the variable is not present, then bail out as the condition
            // cannot be evaluated
            if (condition.getOperator() == IntentConditionOperator.NOT_SET) {
                if (chatContext.isSet(condition.getVariable())) {
                    return false;
                }
            } else if (!chatContext.isSet(condition.getVariable())) {
                return false;
            }

            String variableValue = chatContext.getValue(condition.getVariable());

            switch (condition.getOperator()) {
                case SET:
                    // Only here for completeness, since this has been already evaluated before
                case NOT_SET:
                    // Only here for completeness, since this has been already evaluated before
                    break;
                case EQUALS:
                    return condition.getValue().equals(variableValue);
                case NOT_EQUALS:
                    return !condition.getValue().equals(variableValue);
                case BIGGER_THAN:
                    // fallthrough
                case SMALLER_THAN:
                    return evaluateBiggerSmaller(condition.getValue(), variableValue, condition.getOperator());
                default:
                    break;
            }
        }


        return true;
    }

    private boolean evaluateBiggerSmaller(final String conditionValue, final String variableValue,
                                          final IntentConditionOperator operator) {
        if (operator != IntentConditionOperator.BIGGER_THAN && operator != IntentConditionOperator.SMALLER_THAN) {
            throw new IllegalArgumentException("operator");
        }
        // First try to convert both the condition and variable values into a numeric
        if (conditionValue.matches(NUMBER_MATCH_PATTERN) && variableValue.matches(NUMBER_MATCH_PATTERN)) {
            try {
                Number conditionValueNumber = this.numberFormat.parse(conditionValue);
                Number variableValueNumber = this.numberFormat.parse(variableValue);
                switch (operator) {
                    case BIGGER_THAN:
                        return variableValueNumber.doubleValue() > conditionValueNumber.doubleValue();
                    case SMALLER_THAN:
                        return variableValueNumber.doubleValue() < conditionValueNumber.doubleValue();
                    default:
                        break;
                }
            } catch (ParseException ex) {
                // Silently fail, it means they are not numbers, so we'll treat them as strings
            }
        }

        // Perform a lexicographic comparison
        int result = conditionValue.compareTo(variableValue);
        if (result == 0) {
            // if they're equal they can't be bigger or smaller
            return false;
        }
        switch (operator) {
            case BIGGER_THAN:
                return result > 0;
            case SMALLER_THAN:
                return result < 0;
            default:
                break;
        }

        // Should never get here
        return false;
    }
}
