package com.hutoma.api.containers.sub;

import com.hutoma.api.common.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Memory Intents.
 */
public class MemoryIntent {

    private final String name;
    private final UUID chatId;
    private final UUID aiid;
    private List<MemoryVariable> variables = new ArrayList<>();
    private boolean isFulfilled;
    private Map<String, MemoryVariable> variablesMap;

    /**
     * Ctor.
     * @param name        the intent name
     * @param aiid        the AI ID
     * @param chatId      the Chat ID
     * @param variables   the list of memory variables
     * @param isFulfilled whether it's fulfilled or not
     */
    public MemoryIntent(final String name, final UUID aiid, final UUID chatId, final List<MemoryVariable> variables,
                        final boolean isFulfilled) {
        this.name = name;
        this.aiid = aiid;
        this.chatId = chatId;
        this.variables = variables;
        this.isFulfilled = isFulfilled;
        this.variablesMap = getMapFromVariablesList(this.variables);

    }

    /**
     * Ctor.
     * @param name      the intent name
     * @param aiid      the AI ID
     * @param chatId    the Chat ID
     * @param variables the list of memory variables
     */
    public MemoryIntent(final String name, final UUID aiid, final UUID chatId, final List<MemoryVariable> variables) {
        this(name, aiid, chatId, variables, false);
    }

    /**
     * Gets the AI ID.
     * @return the AI ID
     */
    public UUID getAiid() {
        return this.aiid;
    }

    /**
     * Gets the CharId.
     * @return the CharId
     */
    public UUID getChatId() {
        return this.chatId;
    }

    /**
     * Gets the list of all unfulfilled mandatory variables.
     * @return the list of all unfulfilled mandatory variables
     */
    public List<MemoryVariable> getUnfulfilledVariables() {
        return this.variables.stream().filter(v -> v.isMandatory() && v.getCurrentValue() == null)
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
        for (Pair<String, String> entity : entities) {
            Optional<MemoryVariable> optVariable = this.variables.stream()
                    .filter(x -> x.getName().equalsIgnoreCase(entity.getA())).findFirst();
            if (optVariable.isPresent()) {
                MemoryVariable variable = optVariable.get();
                variable.setCurrentValue(entity.getB());
                variable.setRequested(false);
            }
        }
    }

    /**
     * Gets all the memory variables for this intent.
     * @return all the memory variables for this intent
     */
    public List<MemoryVariable> getVariables() {
        return this.variables;
    }

    public Map<String, MemoryVariable> getVariablesMap() {
        return this.variablesMap;
    }

    private static Map<String, MemoryVariable> getMapFromVariablesList(final List<MemoryVariable> vars) {
        if (vars == null) {
            return new HashMap<>();
        }
        return vars.stream().collect(Collectors.toMap(MemoryVariable::getLabel, x -> x));
    }

    /**
     * Gets the name of this intent.
     * @return the name of this intent
     */
    public String getName() {
        return this.name;
    }
}