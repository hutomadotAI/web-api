package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 07/10/2016.
 */
public class IntentVariable {

    @SerializedName("entity_name")
    private final String entityName;
    private final boolean required;
    @SerializedName("n_prompts")
    private final int numberOfPrompts;
    private String value;
    private int id;

    private List<String> prompts;

    public IntentVariable(String entityName, boolean required, int numPrompts, String value) {
        this.entityName = entityName;
        this.required = required;
        this.numberOfPrompts = numPrompts;
        this.value = value;
        this.prompts = new ArrayList<>();
    }

    public IntentVariable(String entityName, boolean required, int numPrompts, String value, int id) {
        this(entityName, required, numPrompts, value);
        this.id = id;
    }

    public IntentVariable addPrompt(String prompt) {
        this.prompts.add(prompt);
        return this;
    }

    public String getEntityName() {
        return this.entityName;
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

    public int getId() {
        return this.id;
    }
}
