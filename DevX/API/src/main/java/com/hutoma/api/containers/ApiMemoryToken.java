package com.hutoma.api.containers;

import org.joda.time.DateTime;

/**
 * Created by David MG on 19/08/2016.
 */
public class ApiMemoryToken extends ApiResult {

    private final String variable_name;
    private final String variable_value;
    private final String variable_type;
    private final DateTime last_accessed;
    private final int expires_seconds;
    private final int n_prompts;

    public ApiMemoryToken(String variableName, String variableValue, String variableType, DateTime lastAccessed,
                          int expiresSeconds, int numPrompts) {
        this.variable_name = variableName;
        this.variable_value = variableValue;
        this.variable_type = variableType;
        this.last_accessed = lastAccessed;
        this.expires_seconds = expiresSeconds;
        this.n_prompts = numPrompts;
    }

    public String getVariableName() {

        return this.variable_name;
    }

    public DateTime getLast_accessed() {

        return this.last_accessed;
    }

    public int getExpires_seconds() {
        return this.expires_seconds;
    }

    public String getVariable_name() {

        return this.variable_name;
    }
}
