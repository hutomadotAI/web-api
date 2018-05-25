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

    @SerializedName("variables")
    private Map<String, ChatVariableValue> variables = new HashMap<>();

    /**
     * Sets a value for a variable.
     * Note that setting a variable to a null value will effectively remove it.
     * @param variable the variable name
     * @param value    the value for the variable
     */
    public void setValue(final String variable, final String value) {
        if (value == null) {
            variables.remove(variable);
        } else {
            variables.put(variable, new ChatVariableValue(value));
        }
    }

    /**
     * Gets a variable's value.
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
        this.variables.clear();
    }

    /**
     * Returns all the variables as a string map.
     * Note that insertion order is not preserved.
     * @return the string map
     */
    public Map<String, String> getVariablesAsStringMap() {
        Map<String, String> ctx = new HashMap<>();
        this.variables.forEach((k, v) -> ctx.put(k, v.getValue()));
        return ctx;
    }

    /**
     * Checks whether a variable is set (has any value) or not.
     * @param variable the variable's name
     * @return whether it's set or not
     */
    public boolean isSet(final String variable) {
        return variables.containsKey(variable);
    }

    /**
     * Private class for defining a variable value.
     * Currently this just holds string value, but allows future extensibility.
     */
    private static class ChatVariableValue {
        @SerializedName("value")
        private String value;

        ChatVariableValue(final String value) {
            this.value = value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
