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
    @SerializedName("intents")
    private List<ApiIntent> intents;
    @SerializedName("trainingFile")
    private String trainingFile;
    @SerializedName("entities")
    private HashMap<String, ApiEntity> entities;

    public BotStructure(String name, String description, List<ApiIntent> intents, String trainingFile,
                        HashMap<String, ApiEntity> entities) {
        this.name = name;
        this.description = description;
        this.intents = intents;
        this.trainingFile = trainingFile;
        this.entities = entities;
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

    public HashMap<String, ApiEntity> getEntities() { return this.entities; }

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

    public void setEntities(HashMap<String, ApiEntity> entities) { this.entities = entities; }
}
