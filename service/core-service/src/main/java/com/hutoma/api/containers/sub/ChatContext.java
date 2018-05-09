package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import org.elasticsearch.common.Strings;

import java.util.HashMap;

public class ChatContext {

    @SerializedName("variables")
    private HashMap<String, String> variables = new HashMap<>();

    public void setValue(final String variable, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            variables.remove(variable);
        } else {
            variables.put(variable, value);
        }
    }

    public String getValue(final String variable) {
        if (isSet(variable)) {
            return this.variables.get(variable);
        }
        return null;
    }

    public boolean isSet(final String variable) {
        return variables.containsKey(variable);
    }
}
