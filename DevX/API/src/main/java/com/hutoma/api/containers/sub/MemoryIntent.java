package com.hutoma.api.containers.sub;

import com.hutoma.api.common.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by pedrotei on 06/10/16.
 */
public class MemoryIntent {

    private List<MemoryVariable> variables = new ArrayList<>();
    private String name;
    private UUID chatId;
    private UUID aiid;
    private boolean isFulfilled;

    /**
     * Ctor.
     * @param name the intent name
     * @param aiid the AI ID
     * @param chatId the Chat ID
     * @param variables the list of memory variables
     */
    public MemoryIntent(final String name, final UUID aiid, final UUID chatId, final List<MemoryVariable> variables) {
        this.name = name;
        this.aiid = aiid;
        this.chatId = chatId;
        this.variables = variables;
    }

    /**
     * Gets the AI ID.
     * @return the AI ID
     */
    public UUID getAiid() {
        return aiid;
    }

    /**
     * Gets the CharId.
     * @return the CharId
     */
    public UUID getChatId() {
        return chatId;
    }

    /**
     * Gets the list of all unfulfilled mandatory variables.
     * @return the list of all unfulfilled mandatory variables
     */
    public List<MemoryVariable> getUnfulfilledVariables() {
        return variables.stream().filter(v -> v.isMandatory() && v.getCurrentValue() == null)
                .collect(Collectors.toList());
    }

    /**
     * Gets whether this intent has been fulfilled or not (all mandatory variables have been set).
     * @return whether this intent has been fulfilled or not
     */
    public boolean isFulfilled() {
        return this.isFulfilled;
    }

    public void setIsFulfilled(final boolean state) {
        this.isFulfilled = state;
    }

    public void fulfillVariables(List<Pair<String, String>> entities) {
        for (Pair<String, String> entity: entities) {
            Optional<MemoryVariable> optVariable = variables.stream()
                    .filter(x -> x.getName().equalsIgnoreCase(entity.getA())).findFirst();
            if (optVariable.isPresent()) {
                MemoryVariable variable = optVariable.get();
                variable.setCurrentValue(entity.getB());
            }
        }
    }

    /**
     * Gets all the memory variables for this intent.
     * @return all the memory variables for this intent
     */
    public List<MemoryVariable> getVariables() {
        return variables;
    }

    /**
     * Gets the name of this intent.
     * @return the name of this intent
     */
    public String getName() {
        return name;
    }
}