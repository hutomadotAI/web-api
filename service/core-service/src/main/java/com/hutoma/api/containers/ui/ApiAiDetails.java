package com.hutoma.api.containers.ui;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiSkillSummary;

import java.util.List;

/**
 * AI Details.
 */
public class ApiAiDetails extends ApiResult {
    @SerializedName("training_file")
    private String trainingFile;
    @SerializedName("intents")
    private List<String> intents;
    @SerializedName("skills")
    private List<AiSkillSummary> skills;

    public ApiAiDetails(final String trainingFile, final List<String> intents, final List<AiSkillSummary> skills) {
        this.trainingFile = trainingFile;
        this.intents = intents;
        this.skills = skills;
    }

    public String getTrainingFile() {
        return this.trainingFile;
    }

    public List<String> getIntents() {
        return this.intents;
    }

    public List<AiSkillSummary> getSkills() {
        return this.skills;
    }
}
