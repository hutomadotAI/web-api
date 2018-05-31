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
    @SerializedName("required")
    private final boolean required;
    @SerializedName("n_prompts")
    private final int numberOfPrompts;
    @SerializedName("value")
    private String value;
    @SerializedName("id")
    private int id;
    @SerializedName("persistent")
    private boolean persistent;
    @SerializedName("prompts")
    private List<String> prompts;
    @SerializedName("label")
    private String label;
    @SerializedName("lifetime_turns")
    private int lifetimeTurns;


    public IntentVariable(final String entityName, final UUID devOwner, final boolean required, final int numPrompts,
                          final String value, final boolean persistent, final String label) {
        this.entityName = entityName;
        this.devOwner = devOwner;
        this.required = required;
        this.numberOfPrompts = numPrompts;
        this.value = value;
        this.prompts = new ArrayList<>();
        this.persistent = persistent;
        this.label = label;
    }

    public IntentVariable(final String entityName, final UUID devOwner, final boolean required, final int numPrompts,
                          final String value, final int id, final boolean persistent, final String label) {
        this(entityName, devOwner, required, numPrompts, value, persistent, label);
        this.id = id;
    }

    public IntentVariable addPrompt(final String prompt) {
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

    public void setPrompts(final List<String> prompts) {
        this.prompts = prompts;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    public void setPersistent(final boolean persistent) {
        this.persistent = persistent;
    }

    public int getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public int getLifetimeTurns() {
        return this.lifetimeTurns;
    }

    public void setLifetimeTurns(final int lifetimeTurns) {
        this.lifetimeTurns = lifetimeTurns;
    }
}
