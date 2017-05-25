package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    private static final String LOGFROM = "chatlogic";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Tools tools;
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IEntityRecognizer entityRecognizer;
    private final AIChatServices chatServices;
    private final ChatLogger chatLogger;
    private final WebHooks webHooks;
    private final ChatStateHandler chatStateHandler;
    private LogMap telemetryMap;
    private float minP;
    private ChatState chatState;

    @Inject
    public ChatLogic(final Config config, final JsonSerializer jsonSerializer, final AIChatServices chatServices,
                     final Tools tools, final ILogger logger, final IMemoryIntentHandler intentHandler,
                     final IEntityRecognizer entityRecognizer, final ChatLogger chatLogger, final WebHooks webHooks,
                     final ChatStateHandler chatStateHandler) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.intentHandler = intentHandler;
        this.entityRecognizer = entityRecognizer;
        this.chatLogger = chatLogger;
        this.webHooks = webHooks;
        this.chatStateHandler = chatStateHandler;
    }

    public ApiResult chat(final UUID aiid, final UUID devId, final String question, final String chatId,
                          final String history, final String topic, final float minP) {

        // TODO: Bug#1349 - topic is now ignored if passed from the caller
        final String devIdString = devId.toString();
        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);
        this.minP = minP;
        this.chatState = this.chatStateHandler.getState(devId, chatUuid);

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        // Set the timestamp of the request
        apiChat.setTimestamp(startTime);

        // Add telemetry for the request
        this.telemetryMap = LogMap.map("DevId", devId)
                .put("AIID", aiid)
                .put("Topic", this.chatState.getTopic())
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                .put("ChatId", chatUuid)
                .put("History", history)
                .put("Q", question)
                .put("MinP", minP);

        UUID aiidForMemoryIntents = this.chatState.getLockedAiid() == null ? aiid : this.chatState.getLockedAiid();
        List<MemoryIntent> intentsForChat = this.intentHandler.getCurrentIntentsStateForChat(aiidForMemoryIntents, chatUuid);

        // For now we should only have one active intent per chat.
        MemoryIntent currentIntent = intentsForChat.isEmpty() ? null : intentsForChat.get(0);
        ChatResult result = new ChatResult(question);

        try {

            // Check if we're in the middle of an intent flow and process it.
            if (processIntent(devId, aiidForMemoryIntents, currentIntent, question, result)) {
                // Intent was handled, confidence is high
                result.setScore(1.0d);
            } else {
                // Otherwise just go through the regular chat flow

                // async start requests to all servers
                this.chatServices.startChatRequests(devId, aiid, chatUuid, question, history,
                        this.chatState.getTopic());

                // wait for WNET to return
                result = this.interpretSemanticResult(question);

                boolean wnetConfident = false;
                if (result != null) {
                    // are we confident enough with this reply?
                    wnetConfident = (result.getScore() >= minP) && (result.getScore() > 0.00001d);
                    this.telemetryMap.add("WNETScore", result.getScore());
                    this.telemetryMap.add("WNETConfident", wnetConfident);

                    if (wnetConfident) {
                        // if we are taking WNET's reply then process intents
                        UUID aiid_from_result = result.getAiid();
                        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                                aiid_from_result, chatUuid, result.getAnswer());
                        if (memoryIntent != null // Intent was recognized
                                && !memoryIntent.isFulfilled()) {

                            this.telemetryMap.add("IntentRecognized", memoryIntent.getName());

                            if (processIntent(devId, aiid_from_result, memoryIntent, question, result)) {
                                this.telemetryMap.add("AnsweredBy", "WNET");
                            } else {
                                // if intents processing returns false then we need to ignore WNET
                                wnetConfident = false;
                            }
                        } else {
                            this.telemetryMap.add("AnsweredBy", "WNET");
                        }
                    }
                }
                this.telemetryMap.add("AnsweredWithConfidence", wnetConfident);
                this.telemetryMap.add("WNETAnswered", result != null);

                if (!wnetConfident) {
                    // otherwise,
                    // wait for the AIML server to respond
                    ChatResult aimlResult = this.interpretAimlResult(question);

                    boolean aimlConfident = false;
                    // If we don't have AIML available (not linked)
                    if (aimlResult != null) {
                        // are we confident enough with this reply?
                        aimlConfident = aimlResult.getScore() > 0.00001d;
                        this.telemetryMap.add("AIMLScore", aimlResult.getScore());
                        this.telemetryMap.add("AIMLConfident", aimlConfident);
                        if (aimlConfident) {
                            this.telemetryMap.add("AnsweredBy", "AIML");
                            this.telemetryMap.add("AnsweredWithConfidence", true);
                            result = aimlResult;
                        }
                    }
                    this.telemetryMap.add("AIMLAnswered", aimlResult != null);

                    if (aimlResult == null || !aimlConfident) {
                        // get a response from the RNN
                        ChatResult rnnResult = this.interpretRnnResult(question);

                        // Currently RNN "cannot be trusted" as it doesn't provide an accurate confidence level
                        this.telemetryMap.add("AnsweredWithConfidence", false);

                        boolean answeredByRnn = false;

                        if (rnnResult != null) {
                            this.telemetryMap.add("RNNScore", rnnResult.getScore());
                            // If the RNN was clueless or returned an empty response
                            if (rnnResult.getAnswer() != null && !rnnResult.getAnswer().isEmpty()) {
                                result = rnnResult;
                                this.telemetryMap.add("AnsweredBy", "RNN");
                                answeredByRnn = true;
                            }
                        }

                        if (!answeredByRnn) {
                            if (aimlResult != null) {
                                this.telemetryMap.add("FellBackToAIML", "true");
                                this.telemetryMap.add("AnsweredBy", "AIML");
                                result = aimlResult;
                            } else {
                                // TODO we need to figure out something
                                this.telemetryMap.add("AnsweredBy", "NONE");
                                result = getImCompletelyLostChatResult(chatUuid, question);
                            }
                        }

                        this.telemetryMap.add("RNNAnswered", rnnResult != null);
                    }
                }
            }

            // set the history to the answer, unless we have received a reset command,
            // in which case send an empty string
            result.setHistory(result.isResetConversation() ? "" : result.getAnswer());

            // prepare to send back a result
            result.setScore(toOneDecimalPlace(result.getScore()));

            // set the chat response time to the whole duration since the start of the request until now
            result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);

            apiChat.setResult(result);

        } catch (RequestBase.AiNotFoundException notFoundException) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not found", devIdString,
                    LogMap.map("Message", notFoundException.getMessage()).put("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, notFoundException, this.telemetryMap);
            return ApiError.getNotFound("Bot not found");

        } catch (AIChatServices.AiNotReadyToChat ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not ready", devIdString, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getBadRequest(
                    "This bot is not ready to chat. It needs to train and/or be linked to other bots");

        } catch (IntentException | RequestBase.AiControllerException | ServerConnector.AiServicesException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat - " + ex.getClass().getSimpleName(),
                    devIdString, ex, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getInternalServerError();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat", devIdString, e);
            this.chatLogger.logChatError(LOGFROM, devIdString, e, this.telemetryMap);
            return ApiError.getInternalServerError();
        } finally {
            // once we've picked a result, abandon all the others to prevent hanging threads
            this.chatServices.abandonCalls();
        }

        this.chatState.setTopic(apiChat.getResult().getTopicOut());
        this.chatStateHandler.saveState(devId, chatUuid, this.chatState);

        this.telemetryMap.add("RequestDuration", result.getElapsedTime());
        this.telemetryMap.add("ResponseSent", result.getAnswer());
        this.telemetryMap.add("Score", result.getScore());
        this.telemetryMap.add("LockedToAi",
                this.chatState.getLockedAiid() == null ? "" : this.chatState.getLockedAiid().toString());

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, this.telemetryMap);
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString, LogMap.map("AIID", aiid).put("SessionId", chatId));
        return apiChat.setSuccessStatus();
    }

    public ApiResult assistantChat(UUID aiid, UUID devId, String question, String chatId,
                                   String history, String topic, float minP) {
        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // Add telemetry for the request
        this.telemetryMap = LogMap.map("DevId", devId)
                .put("AIID", aiid)
                .put("Topic", topic)
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                .put("ChatId", chatUuid.toString())
                .put("History", history)
                .put("Q", question);

        ChatResult result = new ChatResult(question);
        result.setElapsedTime(this.tools.getTimestamp() - startTime);

        // Set a fixed response.
        result.setAnswer("Hello");
        result.setScore(1.0);

        // set the chat response time to the whole duration since the start of the request until now
        result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);
        this.telemetryMap.add("RequestDuration", result.getElapsedTime());

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        apiChat.setResult(result);

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "AssistantChat", devId.toString(), this.telemetryMap);
        return apiChat.setSuccessStatus();
    }

    private boolean processIntent(final UUID devId, final UUID aiid, final MemoryIntent currentIntent,
                                  final String question, ChatResult chatResult)
            throws IntentException {
        final String devIdString = devId.toString();
        if (currentIntent == null) {
            // no intent to process
            return false;
        }

        List<MemoryIntent> intentsToClear = new ArrayList<>();
        boolean handledIntent = false;

        // Populate persistent entities.
        for (MemoryVariable variable : currentIntent.getVariables()) {
            String persistentValue = this.chatState.getEntityValue(variable.getName());
            if (persistentValue != null) {
                variable.setCurrentValue(persistentValue);
            }
        }

        // Attempt to retrieve entities from the question
        List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(question,
                currentIntent.getVariables());
        if (!entities.isEmpty()) {
            currentIntent.fulfillVariables(entities);

            // Write recognised persistent entities.
            for (Object entity : currentIntent.getVariables()
                    .stream()
                    .filter(x -> x.getIsPersistent() && x.getCurrentValue() != null)
                    .toArray()) {
                MemoryVariable memoryVariable = (MemoryVariable)entity;
                this.chatState.setEntityValue(memoryVariable.getName(), memoryVariable.getCurrentValue());
            }
        }

        // Check if there still are mandatory entities not currently fulfilled
        List<MemoryVariable> vars = currentIntent.getUnfulfilledVariables();
        if (vars.isEmpty()) {
            notifyIntentFulfilled(chatResult, currentIntent, aiid);

            // If the webhook returns a text response, overwrite the answer.
            if (this.webHooks.activeWebhookExists(currentIntent, devId)) {
                WebHookResponse response = this.webHooks.executeWebHook(currentIntent, chatResult, devId);

                if (response == null) {
                    this.logger.logUserErrorEvent(LOGFROM,
                            "Error occured executing WebHook for intent %s for aiid %s.",
                            devIdString,
                            LogMap.map("Intent", currentIntent.getName()).put("AIID", aiid));
                } else if (response.getText() != null && !response.getText().isEmpty()) {
                    chatResult.setAnswer(response.getText());
                }
            }

            intentsToClear.add(currentIntent);
            handledIntent = true;
        } else {
            // For now get the first unfulfilled variable with numPrompts < maxPrompts
            // or we could do random just to make it a 'surprise!' :)
            Optional<MemoryVariable> optVariable = vars.stream()
                    .filter(x -> x.getTimesPrompted() < x.getTimesToPrompt()).findFirst();
            if (optVariable.isPresent()) {
                MemoryVariable variable = optVariable.get();
                if (variable.getPrompts() == null || variable.getPrompts().isEmpty()) {
                    // Should not happen as this should be validated during creation
                    this.logger.logUserErrorEvent(LOGFROM, "HandleIntents - variable with no prompts defined",
                            devIdString, LogMap.map("AIID", aiid).put("Intent", currentIntent.getName())
                                    .put("Variable", variable.getName()));
                    throw new IntentException(
                            String.format("Entity %s for intent %s does not specify any prompts",
                                    currentIntent.getName(), variable.getName()));
                } else {
                    // And prompt the user for the value for that variable
                    int pos = variable.getTimesPrompted() < variable.getPrompts().size()
                            ? variable.getTimesPrompted()
                            : 0;
                    chatResult.setAnswer(variable.getPrompts().get(pos));
                    // and decrement the number of prompts
                    variable.setTimesPrompted(variable.getTimesPrompted() + 1);
                    this.telemetryMap.add("IntentPrompt",
                            String.format("intent:'%s' variable:'%s' currentPrompt:%d/%d",
                                    currentIntent.getName(), variable.getName(),
                                    variable.getTimesPrompted(),
                                    variable.getTimesToPrompt()));
                    handledIntent = true;
                }
            } else { // intent not fulfilled but no variables left to handle
                // if we run out of n_prompts we just stop asking.
                // the user can still answer the question ... or not
                this.telemetryMap.add("IntentNotFulfilled", currentIntent.getName());
                intentsToClear.add(currentIntent);
            }
        }

        chatResult.setIntents(Collections.singletonList(currentIntent));


        if (!intentsToClear.contains(currentIntent)) {
            this.intentHandler.updateStatus(currentIntent);
        }

        // Clear fulfilled intents or intents which have exhausted their prompts, so they can be triggered again
        if (!intentsToClear.isEmpty()) {
            this.intentHandler.clearIntents(intentsToClear);
        }

        return handledIntent;
    }

    private ChatResult getTopScore(final Map<UUID, ChatResult> chatResults, final String question) {
        // Check if the currently locked bot still has an acceptable response
        if (this.chatState.getLockedAiid() != null && chatResults.containsKey(this.chatState.getLockedAiid())) {
            ChatResult result = chatResults.get(this.chatState.getLockedAiid());
            if (result.getScore() >= this.minP) {
                return result;
            }
        }
        UUID responseFromAi = null;
        ChatResult chatResult = new ChatResult(question);
        for (Map.Entry<UUID, ChatResult> entry : chatResults.entrySet()) {
            if (entry.getValue().getScore() >= chatResult.getScore()) {
                chatResult = entry.getValue();
                responseFromAi = entry.getKey();
            }
        }
        // lock to this AI
        this.chatState.setLockedAiid(responseFromAi);
        chatResult.setQuery(question);
        return chatResult;
    }

    private ChatResult interpretSemanticResult(final String question) throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitWnet();
        if (allResults == null) {
            return null;
        }
        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question);
        UUID aiid = chatResult.getAiid();
        this.telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        if (chatResult.getAnswer() != null) {
            // if we receive a reset command then remove the command and flag the status
            if (chatResult.getAnswer().contains(HISTORY_REST_DIRECTIVE)) {
                chatResult.setResetConversation(true);
                chatResult.setAnswer(chatResult.getAnswer()
                        .replace(HISTORY_REST_DIRECTIVE, ""));
            } else {
                chatResult.setResetConversation(false);
            }

            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            this.telemetryMap.add("WNETResponseNULL", "true");
        }

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.add("WNETAnswer", chatResult.getAnswer());
        this.telemetryMap.add("WNETTopicOut", chatResult.getTopicOut());
        this.telemetryMap.add("WNETElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }

    private ChatResult interpretAimlResult(final String question) throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitAiml();
        if (allResults == null) {
            return null;
        }

        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question);
        UUID aiid = chatResult.getAiid();
        this.telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.add("AIMLAnswer", chatResult.getAnswer());
        this.telemetryMap.add("AIMLElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }

    private ChatResult interpretRnnResult(final String question) throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitRnn();
        if (allResults == null) {
            return null;
        }

        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question);
        UUID aiid = chatResult.getAiid();
        this.telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        if (chatResult.getAnswer() != null) {
            // always reset the conversation if we have gone with a non-wnet result
            chatResult.setResetConversation(true);
            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            this.telemetryMap.add("RNNResponseNULL", "");
        }

        this.logger.logDebug(LOGFROM, String.format("RNN response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.add("RNNElapsedTime", chatResult.getElapsedTime());
        this.telemetryMap.add("RNNAnswer", chatResult.getAnswer());
        this.telemetryMap.add("RNNTopicOut", chatResult.getTopicOut());

        return chatResult;
    }

    private double toOneDecimalPlace(double input) {
        return Math.round(input * 10.0d) / 10.0d;
    }

    private void notifyIntentFulfilled(ChatResult chatResult, MemoryIntent memoryIntent, UUID aiid) {
        memoryIntent.setIsFulfilled(true);
        ApiIntent intent = this.intentHandler.getIntent(aiid, memoryIntent.getName());
        if (intent != null) {
            List<String> responses = intent.getResponses();
            chatResult.setAnswer(responses.get((int) (Math.random() * responses.size())));
        }
        this.telemetryMap.add("IntentFulfilled", memoryIntent.getName());

    }

    private ChatResult getImCompletelyLostChatResult(final UUID chatId, final String question) {
        ChatResult result = new ChatResult(question);
        result.setChatId(chatId);
        result.setScore(0.0);
        result.setAnswer("Erm... What?");
        result.setContext("");
        result.setTopicOut("");
        return result;
    }

    static class IntentException extends Exception {
        public IntentException(final String message) {
            super(message);
        }
    }

}



