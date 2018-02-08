package com.hutoma.api.containers.ui;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiSkillSummary;

/**
 * AI Details.
 */
public class ApiAiDetails extends ApiResult {
    @SerializedName("training_file")
    private String trainingFile;
    @SerializedName("intents")
    private List<ApiIntent> intents;
    @SerializedName("skills")
    private List<AiSkillSummary> skills;

    public ApiAiDetails(final String trainingFile, final List<ApiIntent> intents, final List<AiSkillSummary> skills) {
        this.trainingFile = trainingFile;
        this.intents = intents;
        this.skills = skills;
    }

    public String getTrainingFile() {
        return this.trainingFile;
    }

    public List<ApiIntent> getIntents() {
        return this.intents;
    }

    public List<AiSkillSummary> getSkills() {
        return this.skills;
    }
}
