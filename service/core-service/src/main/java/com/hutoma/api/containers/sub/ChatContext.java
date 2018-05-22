package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ChatContext {

    @SerializedName("variables")
    private HashMap<String, ChatVariableValue> variables = new HashMap<>();

    public void setValue(final String variable, final String value) {
        if (value == null) {
            variables.remove(variable);
        } else {
            variables.put(variable, new ChatVariableValue(value));
        }
    }

    public String getValue(final String variable) {
        if (isSet(variable)) {
            return this.variables.get(variable).getValue();
        }
        return null;
    }

    public boolean isSet(final String variable) {
        return variables.containsKey(variable);
    }

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
