package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

import java.util.Locale;
import java.util.TimeZone;

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
    private TrainingStatus ai_status;
    private String ai_training_file;
    private int personality; // aka. Learn from users
    private double confidence; // aka Create new answers
    private int voice;
    private Locale language;
    private TimeZone timezone;

    public ApiAi(String aiid, String clientToken) {
        this.aiid = aiid;
        this.client_token = clientToken;
    }

    public ApiAi(String aiid, String clientToken, String name, String description, DateTime createdOn,
                 boolean isPrivate, double deepLearningError, String trainingDebugInfo, String trainingStatus,
                 TrainingStatus aiStatus, String aiTrainingFile, int personality, double confidence,
                 int voice, Locale language, TimeZone timezone) {
        this.aiid = aiid;
        this.client_token = clientToken;
        this.name = name;
        this.description = description;
        this.created_on = createdOn;
        this.is_private = isPrivate;
        this.deep_learning_error = deepLearningError;
        this.training_debug_info = trainingDebugInfo;
        this.training_status = trainingStatus;
        this.ai_status = aiStatus;
        this.ai_training_file = aiTrainingFile;
        this.personality = personality;
        this.confidence = confidence;
        this.voice = voice;
        this.language = language;
        this.timezone = timezone;
    }

    public String getAiid() {

        return this.aiid;
    }

    public String getClient_token() {

        return this.client_token;
    }

    public TrainingStatus getAiStatus() {

        return this.ai_status;
    }
}
