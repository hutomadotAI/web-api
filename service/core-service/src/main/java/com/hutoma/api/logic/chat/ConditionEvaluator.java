package com.hutoma.api.logic.chat;

import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.IntentConditionOperator;
import com.hutoma.api.containers.sub.IntentVariableCondition;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConditionEvaluator {

    private List<IntentVariableCondition> conditions;
    private final NumberFormat numberFormat;
    private static final String NUMBER_MATCH_PATTERN = "^[-+]?\\d+(\\.\\d+)?$";

    public ConditionEvaluator() {
        this(new ArrayList<>());
    }

    public ConditionEvaluator(final List<IntentVariableCondition> conditions) {
        this.conditions = conditions;
        this.numberFormat = NumberFormat.getInstance();
    }

    public List<IntentVariableCondition> getConditions() {
        return this.conditions;
    }

    public void setConditions(final List<IntentVariableCondition> conditions) {
        this.conditions = conditions;
    }

    public Results evaluate(final ChatContext chatContext) {

        Results results = new Results();

        for (IntentVariableCondition condition : this.conditions) {

            Result result = new Result(condition);

            // shortcut - if the variable is not present, then bail out as the condition
            // cannot be evaluated
            if (condition.getOperator() == IntentConditionOperator.NOT_SET) {
                if (chatContext.isSet(condition.getVariable())) {
                    result.setResult(ResultType.FAILED);
                }
            } else if (!chatContext.isSet(condition.getVariable())) {
                result.setResult(ResultType.FAILED);
            }

            // Failed already, so shortcut the evaluation and stop evaluating more conditions
            if (result.getResult() == ResultType.FAILED) {
                return results.add(result);
            }

            String variableValue = chatContext.getValue(condition.getVariable());

            switch (condition.getOperator()) {
                case SET:
                    // fallthrough
                case NOT_SET:
                    result.setResult(ResultType.PASSED);
                    break;
                case EQUALS:
                    result.setResult(condition.getValue().equalsIgnoreCase(variableValue)
                            ? ResultType.PASSED : ResultType.FAILED);
                    break;
                case NOT_EQUALS:
                    result.setResult(!condition.getValue().equals(variableValue)
                            ? ResultType.PASSED : ResultType.FAILED);
                    break;
                case GREATER_THAN:
                    // fallthrough
                case GREATER_THAN_OR_EQUALS:
                    // fallthrough
                case SMALLER_THAN_OR_EQUALS:
                    // fallthrough
                case SMALLER_THAN:
                    result.setResult(evaluateBiggerSmaller(condition.getValue(), variableValue, condition.getOperator())
                            ? ResultType.PASSED : ResultType.FAILED);
                    break;
                default:
                    break;
            }
            results.add(result);

            // Stop processing conditions if we've already failed
            if (result.getResult() != ResultType.PASSED) {
                return results;
            }
        }


        return results;
    }

    private boolean evaluateBiggerSmaller(final String conditionValue, final String variableValue,
                                          final IntentConditionOperator operator) {
        if (operator != IntentConditionOperator.GREATER_THAN
                && operator != IntentConditionOperator.SMALLER_THAN
                && operator != IntentConditionOperator.GREATER_THAN_OR_EQUALS
                && operator != IntentConditionOperator.SMALLER_THAN_OR_EQUALS) {
            throw new IllegalArgumentException("operator");
        }
        // First try to convert both the condition and variable values into a numeric
        if (conditionValue.matches(NUMBER_MATCH_PATTERN) && variableValue.matches(NUMBER_MATCH_PATTERN)) {
            try {
                Number conditionValueNumber = this.numberFormat.parse(conditionValue);
                Number variableValueNumber = this.numberFormat.parse(variableValue);
                switch (operator) {
                    case GREATER_THAN:
                        return variableValueNumber.doubleValue() > conditionValueNumber.doubleValue();
                    case SMALLER_THAN:
                        return variableValueNumber.doubleValue() < conditionValueNumber.doubleValue();
                    case GREATER_THAN_OR_EQUALS:
                        return variableValueNumber.doubleValue() >= conditionValueNumber.doubleValue();
                    case SMALLER_THAN_OR_EQUALS:
                        return variableValueNumber.doubleValue() <= conditionValueNumber.doubleValue();
                    default:
                        break;
                }
            } catch (ParseException ex) {
                // Silently fail, it means they are not numbers, so we'll treat them as strings
            }
        }

        // Perform a lexicographic comparison
        int result = conditionValue.compareToIgnoreCase(variableValue);

        switch (operator) {
            case GREATER_THAN:
                return result > 0;
            case SMALLER_THAN:
                return result < 0;
            case GREATER_THAN_OR_EQUALS:
                return result >= 0;
            case SMALLER_THAN_OR_EQUALS:
                return result <= 0;
            default:
                break;
        }

        // Should never get here
        return false;
    }

    public enum ResultType {
        PASSED,
        FAILED,
        NOT_EVALUATED
    }

    public static class Result {
        private IntentVariableCondition condition;
        private ResultType result;

        public Result(final IntentVariableCondition condition) {
            this(condition, ResultType.NOT_EVALUATED);
        }

        public Result(final IntentVariableCondition condition, final ResultType result) {
            this.condition = condition;
            this.result = result;
        }

        public IntentVariableCondition getCondition() {
            return this.condition;
        }

        public ResultType getResult() {
            return this.result;
        }

        public void setResult(final ResultType result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s -> %s",
                    this.condition.getVariable(), this.condition.getOperator().toString(),
                    this.condition.getValue() == null ? "" : condition.getValue(), this.result.toString());
        }
    }

    public static class Results {
        private List<Result> results;

        public Results() {
            this(new ArrayList<>());
        }

        public Results(final List<Result> results) {
            this.results = results;
        }

        public List<Result> getResults() {
            return this.results;
        }

        public Results add(final Result result) {
            this.results.add(result);
            return this;
        }

        public boolean passed() {
            return this.results.stream().allMatch(x -> x.getResult() == ResultType.PASSED);
        }

        public boolean allEvaluated() {
            return this.results.stream().noneMatch(x -> x.getResult() == ResultType.NOT_EVALUATED);
        }

        public boolean failed() {
            return this.results.stream().anyMatch(x -> x.getResult() == ResultType.FAILED
                    || x.getResult() == ResultType.NOT_EVALUATED);
        }

        public Result firstFailed() {
            Optional<Result> opt = this.results.stream().filter(x -> x.getResult() == ResultType.FAILED).findFirst();
            return opt.orElse(null);
        }
    }
}
