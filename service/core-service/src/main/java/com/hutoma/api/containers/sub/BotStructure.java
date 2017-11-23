package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    private HashMap<String, ApiEntity> entities;
    @SerializedName("version")
    private int version;
    @SerializedName("linkedBots")
    private List<UUID> linkedBots;

    public BotStructure(final String name, final String description, final List<ApiIntent> intents,
                        final String trainingFile, final HashMap<String, ApiEntity> entities, final int version,
                        final boolean isPrivate, final int personality, final double confidence, final int voice,
                        final String language, final String timezone, final List<UUID> linkedBots) {
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
        this.linkedBots = linkedBots;
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
        this.linkedBots = other.linkedBots;
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

    public List<UUID> getLinkedBots() { return this.linkedBots; }

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

    public void setEntities(final HashMap<String, ApiEntity> entities) {
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

    public void setLinkedBots(final List<UUID> linkedBots) { this.linkedBots = linkedBots; }
}
