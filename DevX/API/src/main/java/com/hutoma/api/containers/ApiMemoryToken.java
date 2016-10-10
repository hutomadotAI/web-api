package com.hutoma.api.containers;

import org.joda.time.DateTime;

/**
 * Created by David MG on 19/08/2016.
 */
public class ApiMemoryToken extends ApiResult {

    String variable_name;
    String variable_value;
    String variable_type;
    DateTime last_accessed;
    int expires_seconds;
    int n_prompts;

    public ApiMemoryToken(String variable_name, String variable_value, String variable_type, DateTime last_accessed, int expires_seconds, int n_prompts) {
        this.variable_name = variable_name;
        this.variable_value = variable_value;
        this.variable_type = variable_type;
        this.last_accessed = last_accessed;
        this.expires_seconds = expires_seconds;
        this.n_prompts = n_prompts;
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
