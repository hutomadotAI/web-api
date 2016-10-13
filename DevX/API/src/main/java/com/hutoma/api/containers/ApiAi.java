package com.hutoma.api.containers;

import org.joda.time.DateTime;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAi extends ApiResult {

    private final String aiid;
    private final String client_token;

    private String name;
    private String description;
    private DateTime created_on;
    private boolean is_private;
    private double deep_learning_error;
    private String training_debug_info;
    private String training_status;
    private String ai_status;
    private String ai_training_file;

    public ApiAi(String aiid, String client_token) {
        this.aiid = aiid;
        this.client_token = client_token;
    }

    public ApiAi(String aiid, String client_token, String name, String description, DateTime created_on, boolean is_private,
                 double deep_learning_error, String training_debug_info, String training_status, String ai_status, String ai_training_file) {
        this.aiid = aiid;
        this.client_token = client_token;
        this.name = name;
        this.description = description;
        this.created_on = created_on;
        this.is_private = is_private;
        this.deep_learning_error = deep_learning_error;
        this.training_debug_info = training_debug_info;
        this.training_status = training_status;
        this.ai_status = ai_status;
        this.ai_training_file = ai_training_file;
    }

    public String getAiid() {

        return this.aiid;
    }

    public String getClient_token() {

        return this.client_token;
    }

    public String getAiStatus() {

        return this.ai_status;
    }
}
