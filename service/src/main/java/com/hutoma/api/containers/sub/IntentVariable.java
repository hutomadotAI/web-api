package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 07/10/2016.
 */
public class IntentVariable {

    @SerializedName("entity_name")
    private final String entityName;
    @SerializedName("dev_owner")
    private final UUID devOwner;

    private final boolean required;
    @SerializedName("n_prompts")
    private final int numberOfPrompts;
    @SerializedName("value")
    private String value;
    private int id;
    private boolean persistent;

    private List<String> prompts;

    public IntentVariable(String entityName, UUID devOwner, boolean required, int numPrompts, String value, boolean persistent) {
        this.entityName = entityName;
        this.devOwner = devOwner;
        this.required = required;
        this.numberOfPrompts = numPrompts;
        this.value = value;
        this.prompts = new ArrayList<>();
        this.persistent = persistent;
    }

    public IntentVariable(String entityName, UUID devOwner, boolean required, int numPrompts, String value, int id, boolean persistent) {
        this(entityName, devOwner, required, numPrompts, value, persistent);
        this.id = id;
    }

    public IntentVariable addPrompt(String prompt) {
        this.prompts.add(prompt);
        return this;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public UUID getDevOwner() {
        return this.devOwner;
    }

    public boolean isRequired() {
        return this.required;
    }

    public int getNumPrompts() {
        return this.numberOfPrompts;
    }

    public List<String> getPrompts() {
        return this.prompts;
    }

    public void setPrompts(List<String> prompts) {
        this.prompts = prompts;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPersistent() { return this.persistent; }

    public void setPersistent(boolean persistent) { this.persistent = persistent; }

    public int getId() {
        return this.id;
    }
}
