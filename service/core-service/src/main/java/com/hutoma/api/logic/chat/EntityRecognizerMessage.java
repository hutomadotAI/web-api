package com.hutoma.api.logic.chat;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRecognizerMessage {
    @SerializedName("conversation")
    private String conversation;
    @SerializedName("entities")
    private HashMap<String, List<String>> entities;
    @SerializedName("regex_entities")
    private HashMap<String, String> regexEntities;

    /**
     * Ctor
     */
    public EntityRecognizerMessage() {
        entities = new HashMap<String, List<String>>();
        regexEntities = new HashMap<String, String>();
    }

    /**
     * Gets the conversation message
     */
    public String getConversation() {
        return this.conversation;
    }

    /**
     * Sets the conversation
     */
    public void setConversation(String message) {
        this.conversation = message;
    }

    /**
     * Gets the entities
     */
    public Map<String, List<String>> getEntities() {
        return this.entities;
    }

    /**
     * Gets the regex entities
     */
    public Map<String, String> getRegexEntities() {
        return this.regexEntities;
    }
}
