package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

import java.util.Locale;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAi extends ApiResult {

    private final String aiid;
    private final String client_token;

    private String name;
    private String description;

    @SerializedName("created_on")
    private DateTime createdOn;

    @SerializedName("is_private")
    private boolean isPrivate;

    private int personality; // aka. Learn from users
    private double confidence; // aka Create new answers
    private int voice;
    private Locale language;
    private String timezone;

    // transient because this should never be serialized along with the ApiAi object
    private transient BackendStatus backendStatus;

    // this is a single status that represents the ai state on multiple backend servers
    @SerializedName("ai_status")
    private TrainingStatus summaryStatus;

    public ApiAi(String aiid, String clientToken) {
        this.aiid = aiid;
        this.client_token = clientToken;
    }

    public ApiAi(String aiid, String clientToken, String name, String description, DateTime createdOn,
                 boolean isPrivate, BackendStatus backendStatus, TrainingStatus summaryStatus,
                 int personality, double confidence, int voice, Locale language, String timezone) {
        this.aiid = aiid;
        this.client_token = clientToken;
        this.name = name;
        this.description = description;
        this.createdOn = createdOn;
        this.isPrivate = isPrivate;
        this.backendStatus = backendStatus;
        this.personality = personality;
        this.confidence = confidence;
        this.voice = voice;
        this.language = language;
        this.timezone = timezone;
        this.summaryStatus = summaryStatus;
    }

    public String getAiid() {
        return this.aiid;
    }

    public String getClient_token() {
        return this.client_token;
    }

    public TrainingStatus getSummaryAiStatus() {
        return this.summaryStatus;
    }

}
