package com.hutoma.api.logic.chat;

import com.google.common.base.Strings;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

public class IntentProcessor {

    private static final String LOGFROM = "chatintenthandler";
    private static final String SYSANY = "sys.any";
    private final IEntityRecognizer entityRecognizer;
    private final IMemoryIntentHandler intentHandler;
    private final WebHooks webHooks;
    private final ILogger logger;

    @Inject
    public IntentProcessor(final IEntityRecognizer entityRecognizer,
                           final IMemoryIntentHandler intentHandler,
                           final WebHooks webHooks,
                           final ILogger logger) {
        this.entityRecognizer = entityRecognizer;
        this.intentHandler = intentHandler;
        this.webHooks = webHooks;
        this.logger = logger;
    }

    /**
     * Processes a given intent.
     * @param chatInfo             the chat request info
     * @param aiidForMemoryIntents the aiid for memory intents
     * @param currentIntent        the intent to process
     * @param chatResult           current chat result
     * @return whether there was an intent to process or not
     * @throws ChatLogic.IntentException WebHooks.WebHookException
     */
    public boolean processIntent(final ChatRequestInfo chatInfo,
                                 final UUID aiidForMemoryIntents,
                                 final MemoryIntent currentIntent,
                                 final ChatResult chatResult,
                                 final LogMap telemetryMap)
            throws ChatLogic.IntentException, WebHooks.WebHookException {

        if (currentIntent == null) {
            // no intent to process
            return false;
        }

        Map<String, Object> intentLog = new HashMap<>();
        intentLog.put("Name", currentIntent.getName());

        List<MemoryIntent> intentsToClear = new ArrayList<>();
        boolean handledIntent;

        // Are we in the middle of an entity value request?
        Optional<MemoryVariable> requestedVariable = currentIntent.getVariables()
                .stream().filter(MemoryVariable::isRequested).findFirst();

        try {

            if (requestedVariable.isPresent()) {
                MemoryVariable mv = requestedVariable.get();

                telemetryMap.add("EntityRequested.Name", mv.getName());
                telemetryMap.add("EntityRequested.Label", mv.getLabel());
                chatResult.setScore(1.0d);

                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(chatInfo.getQuestion(),
                        currentIntent.getVariables());
                // Did the recognizer find something for this entity?
                Optional<Pair<String, String>> entityValue = entities.stream()
                        .filter(x -> x.getA().equals(mv.getName())).findFirst();
                if (entityValue.isPresent() || mv.getName().equals(SYSANY)) {
                    handledIntent = processVariables(chatInfo, aiidForMemoryIntents, currentIntent, chatResult,
                            Collections.singletonList(mv), intentsToClear, intentLog, telemetryMap);

                } else {
                    // If we have prompted enough, then give up
                    if (mv.getTimesPrompted() >= mv.getTimesToPrompt()) {
                        mv.setRequested(false);
                        handledIntent = false;
                        // clear the intent whenever a mandatory variable is not set within
                        // the allowed number of prompts
                        intentsToClear.add(currentIntent);
                    } else {
                        promptForVariable(mv, chatResult, intentLog);
                        handledIntent = true;
                    }
                }

            } else {

                // Populate persistent entities.
                for (MemoryVariable variable : currentIntent.getVariables()) {
                    String persistentValue = chatResult.getChatState().getEntityValue(variable.getName());
                    if (persistentValue != null) {
                        variable.setCurrentValue(persistentValue);
                    }
                }

                // Do we have multiple entities with the same type?
                MemoryVariable variableToPrompt = getVariableToPromptFromEntityList(currentIntent.getVariables());

                // When we have multiple instances of a single entity type, we need to
                // prompt for them until they're all fulfilled
                if (variableToPrompt != null) {
                    // And prompt the user for the value for that variable
                    promptForVariable(variableToPrompt, chatResult, intentLog);
                    handledIntent = true;

                } else {
                    handledIntent = processVariables(chatInfo, aiidForMemoryIntents, currentIntent, chatResult,
                            currentIntent.getVariables(), intentsToClear, intentLog, telemetryMap);

                }
            }

        } catch (WebHooks.WebHookException ex) {
            // If we get a webhook exception it means the variables have been processed and we have fulfilled
            // the intent, thus triggering the webhook. So for now clear the variables so that the intent can be
            // fully processed again.
            this.intentHandler.clearIntents(chatResult.getChatState(), Collections.singletonList(currentIntent));

            // rethrow for bubbling up
            throw ex;
        }

        chatResult.setIntents(Collections.singletonList(currentIntent));

        if (currentIntent.isFulfilled()) {
            chatResult.getChatState().getCurrentIntents().remove(currentIntent);
        } else {
            if (!intentsToClear.contains(currentIntent)) {
                chatResult.getChatState().updateMemoryIntent(currentIntent);
            }
        }

        // Clear fulfilled intents or intents which have exhausted their prompts, so they can be triggered again
        if (!intentsToClear.isEmpty()) {
            this.intentHandler.clearIntents(chatResult.getChatState(), intentsToClear);
        }

        intentLog.put("Handled", handledIntent);
        telemetryMap.add("Intent", intentLog);

        return handledIntent;
    }

