package com.hutoma.api.containers.sub;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 07/10/2016.
 */
public class IntentVariable {

    private String entity_name;
    private boolean required;
    private int n_prompts;
    private String value;

    private List<String> prompts;

    public IntentVariable(String entity_name, boolean required, int n_prompts, String value) {
        this.entity_name = entity_name;
        this.required = required;
        this.n_prompts = n_prompts;
        this.value = value;
        this.prompts = new ArrayList<>();
    }

    public IntentVariable addPrompt(String prompt) {
        this.prompts.add(prompt);
        return this;
    }

    public String getEntityName() {
        return this.entity_name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public int getNumPrompts() {
        return this.n_prompts;
    }

    public List<String> getPrompts() {
        return this.prompts;
    }
}
