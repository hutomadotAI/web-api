package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bretc on 10/08/2017.
 */
public class BotStructure {

    private static final String LOGFROM = "BotStructure";

    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("isPrivate")
    private boolean isPrivate;
    @SerializedName("personality")
    private int personality;
    @SerializedName("confidence")
    private double confidence;
    @SerializedName("voice")
    private int voice;
    @SerializedName("language")
    private String language;
    @SerializedName("timezone")
    private String timezone;
    @SerializedName("intents")
    private List<ApiIntent> intents;
    @SerializedName("trainingFile")
    private String trainingFile;
    @SerializedName("entities")
    private Map<String, ApiEntity> entities;
    @SerializedName("version")
    private int version;
    @SerializedName("default_responses")
    private List<String> defaultResponses;
    @SerializedName("passthrough_url")
    private String passthroughUrl;

    public BotStructure(final String name, final String description, final List<ApiIntent> intents,
                        final String trainingFile, final Map<String, ApiEntity> entities, final int version,
                        final boolean isPrivate, final int personality, final double confidence, final int voice,
                        final String language, final String timezone, final List<String> defaultResponses,
                        final String passthroughUrl) {
        this.name = name;
        this.description = description;
        this.intents = intents;
        this.trainingFile = trainingFile;
        this.entities = entities;
        this.version = version;
        this.isPrivate = isPrivate;
        this.personality = personality;
        this.confidence = confidence;
        this.voice = voice;
        this.language = language;
        this.timezone = timezone;
        this.defaultResponses = defaultResponses;
        this.passthroughUrl = passthroughUrl;
    }

    /**
     * Copy ctor.
     * @param other the structure to copy from
     */
    public BotStructure(final BotStructure other) {
        this.name = other.name;
        this.description = other.description;
        this.intents = other.intents;
        this.trainingFile = other.trainingFile;
        this.entities = other.entities;
        this.version = other.version;
        this.isPrivate = other.isPrivate;
        this.personality = other.personality;
        this.confidence = other.confidence;
        this.voice = other.voice;
        this.language = other.language;
        this.timezone = other.timezone;
        this.defaultResponses = new ArrayList<>(other.defaultResponses);
        this.passthroughUrl = other.passthroughUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<ApiIntent> getIntents() {
        return this.intents;
    }

    public String getTrainingFile() {
        return this.trainingFile;
    }

    public Map<String, ApiEntity> getEntities() {
        return this.entities;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public int getPersonality() {
        return this.personality;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public int getVoice() {
        return this.voice;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public List<String> getDefaultResponses() {
        return this.defaultResponses;
    }

    public String getPassthroughUrl() {
        return this.passthroughUrl;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setIntents(final List<ApiIntent> intents) {
        this.intents = intents;
    }

    public void setTrainingFile(final String trainingFile) {
        this.trainingFile = trainingFile;
    }

    public void setEntities(final Map<String, ApiEntity> entities) {
        this.entities = entities;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public void setPrivate(final boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setPersonality(final int personality) {
        this.personality = personality;
    }

    public void setConfidence(final double confidence) {
        this.confidence = confidence;
    }

    public void setVoice(final int voice) {
        this.voice = voice;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public void setDefaultResponses(final List<String> defaultResponses) {
        this.defaultResponses = defaultResponses;
    }

    public void setPassthroughUrl(final String passthroughUrl) {
        this.passthroughUrl = passthroughUrl;
    }
}
