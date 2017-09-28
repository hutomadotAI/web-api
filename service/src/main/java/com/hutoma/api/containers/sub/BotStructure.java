package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;

import java.util.HashMap;
import java.util.List;

/**
 * Created by bretc on 10/08/2017.
 */
public class BotStructure {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("isPrivate")
    private boolean isPrivate;
    @SerializedName("personality")
    private int personality;
    @SerializedName("confidence")
    private float confidence;
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
    private HashMap<String, ApiEntity> entities;
    @SerializedName("version")
    private int version;

    public BotStructure(String name, String description, List<ApiIntent> intents, String trainingFile,
                        HashMap<String, ApiEntity> entities, int version, boolean isPrivate, int personality,
                        float confidence, int voice, String language, String timezone) {
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

    public HashMap<String, ApiEntity> getEntities() {
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

    public float getConfidence() {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIntents(List<ApiIntent> intents) {
        this.intents = intents;
    }

    public void setTrainingFile(String trainingFile) {
        this.trainingFile = trainingFile;
    }

    public void setEntities(HashMap<String, ApiEntity> entities) {
        this.entities = entities;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setPersonality(int personality) {
        this.personality = personality;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean validVersion() {
        if (this.version == 1) {
            return validV1();
        }

        return false;
    }

    private boolean validV1() {
        if (this.name == null || this.name.isEmpty()
                || this.description == null
                || this.language == null || this.language.isEmpty()
                || this.timezone == null || this.timezone.isEmpty()) {
            return false;
        }
        return true;
    }
}
