package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedrotei on 06/10/16.
 */
public class MemoryVariable {
    @SerializedName("entity")
    private String name;
    @SerializedName("value")
    private String currentValue;
    @SerializedName("mandatory")
    private boolean isMandatory;
    @SerializedName("times_prompted")
    private int timesPrompted;
    @SerializedName("max_prompts")
    private int timesToPrompt;
    private boolean persistent;
    @SerializedName("system_entity")
    private boolean isSystem;
    @SerializedName("label")
    private String label;
    @SerializedName("requested")
    private boolean requested;
    @SerializedName("reset_on_entry")
    private boolean resetOnEntry;

    // do not send these back to the user
    @SerializedName("entity_keys")
    private List<String> entityKeys;
    // do not send these back to the user
    @SerializedName("prompts")
    private List<String> prompts;

    /**
     * Ctor.
     * @param name       the name
     * @param entityKeys the entity keys
     */
    public MemoryVariable(String name, List<String> entityKeys) {
        this.name = name;
        this.entityKeys = entityKeys;
    }

    public MemoryVariable(final String name, final List<String> entityKeys,
                          final boolean isSystem, final String label) {
        this.name = name;
        this.entityKeys = entityKeys;
        this.isSystem = isSystem;
        this.label = label;
    }

    public MemoryVariable(final String name, final String currentValue, final boolean isMandatory,
                          final List<String> entityKeys, final List<String> prompts, final int timesToPrompt,
                          final int timesPrompted, final boolean isSystem, final boolean persistent,
                          final String label, final boolean resetOnEntry) {
        this(name, entityKeys);
        this.currentValue = currentValue;
        this.isMandatory = isMandatory;
        this.prompts = prompts;
        this.timesPrompted = timesPrompted;
        this.timesToPrompt = timesToPrompt;
        this.persistent = persistent;
        this.isSystem = isSystem;
        this.label = label;
        this.resetOnEntry = resetOnEntry;
    }

    public MemoryVariable(final IntentVariable intentVariable) {
        this.name = intentVariable.getEntityName();
        this.entityKeys = new ArrayList<>();
        this.label = intentVariable.getLabel();
        this.isMandatory = intentVariable.isRequired();
        this.prompts = intentVariable.getPrompts();
        this.resetOnEntry = intentVariable.getClearOnEntry();
    }

    private MemoryVariable(final MemoryVariable source) {
        this.name = source.name;
        this.currentValue = source.currentValue;
        this.isMandatory = source.isMandatory;
        this.timesPrompted = source.timesPrompted;
        this.timesToPrompt = source.timesToPrompt;
        this.persistent = source.persistent;
        this.isSystem = source.isSystem;
        this.label = source.label;
        this.requested = source.requested;
        this.resetOnEntry = source.resetOnEntry;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(final boolean beingRequested) {
        this.requested = beingRequested;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the current value.
     * @return the current value
     */
    public String getCurrentValue() {
        return this.currentValue;
    }

    /**
     * Sets the current value.
     * @param currentValue the current value
     */
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * Gets whether it's mandatory or not.
     * @return whether it's mandatory or not
     */
    public boolean isMandatory() {
        return this.isMandatory;
    }

    /**
     * Sets whether it's mandatory or not.
     * @param isMandatory whether it's mandatory or not
     */
    public void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    /**
     * Gets a list of the entity keys.
     * @return list of the entity keys
     */
    public List<String> getEntityKeys() {
        return this.entityKeys;
    }

    /**
     * Sets the list of the entity keys
     * @param entityKeys the list of the entity keys
     */
    public void setEntityKeys(List<String> entityKeys) {
        this.entityKeys = entityKeys;
    }

    public int getTimesPrompted() {
        return this.timesPrompted;
    }

    /**
     * Sets how many times it has been prompted.
     * @param timesPrompted how many times it has been prompted.
     */
    public void setTimesPrompted(int timesPrompted) {
        this.timesPrompted = timesPrompted;
    }

    /**
     * Gets how many times to prompt.
     * @return how many times to prompt
     */
    public int getTimesToPrompt() {
        return this.timesToPrompt;
    }

    /***
     * Sets how many times to prompt.
     * @param timesToPrompt how many times to prompt.
     */
    public void setTimesToPrompt(int timesToPrompt) {
        this.timesToPrompt = timesToPrompt;
    }

    /**
     * Gets whether the entity is persistent.
     * @return true if true, else false.
     */
    public boolean getIsPersistent() {
        return this.persistent;
    }

    /**
     * Sets whether the entity is persistent.
     * @param persistent Whether the entity should be persistent.
     */
    public void setIsPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Gets whether a persistent value for an entity is ignored when the intent is triggered.
     * @return
     */
    public boolean getResetOnEntry() { 
        return this.resetOnEntry; 
    }


    /**
     * Sets whether an existing value for an entity is ignored when the intent is triggered.
     * @param resetOnEntry Whether the entity should be reset.
     */
    public void setResetOnEntry(final boolean resetOnEntry) {
        this.resetOnEntry = resetOnEntry; 
    }

    /**
     * Gets the prompts.
     * @return the prompts
     */
    public List<String> getPrompts() {
        return this.prompts;
    }

    /**
     * Sets the prompts.
     * @param prompts the prompts
     */
    public void setPrompts(List<String> prompts) {
        this.prompts = prompts;
    }

    /**
     * Gets whether this is a system variable or a custom one.
     * @return whether this is a system variable or a custom one
     */
    public boolean isSystem() {
        return this.isSystem;
    }

    /**
     * Gets the label.
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label.
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /***
     * Clone a version of the memory intent that we can send back to the user
     */
    public static MemoryVariable getUserViewable(final MemoryVariable source) {
        return new MemoryVariable(source);
    }

}