    private boolean processVariables(final ChatRequestInfo chatInfo,
                                     final UUID aiidForMemoryIntents,
                                     final MemoryIntent currentIntent,
                                     final ChatResult chatResult,
                                     final List<MemoryVariable> memoryVariables,
                                     final List<MemoryIntent> intentsToClear,
                                     final Map<String, Object> log,
                                     final LogMap telemetryMap)
            throws ChatLogic.IntentException, WebHooks.WebHookException {

        boolean handledIntent = false;


        List<Pair<String, String>> entities = null;
        if (!currentIntent.getVariables().isEmpty()) {
            // At this stage we're guaranteed to have variables with different entity types
            // Attempt to retrieve entities from the question
            entities = this.entityRecognizer.retrieveEntities(chatInfo.getQuestion(), memoryVariables);
        }

        if (entities != null && !entities.isEmpty()) {
            log.put("Entities retrieved", StringUtils.join(entities, ','));

            // If we're processing just one requested variable, we need to check if we have a value recognized
            // for it, and use it
            if (memoryVariables.size() == 1 && memoryVariables.get(0).getName().equals(entities.get(0).getA())) {
                memoryVariables.get(0).setCurrentValue(entities.get(0).getB());
                memoryVariables.get(0).setRequested(false);
            } else {
                // Otherwise try to fulfill variables the normal way
                currentIntent.fulfillVariables(entities);
            }

            // Write recognised persistent entities.
            for (Object entity : currentIntent.getVariables()
                    .stream()
                    .filter(x -> x.getIsPersistent() && x.getCurrentValue() != null)
                    .toArray()) {
                MemoryVariable memoryVariable = (MemoryVariable) entity;
                chatResult.getChatState().setEntityValue(memoryVariable.getName(), memoryVariable.getCurrentValue());
            }

            // Update context
            for (MemoryVariable var: currentIntent.getVariables()) {
                chatResult.getChatState().getChatContext().setValue(
                        String.format("%s.%s", currentIntent.getName(), var.getLabel()),
                        var.getCurrentValue());
            }
        }

        // Check if there still are mandatory entities not currently fulfilled
        List<MemoryVariable> vars = currentIntent.getUnfulfilledVariables();
        log.put("Fulfilled", vars.isEmpty());
        // assume all variables are filled until we find one we need to prompt for
        boolean allVariablesFilled = true;

        if (!vars.isEmpty()) {

            MemoryVariable variable = getNextVariableToPrompt(currentIntent);

            if (variable != null) {

                // we check if the variable is sys.any but also if the we prompted at least once
                // the prompt check is necessary otherwise the entity will be immediately recognised
                // before we even prompt for it.
                if (variable.getName().equalsIgnoreCase(SYSANY) && (variable.getTimesPrompted() > 0)) {
                    variable.setCurrentValue(chatInfo.getQuestion());
                    variable.setRequested(false);
                    MemoryVariable nextVariable = getNextVariableToPrompt(currentIntent);
                    if (nextVariable != null) {
                        promptForVariable(nextVariable, chatResult, log);
                    }
                    allVariablesFilled = nextVariable == null;
                    handledIntent = true;
                } else {
                    if (variable.getPrompts() == null || variable.getPrompts().isEmpty()) {
                        // Should not happen as this should be validated during creation
                        this.logger.logUserErrorEvent(LOGFROM, "HandleIntents - variable with no prompts defined",
                                chatInfo.getDevId().toString(),
                                LogMap.map("AIID", aiidForMemoryIntents)
                                        .put("Intent", currentIntent.getName())
                                        .put("Variable", variable.getName()));
                        throw new ChatLogic.IntentException(
                                String.format("Entity %s for intent %s does not specify any prompts",
                                        currentIntent.getName(), variable.getName()));
                    } else {
                        promptForVariable(variable, chatResult, log);
                        handledIntent = true;

                        // we had to prompt, set the variables filled flag to false
                        allVariablesFilled = false;
                    }
                }
            } else { // intent not fulfilled but no variables left to handle
                // if we run out of n_prompts we just stop asking.
                // the user can still answer the question ... or not
                telemetryMap.add("IntentNotFulfilled", currentIntent.getName());
                intentsToClear.add(currentIntent);
                allVariablesFilled = false;
            }
        }

        if (allVariablesFilled) {
            notifyIntentFulfilled(chatResult, currentIntent, aiidForMemoryIntents, telemetryMap);
            checkAndExecuteWebhook(chatInfo, aiidForMemoryIntents, currentIntent, chatResult, log,
                    telemetryMap);
            intentsToClear.add(currentIntent);
            handledIntent = true;
        }

        return handledIntent;
    }

