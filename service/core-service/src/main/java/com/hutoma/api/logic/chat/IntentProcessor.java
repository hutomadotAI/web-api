package com.hutoma.api.logic.chat;

import com.google.common.base.Strings;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.IntentOutConditional;
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

/**
 * Handled all the intent processing logic.
 */
public class IntentProcessor {

    private static final double SCORE_INTENT_RECOGNIZED = 1.0d;
    private static final String LOGFROM = "chatintenthandler";
    private static final String SYSANY = "sys.any";
    private final IEntityRecognizer entityRecognizer;
    private final IMemoryIntentHandler intentHandler;
    private final WebHooks webHooks;
    private final ConditionEvaluator conditionEvaluator;
    private final ContextVariableExtractor contextVariableExtractor;
    private final ILogger logger;

    @Inject
    public IntentProcessor(final IEntityRecognizer entityRecognizer,
                           final IMemoryIntentHandler intentHandler,
                           final WebHooks webHooks,
                           final ConditionEvaluator conditionEvaluator,
                           final ContextVariableExtractor contextVariableExtractor,
                           final ILogger logger) {
        this.entityRecognizer = entityRecognizer;
        this.intentHandler = intentHandler;
        this.webHooks = webHooks;
        this.conditionEvaluator = conditionEvaluator;
        this.contextVariableExtractor = contextVariableExtractor;
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

        List<MemoryIntent> intentsToClear = new ArrayList<>();
        boolean handledIntent;
        Map<String, Object> intentLog = new HashMap<>();
        intentLog.put("Name", currentIntent.getName());

        chatResult.getChatState().restartChatWorkflow(false);

        ApiIntent intent = this.intentHandler.getIntent(aiidForMemoryIntents, currentIntent.getName());
        if (!canExecuteIntent(intent, chatResult)) {
            if (Strings.isNullOrEmpty(intent.getConditionsFallthroughMessage())) {
                return false;
            } else {
                chatResult.setScore(SCORE_INTENT_RECOGNIZED);
                chatResult.setAnswer(intent.getConditionsFallthroughMessage());
                chatResult.getChatState().clearFromCurrentIntents(Collections.singletonList(currentIntent));
                telemetryMap.add("AnsweredBy", "IntentProcessor");
                return true;
            }
        }

        // If the intent is gated on any conditionals, evaluate them
        if (!intent.getConditionsIn().isEmpty()) {
            ConditionEvaluator.Results results =
                    this.conditionEvaluator.evaluate(chatResult.getChatState().getChatContext());
            if (results.failed()) {
                ConditionEvaluator.Result failed = results.firstFailed();
                // failed cannot be null, but just doublecheck
                if (failed == null) {
                    this.logger.logError(LOGFROM, "No failed condition evaluation found when previously found!");
                } else {
                    this.logger.logInfo(LOGFROM, "IntentCondition - not met",
                            LogMap.map("AIID", aiidForMemoryIntents)
                                    .put("ChatId", chatResult.getChatId())
                                    .put("Intent", currentIntent.getName())
                                    .put("NumConditions", intent.getConditionsIn().size())
                                    .put("FailedCondition", failed));
                }
                // bail out as we don't have yet the conditions to trigger the intent execution
                return false;
            } else {
                this.logger.logInfo(LOGFROM, "IntentCondition - passed",
                        LogMap.map("AIID", aiidForMemoryIntents)
                                .put("ChatId", chatResult.getChatId())
                                .put("Intent", currentIntent.getName())
                                .put("NumConditions", intent.getConditionsIn().size()));
            }
        }

        // Make sure all context_in variables are read and applied to the chat state
        intent.getContextIn().forEach((k, v) -> chatResult.getChatState().getChatContext().setValue(k, v));

        // Are we in the middle of an entity value request?
        Optional<MemoryVariable> requestedVariable = currentIntent.getVariables()
                .stream().filter(MemoryVariable::isRequested).findFirst();

        try {

            if (requestedVariable.isPresent()) {
                MemoryVariable mv = requestedVariable.get();

                telemetryMap.add("EntityRequested.Name", mv.getName());
                telemetryMap.add("EntityRequested.Label", mv.getLabel());
                chatResult.setScore(SCORE_INTENT_RECOGNIZED);

                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(chatInfo.getQuestion(),
                        currentIntent.getVariables());
                // Did the recognizer find something for this entity?
                Optional<Pair<String, String>> entityValue = entities.stream()
                        .filter(x -> x.getA().equals(mv.getName())).findFirst();
                if (entityValue.isPresent() || mv.getName().equals(SYSANY)) {
                    handledIntent = processVariables(chatInfo, aiidForMemoryIntents, currentIntent, chatResult,
                            Collections.singletonList(mv), intentsToClear, intent, intentLog, telemetryMap);

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
                            currentIntent.getVariables(), intentsToClear, intent, intentLog, telemetryMap);

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

        // Intent was handled, confidence is high
        chatResult.setScore(SCORE_INTENT_RECOGNIZED);
        chatResult.setIntents(Collections.singletonList(currentIntent));
        telemetryMap.add("AnsweredBy", "IntentProcessor");

        if (currentIntent.isFulfilled()) {
            chatResult.getChatState().getCurrentIntents().remove(currentIntent);

            if (intent.getResetContextOnExit()) {
                chatResult.getChatState().getChatContext().clear();
                intentLog.put("ResetOnExit", intent.getResetContextOnExit());
            } else {
                // Make sure all context_out variables are read and applied to the chat state
                intent.getContextOut().forEach((k, v) -> chatResult.getChatState().getChatContext().setValue(k, v));
            }

            boolean hasNextedIntentToExecute = false;
            // Check if there are any conditions out
            if (!intent.getIntentOutConditionals().isEmpty()) {

                for (IntentOutConditional outConditional: intent.getIntentOutConditionals()) {
                    this.conditionEvaluator.setConditions(outConditional.getConditions());
                    ConditionEvaluator.Results results = this.conditionEvaluator.evaluate(
                            chatResult.getChatState().getChatContext());
                    if (results.passed()) {
                        try {
                            MemoryIntent intentToTrigger = this.intentHandler.buildMemoryIntentFromIntentName(
                                    chatInfo.getDevId(), chatInfo.getAiid(), outConditional.getIntentName(),
                                    chatInfo.getChatId());
                            chatResult.getChatState().getCurrentIntents().add(intentToTrigger);
                            chatResult.getChatState().restartChatWorkflow(true);
                            hasNextedIntentToExecute = true;
                            chatResult.getChatState().setInIntentLoop(true);
                            break;
                        } catch (DatabaseException ex) {
                            chatResult.getChatState().setInIntentLoop(false);
                            throw new ChatLogic.IntentException(ex);
                        }
                    }
                }
            }

            if (!hasNextedIntentToExecute) {
                chatResult.getChatState().setInIntentLoop(false);
            }


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
        telemetryMap.add("IntentStream", telemetryMap.get().containsKey("IntentStream")
                ? String.format("%s, %s", telemetryMap.get().get("IntentStream"), intent.getIntentName())
                : intent.getIntentName());

        if (handledIntent) {
            this.contextVariableExtractor.extractContextVariables(chatResult);
        }

        return handledIntent;
    }

    /**
     * Checks whether an intent can be executed based on the conditions associated with it.
     * @param intent     the intent
     * @param chatResult the current chat result
     * @return whether the intent can be executed or not
     */
    private boolean canExecuteIntent(final ApiIntent intent, final ChatResult chatResult) {
        // If the intent is gated on any conditionals, evaluate them
        this.conditionEvaluator.setConditions(intent.getConditionsIn());
        if (!intent.getConditionsIn().isEmpty()) {
            ConditionEvaluator.Results results =
                    this.conditionEvaluator.evaluate(chatResult.getChatState().getChatContext());
            if (results.failed()) {
                ConditionEvaluator.Result failed = results.firstFailed();
                // failed cannot be null, but just doublecheck
                if (failed == null) {
                    this.logger.logError(LOGFROM, "No failed condition evaluation found when previously found!");
                } else {
                    this.logger.logInfo(LOGFROM, "IntentCondition - not met",
                            LogMap.map("AIID", chatResult.getAiid())
                                    .put("ChatId", chatResult.getChatId())
                                    .put("Intent", intent.getIntentName())
                                    .put("NumConditions", intent.getConditionsIn().size())
                                    .put("FailedCondition", failed));
                }
                // bail out as we don't have yet the conditions to trigger the intent execution
                return false;
            } else {
                this.logger.logInfo(LOGFROM, "IntentCondition - passed",
                        LogMap.map("AIID", chatResult.getAiid())
                                .put("ChatId", chatResult.getChatId())
                                .put("Intent", intent.getIntentName())
                                .put("NumConditions", intent.getConditionsIn().size()));
            }
        }
        return true;
    }

    /**
     * Processes the intent's variables.
     * @param chatInfo             the chat request information
     * @param aiidForMemoryIntents AI id for the intent being processed
     * @param currentIntent        intent being processed
     * @param chatResult           the current chat result
     * @param memoryVariables      list of memory variables
     * @param intentsToClear       list of intents to clear
     * @param intent               intent definition
     * @param log                  log structure
     * @param telemetryMap         telemetry structure
     * @return whether the intent was handled or not
     * @throws ChatLogic.IntentException an exception during the intent processing
     * @throws WebHooks.WebHookException an exception while calling the webhook
     */
    private boolean processVariables(final ChatRequestInfo chatInfo,
                                     final UUID aiidForMemoryIntents,
                                     final MemoryIntent currentIntent,
                                     final ChatResult chatResult,
                                     final List<MemoryVariable> memoryVariables,
                                     final List<MemoryIntent> intentsToClear,
                                     final ApiIntent intent,
                                     final Map<String, Object> log,
                                     final LogMap telemetryMap)
            throws ChatLogic.IntentException, WebHooks.WebHookException {

        boolean handledIntent = false;


        List<Pair<String, String>> entities = null;
        if (!currentIntent.getVariables().isEmpty()
                && !chatResult.getChatState().isInIntentLoop()) { // we cannot infer variables in nested intents
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
            currentIntent.getVariables().forEach(
                    v -> chatResult.getChatState().getChatContext().setValue(v.getLabel(), v.getCurrentValue()));
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
            notifyIntentFulfilled(chatResult, currentIntent, aiidForMemoryIntents, intent, telemetryMap);
            checkAndExecuteWebhook(chatInfo, aiidForMemoryIntents, currentIntent, chatResult, intent, log,
                    telemetryMap);
            intentsToClear.add(currentIntent);
            handledIntent = true;
        }

        return handledIntent;
    }

    /**
     * Gets the next variable to prompt, if any.
     * @param currentIntent the current intent being processed
     * @return the memory variable (or null if no variable to prompt)
     */
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

    /**
     * Gets the next variable to prompt for from the variables list.
     * @param variables the variables list
     * @return the variable to prompt for next
     */
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

    /**
     * Updates the chat result to prompt the user with a variable.
     * @param variable   the variable to prompt for
     * @param chatResult the current chat result
     * @param log        the log structure
     */
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

    /**
     * Notifies that the intent has beel fulfilled.
     * @param chatResult   the current chat result
     * @param memoryIntent the memory intent
     * @param aiid         the AI id
     * @param intent       the intent
     * @param telemetryMap the telemetry structure
     */
    private void notifyIntentFulfilled(final ChatResult chatResult, final MemoryIntent memoryIntent,
                                       final UUID aiid, final ApiIntent intent,
                                       final LogMap telemetryMap) {
        memoryIntent.setIsFulfilled(true);
        chatResult.setAnswer(getRandomIntentResponse(aiid, memoryIntent, intent));

        telemetryMap.add("IntentFulfilled", memoryIntent.getName());
    }

    /**
     * Gets a random response form the list of intent responses.
     * @param aiid         the AI id
     * @param memoryIntent the memory intent
     * @param intent       the intent
     * @return the random response
     */
    private String getRandomIntentResponse(final UUID aiid, final MemoryIntent memoryIntent, final ApiIntent intent) {
        if (intent != null) {
            List<String> responses = intent.getResponses();
            String response = responses.get((int) (Math.random() * responses.size()));
            return response;
        }
        return null;
    }

    /**
     * Check whether there's a webhook to execute, and call it.
     * @param chatInfo             the chat request info
     * @param aiidForMemoryIntents the AI id for the intent in flight
     * @param currentIntent        the intent in flight
     * @param chatResult           the current chat result
     * @param intent               tje intent
     * @param log                  the log structure
     * @param telemetryMap         the telemetry structure
     * @throws WebHooks.WebHookException if there is an exception calling the webhook
     */
    private void checkAndExecuteWebhook(final ChatRequestInfo chatInfo,
                                        final UUID aiidForMemoryIntents,
                                        final MemoryIntent currentIntent,
                                        final ChatResult chatResult,
                                        final ApiIntent intent,
                                        final Map<String, Object> log,
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
                    // copy the text reply
                    chatResult.setAnswer(response.getText());
                    // and copy the whole response to include any rich content
                    chatResult.setWebHookResponse(response);
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
                response = new WebHookResponse(getRandomIntentResponse(chatInfo.getAiid(), currentIntent, intent));
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
