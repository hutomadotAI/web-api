package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.controllers.RequestBase;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.LinkedHashMap;
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

    private Map<String, String> telemetryMap;

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, AIChatServices chatServices,
                     Tools tools, ILogger logger, IMemoryIntentHandler intentHandler,
                     IEntityRecognizer entityRecognizer, ChatLogger chatLogger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.intentHandler = intentHandler;
        this.entityRecognizer = entityRecognizer;
        this.chatLogger = chatLogger;
    }

    public ApiResult chat(UUID aiid, String devId, String question, String chatId,
                          String history, String topic, float minP) {

        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);

        // Add telemetry for the request
        this.telemetryMap = new LinkedHashMap<String, String>() {
            {
                put("DevId", devId);
                put("AIID", aiid.toString());
                put("Topic", topic);
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                put("ChatId", chatUuid.toString());
                put("History", history);
                put("Q", question);
            }
        };

        try {
            // async start requests to all servers
            this.chatServices.startChatRequests(devId, aiid, chatUuid, question, history, topic);

            // wait for WNET to return
            ChatResult result = this.interpretSemanticResult();

            boolean wnetConfident = false;
            if (result != null) {
                // are we confident enough with this reply?
                wnetConfident = (result.getScore() >= minP) && (result.getScore() > 0.0d);
                this.telemetryMap.put("WNETConfident", Boolean.toString(wnetConfident));

                if (wnetConfident) {
                    // if we are taking WNET's reply then process intents
                    if (this.handleIntents(result, devId, aiid, chatUuid, question, this.telemetryMap)) {
                        this.telemetryMap.put("AnsweredBy", "WNET");
                        this.telemetryMap.put("AnsweredWithConfidence", "true");
                    } else {
                        // if intents processing returns false then we need to ignore WNET
                        wnetConfident = false;
                    }
                }
            }
            this.telemetryMap.put("WNETAnswered", Boolean.toString(result != null));

            if (!wnetConfident) {
                // otherwise,
                // wait for the AIML server to respond
                ChatResult aimlResult = this.interpretAimlResult();

                boolean aimlConfident = false;
                // If we don't have AIML available (not linked)
                if (aimlResult != null) {
                    // are we confident enough with this reply?
                    aimlConfident = aimlResult.getScore() > 0.0d;
                    this.telemetryMap.put("AIMLConfident", Boolean.toString(aimlConfident));
                    if (aimlConfident) {
                        this.telemetryMap.put("AnsweredBy", "AIML");
                        this.telemetryMap.put("AnsweredWithConfidence", "true");
                        result = aimlResult;
                    }
                }
                this.telemetryMap.put("AIMLAnswered", Boolean.toString(aimlResult != null));

                if (aimlResult == null || !aimlConfident) {
                    // get a response from the RNN
                    ChatResult rnnResult = this.interpretRnnResult();

                    if (rnnResult != null) {
                        // If the RNN was clueless or returned an empty response
                        if (rnnResult.getAnswer() == null || rnnResult.getAnswer().isEmpty()) {
                            // Mark it as not really answered
                            this.telemetryMap.put("AnsweredWithConfidence", "false");
                            // Use AIML, if available, use it as it will always generate something
                            if (aimlResult != null) {
                                this.telemetryMap.put("AnsweredBy", "AIML");
                                result = aimlResult;
                            } else {
                                // TODO we need to figure out something
                                this.telemetryMap.put("AnsweredBy", "NONE");
                                result = getImCompletelyLostChatResult(chatUuid);
                            }
                        } else {
                            result = rnnResult;
                            this.telemetryMap.put("AnsweredBy", "RNN");
                            this.telemetryMap.put("AnsweredWithConfidence", "true");
                        }
                    } else {
                        // TODO we need to figure out something
                        this.telemetryMap.put("AnsweredBy", "NONE");
                        result = getImCompletelyLostChatResult(chatUuid);
                    }

                    this.telemetryMap.put("RNNAnswered", Boolean.toString(rnnResult != null));
                }
            }

            // set the history to the answer, unless we have received a reset command,
            // in which case send an empty string
            result.setHistory(result.isResetConversation() ? "" : result.getAnswer());

            // prepare to send back a result
            result.setScore(toOneDecimalPlace(result.getScore()));

            // set the chat response time to the whole duration since the start of the request until now
            result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);
            this.telemetryMap.put("RequestDuration", Double.toString(result.getElapsedTime()));

            apiChat.setResult(result);

        } catch (RequestBase.AiNotFoundException notFoundException) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not found", devId,
                    "Message", notFoundException.getMessage(), "AIID", aiid.toString());
            this.chatLogger.logChatError(LOGFROM, devId, notFoundException, this.telemetryMap);
            return ApiError.getNotFound("AI not found");

        } catch (AIChatServices.AiNotReadyToChat ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not ready", "AIID", aiid.toString());
            this.chatLogger.logChatError(LOGFROM, devId, ex, this.telemetryMap);
            return ApiError.getBadRequest("This AI is not ready to chat. It needs to train and/or be linked to bots");

        } catch (ServerConnector.AiServicesException aiException) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI services exception", devId, "AIID", aiid.toString(),
                    "Exception", aiException.toString());
            this.chatLogger.logChatError(LOGFROM, devId, aiException, this.telemetryMap);
            return ApiError.getInternalServerError();

        } catch (IntentException ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - intent exception", devId, "AIID", aiid.toString(),
                    "Exception", ex.toString());
            this.chatLogger.logChatError(LOGFROM, devId, ex, this.telemetryMap);
            return ApiError.getInternalServerError();

        } catch (RequestBase.AiControllerException ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI controller exception", "AIID", aiid.toString(),
                    "Exception", ex.toString());
            this.chatLogger.logChatError(LOGFROM, devId, ex, this.telemetryMap);
            return ApiError.getInternalServerError();

        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat", devId, e);
            this.chatLogger.logChatError(LOGFROM, devId, e, this.telemetryMap);
            return ApiError.getInternalServerError();

        } finally {
            // once we've picked a result, abandon all the others to prevent hanging threads
            this.chatServices.abandonCalls();
        }

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devId, this.telemetryMap);
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devId, "AIID", aiid.toString(), "SessionId", chatId);
        return apiChat.setSuccessStatus();
    }

    public ApiResult assistantChat(UUID aiid, String devId, String question, String chatId,
                                   String history, String topic, float minP) {

        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // Add telemetry for the request
        this.telemetryMap = new LinkedHashMap<String, String>() {
            {
                put("DevId", devId);
                put("AIID", aiid.toString());
                put("Topic", topic);
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                put("ChatId", chatUuid.toString());
                put("History", history);
                put("Q", question);
            }
        };

        ChatResult result = new ChatResult();
        result.setElapsedTime(this.tools.getTimestamp() - startTime);
        result.setQuery(question);

        // Set a fixed response.
        result.setAnswer("Hello");
        result.setScore(1.0);

        // set the chat response time to the whole duration since the start of the request until now
        result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);
        this.telemetryMap.put("RequestDuration", Double.toString(result.getElapsedTime()));

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        apiChat.setResult(result);

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "AssistantChat", devId, this.telemetryMap);
        return apiChat.setSuccessStatus();
    }

    private Pair<UUID, ChatResult> getTopScore(Map<UUID, ChatResult> chatResults) {
        UUID responseFromAi = null;
        ChatResult chatResult = new ChatResult();
        for (Map.Entry<UUID, ChatResult> entry : chatResults.entrySet()) {
            if (entry.getValue().getScore() >= chatResult.getScore()) {
                chatResult = entry.getValue();
                responseFromAi = entry.getKey();
            }
        }
        return new Pair<>(responseFromAi, chatResult);
    }

    private ChatResult interpretSemanticResult() throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitWnet();
        if (allResults == null) {
            return null;
        }
        // Get the top score
        Pair<UUID, ChatResult> result = getTopScore(allResults);
        this.telemetryMap.put("ResponseFromAI", result.getA() == null ? "" : result.getA().toString());

        ChatResult chatResult = result.getB();
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
            this.telemetryMap.put("WNETResponseNULL", "true");
        }

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("WNETAnswer", chatResult.getAnswer());
        this.telemetryMap.put("WNETTopicOut", chatResult.getTopicOut());
        this.telemetryMap.put("WNETElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretAimlResult() throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitAiml();
        if (allResults == null) {
            return null;
        }

        // Get the top score
        Pair<UUID, ChatResult> result = getTopScore(allResults);
        ChatResult chatResult = result.getB();

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("AIMLAnswer", chatResult.getAnswer());
        this.telemetryMap.put("AIMLElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretRnnResult() throws RequestBase.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitRnn();
        if (allResults == null) {
            return null;
        }
        // Get the top score
        Pair<UUID, ChatResult> result = getTopScore(allResults);
        this.telemetryMap.put("ResponseFromAI", result.getA() == null ? "" : result.getA().toString());

        ChatResult chatResult = result.getB();
        if (chatResult.getAnswer() != null) {
            // always reset the conversation if we have gone with a non-wnet result
            chatResult.setResetConversation(true);
            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            this.telemetryMap.put("RNNResponseNULL", "");
        }

        this.logger.logDebug(LOGFROM, String.format("RNN response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
        this.telemetryMap.put("RNNAnswer", chatResult.getAnswer());
        this.telemetryMap.put("RNNTopicOut", chatResult.getTopicOut());

        return chatResult;
    }

    private double toOneDecimalPlace(double input) {
        return Math.round(input * 10.0d) / 10.0d;
    }

    /**
     * Handle any intents.
     * @param chatResult   the current chat result
     * @param aiid         the AI ID
     * @param chatUuid     the Chat ID
     * @param question     the question
     * @param telemetryMap the telemetry map
     */
    private boolean handleIntents(final ChatResult chatResult, final String devId, final UUID aiid, final UUID chatUuid,
                                  final String question, final Map<String, String> telemetryMap)
            throws IntentException {

        // the reply that we are processing is the one to return to the user
        boolean replyConfidence = true;

        // Now that have the chat result, we need to check if there's an intent being returned
        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                devId, aiid, chatUuid, chatResult.getAnswer());
        if (memoryIntent != null // Intent was recognized
                && !memoryIntent.isFulfilled()) {
            telemetryMap.put("IntentRecognized", memoryIntent.getName());
            if (memoryIntent.getUnfulfilledVariables().isEmpty()) {
                notifyIntentFulfilled(chatResult, memoryIntent, devId, aiid, telemetryMap);
            } else {
                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(question,
                        memoryIntent.getVariables());
                if (!entities.isEmpty()) {
                    memoryIntent.fulfillVariables(entities);
                }

                // We've now fulfilled any variables present on the user's question.
                // Need to determine if there are still any unfulfilled variable
                // and prompt for it
                List<MemoryVariable> vars = memoryIntent.getUnfulfilledVariables();
                if (vars.isEmpty()) {
                    notifyIntentFulfilled(chatResult, memoryIntent, devId, aiid, telemetryMap);
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
                                    devId, "AIID", aiid.toString(), "Intent", memoryIntent.getName(),
                                    "Variable", variable.getName());
                            throw new IntentException(
                                    String.format("Entity %s for intent %s does not specify any prompts",
                                            memoryIntent.getName(), variable.getName()));
                        } else {

                            // And prompt the user for the value for that variable
                            int pos = variable.getTimesPrompted() < variable.getPrompts().size()
                                    ? variable.getTimesPrompted()
                                    : 0;
                            chatResult.setAnswer(variable.getPrompts().get(pos));
                            // and decrement the number of prompts
                            variable.setTimesPrompted(variable.getTimesPrompted() + 1);
                            telemetryMap.put("IntentPrompt",
                                    String.format("intent:'%s' variable:'%s' currentPrompt:%d/%d",
                                            memoryIntent.getName(), variable.getName(),
                                            variable.getTimesPrompted(),
                                            variable.getTimesToPrompt()));

                        }
                    } else { // intent not fulfilled but no variables left to handle
                        // if we run out of n_prompts we just stop asking.
                        // the user can still answer the question ... or not
                        telemetryMap.put("IntentNotFulfilled", memoryIntent.getName());
                        replyConfidence = false;
                    }

                }
            }
            this.intentHandler.updateStatus(memoryIntent);
        }

        // Add the current intents state to the chat response
        chatResult.setIntents(this.intentHandler.getCurrentIntentsStateForChat(aiid, chatUuid));
        return replyConfidence;
    }

    private void notifyIntentFulfilled(ChatResult chatResult, MemoryIntent memoryIntent, String devId, UUID aiid,
                                       Map<String, String> telemetryMap) {
        memoryIntent.setIsFulfilled(true);
        ApiIntent intent = this.intentHandler.getIntent(devId, aiid, memoryIntent.getName());
        if (intent != null) {
            List<String> responses = intent.getResponses();
            chatResult.setAnswer(responses.get((int) (Math.random() * responses.size())));
        }
        telemetryMap.put("IntentFulfilled", memoryIntent.getName());

    }

    private ChatResult getImCompletelyLostChatResult(final UUID chatId) {
        ChatResult result = new ChatResult();
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