    private MemoryVariable getNextVariableToPrompt(final MemoryIntent currentIntent) {
        MemoryVariable variable = getVariableToPromptFromEntityList(currentIntent.getVariables());
        List<MemoryVariable> vars = currentIntent.getUnfulfilledVariables();

        if (variable == null) {
            // For now get the first unfulfilled variable with numPrompts < maxPrompts
            // or we could do random just to make it a 'surprise!' :)
            Optional<MemoryVariable> optVariable = vars.stream()
                    .filter(x -> x.getTimesPrompted() <= x.getTimesToPrompt()).findFirst();
            if (optVariable.isPresent()) {
                variable = optVariable.get();
            }
        }

        return variable;
    }

    private MemoryVariable getVariableToPromptFromEntityList(List<MemoryVariable> variables) {
        Map<String, List<MemoryVariable>> entitiesMap = new HashMap<>();
        for (MemoryVariable variable : variables) {
            List<MemoryVariable> list = entitiesMap.containsKey(variable.getName())
                    ? entitiesMap.get(variable.getName())
                    : new ArrayList<>();
            list.add(variable);
            entitiesMap.put(variable.getName(), list);
        }

        MemoryVariable variableToPrompt = null;
        for (Map.Entry<String, List<MemoryVariable>> entry : entitiesMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                for (MemoryVariable mv : entry.getValue()) {
                    // and if there is any unfulfilled
                    if (mv.isMandatory() && mv.getCurrentValue() == null
                            && mv.getTimesPrompted() < mv.getTimesToPrompt()) {
                        variableToPrompt = mv;
                        break;
                    }
                }
                if (variableToPrompt != null) {
                    break;
                }
            }
        }
        return variableToPrompt;
    }

    private void promptForVariable(final MemoryVariable variable, final ChatResult chatResult,
                                   final Map<String, Object> log) {
        // And prompt the user for the value for that variable
        int pos = variable.getTimesPrompted() < variable.getPrompts().size()
                ? variable.getTimesPrompted()
                : 0;
        chatResult.setAnswer(variable.getPrompts().get(pos));
        // keep a record of what this response is a prompt for
        chatResult.setPromptForIntentVariable(variable.getLabel());
        // and decrement the number of prompts
        variable.setTimesPrompted(variable.getTimesPrompted() + 1);
        variable.setRequested(true);
        log.put("Variable name", variable.getName());
        log.put("Variable label", variable.getLabel());
        log.put("Variable times prompted", variable.getTimesPrompted());
        log.put("Variable times to prompt", variable.getTimesToPrompt());
    }

    private void notifyIntentFulfilled(final ChatResult chatResult, final MemoryIntent memoryIntent, final UUID aiid,
                                       final LogMap telemetryMap) {
        memoryIntent.setIsFulfilled(true);
        chatResult.setAnswer(getRandomIntentResponse(aiid, memoryIntent));
        telemetryMap.add("IntentFulfilled", memoryIntent.getName());
    }

    private String getRandomIntentResponse(final UUID aiid, final MemoryIntent memoryIntent) {
        ApiIntent intent = this.intentHandler.getIntent(aiid, memoryIntent.getName());
        if (intent != null) {
            List<String> responses = intent.getResponses();
            String response = responses.get((int) (Math.random() * responses.size()));
            for (MemoryVariable variable : memoryIntent.getVariables()) {
                response = response.replace("$" + variable.getLabel(), variable.getCurrentValue());
            }
            return response;
        }
        return null;
    }

    private void checkAndExecuteWebhook(final ChatRequestInfo chatInfo, final UUID aiidForMemoryIntents,
                                        final MemoryIntent currentIntent,
                                        final ChatResult chatResult, final Map<String, Object> log,
                                        final LogMap telemetryMap)
            throws WebHooks.WebHookException {

        if (currentIntent == null) {
            return;
        }

        // If the webhook returns a text response, overwrite the answer.
        WebHook webHook = this.webHooks.getWebHookForIntent(currentIntent, chatInfo.getDevId());
        if (webHook != null && webHook.isEnabled()) {
            log.put("Webhook run", true);

            WebHookResponse response;
            try {
                response = this.webHooks.executeIntentWebHook(webHook, currentIntent, chatResult, chatInfo);
                // first store the whole deserialized webhook in a transient field
                chatResult.setWebHookResponse(response);

                // log and set the text if there was any
                if (!Strings.isNullOrEmpty(response.getText())) {
                    chatResult.setAnswer(response.getText());
                    log.put("Webhook response", response.getText());
                } else {
                    // otherwise we got no text
                    this.logger.logUserInfoEvent(LOGFROM,
                            "Executing WebHook for intent for aiid: empty response.",
                            chatInfo.getDevId().toString(),
                            LogMap.map("Intent", currentIntent.getName()).put("AIID", aiidForMemoryIntents));
                }
            } catch (WebHooks.WebHookExternalException ex) {
                // Set the default response
                response = new WebHookResponse(getRandomIntentResponse(chatInfo.getAiid(), currentIntent));
                String webHookErrorString = ex.getMessage();
                this.logger.logUserWarnEvent(LOGFROM,
                        "Call to WebHook failed for intent",
                        chatInfo.getDevId().toString(),
                        LogMap.map("Intent", currentIntent.getName())
                                .put("AIID", aiidForMemoryIntents)
                                .put("Error", webHookErrorString));
                telemetryMap.add("webHookCallFailure", webHookErrorString);
            }

            // log the Facebook rich-content type if available
            if ((response.getFacebookNode() != null)
                    && (response.getFacebookNode().getContentType() != null)) {
                log.put("Webhook facebook response",
                        response.getFacebookNode().getContentType().name());
            }

        } else {
            log.put("Webhook run", false);
        }
    }
}
