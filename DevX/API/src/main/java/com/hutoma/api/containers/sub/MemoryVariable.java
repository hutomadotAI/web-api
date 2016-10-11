package com.hutoma.api.containers.sub;

import java.util.List;

/**
 * Created by pedrotei on 06/10/16.
 */
public class MemoryVariable {
    private String name;
    private String currentValue;
    private boolean isMandatory;
    private List<String> entityKeys;
    private List<String> prompts;
    private int timesPrompted;

    /**
     * Ctor.
     * @param name the name
     * @param entityKeys the entity keys
     */
    public MemoryVariable(String name, List<String> entityKeys) {
        this.name = name;
        this.entityKeys = entityKeys;
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
     * @param isMandatory  whether it's mandatory or not
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

    /**
     * Gets how many times to prompt.
     * @return how many times to prompt
     */
    public int getTimesToPrompt() {
        return this.timesPrompted;
    }

    /**
     * Sets how many times to prompt.
     * @param timesPrompted how many times to prompt
     */
    public void setTimesPrompted(int timesPrompted) {
        this.timesPrompted = timesPrompted;
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

}
