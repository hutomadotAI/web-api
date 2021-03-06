package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * The Chat Context holds all the information about th context used within a chat session.
 * Typically it will hold global and local (to intent) variables that then can be read,
 * manipulated, and used for gating the execution of actions.
 */
public class ChatContext {
    @SerializedName("intent_score")
    private double intentScore;

    @SerializedName("variables")
    private Map<String, ChatVariableValue> variables = new HashMap<>();

    /**
     * Gets the intent score
     */
    public double getIntentScore() {
        return this.intentScore;
    }

    /**
     * Sets the intent score
     *
     * @param intentScore the intent score
     */
    public void setIntentScore(final double intentScore) {
        this.intentScore = intentScore;
    }

    /**
     * Sets a value for a variable.
     * Note that setting a variable to a null value will effectively remove it.
     *
     * @param variable     the variable name
     * @param value        the value for the variable
     * @param turnLifetime the number of turns after which this variable will be deleted
     */
    public void setValue(final String variable, final String value, final int turnLifetime) {
        if (value == null) {
            this.variables.remove(variable);
        } else {
            this.variables.put(variable, new ChatVariableValue(value));
            this.variables.get(variable).setLifespanTurns(turnLifetime);
        }
    }

    /**
     * Gets a variable's value.
     *
     * @param variable the variable's name
     * @return the variable's value, or null if there is no variable with that name
     */
    public String getValue(final String variable) {
        if (isSet(variable)) {
            return this.variables.get(variable).getValue();
        }
        return null;
    }

    /**
     * Clears all the variables.
     */
    public void clear() {
        this.intentScore = 0.0;
        this.variables.clear();
    }

    /**
     * Clear specific variable
     */
    public void clearVariable(final String variable) {
        this.variables.remove(variable);
    }

    /**
     * Returns all the variables as a string map.
     * Note that insertion order is not preserved.
     *
     * @return the string map
     */
    public Map<String, String> getVariablesAsStringMap() {
        Map<String, String> ctx = new HashMap<>();
        this.variables.forEach((k, v) -> ctx.put(k, v != null ? v.getValue() : null));
        return ctx;
    }

    /**
     * Checks whether a variable is set (has any value) or not.
     *
     * @param variable the variable's name
     * @return whether it's set or not
     */
    public boolean isSet(final String variable) {
        return variables.containsKey(variable);
    }

    /**
     * Cleans up all the expired variables (number of turns remaining is zero)
     */
    public void cleanupExpiredVariables() {
        this.variables.entrySet().removeIf(x -> x.getValue().lifespanTurns == 0);
    }

    /**
     * Decreases the turn lifetime for all variables (if they have a lifetime)
     */
    public void decrementVariablesTurnLifetime() {
        for (ChatVariableValue value : this.variables.values()) {
            if (value.lifespanTurns > 0) {
                value.lifespanTurns--;
            }
        }
    }

    public ChatVariableValue getVariable(final String variable) {
        return this.variables.get(variable);
    }

    /**
     * Private class for defining a variable value.
     * Currently this just holds string value, but allows future extensibility.
     */
    public static class ChatVariableValue {

        public static final int DEFAULT_LIFESPAN_TURNS = -1;

        @SerializedName("value")
        private String value;
        @SerializedName("lifespan_turns")
        private int lifespanTurns;

        ChatVariableValue(final String value) {
            this.value = value;
            this.lifespanTurns = DEFAULT_LIFESPAN_TURNS;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        void setLifespanTurns(final int lifespanTurns) {
            this.lifespanTurns = lifespanTurns;
        }

        public int getLifespanTurns() {
            return this.lifespanTurns;
        }
    }
}
