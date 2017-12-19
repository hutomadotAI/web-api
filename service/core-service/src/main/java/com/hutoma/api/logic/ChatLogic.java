package com.hutoma.api.logic;

import com.google.common.base.Strings;
import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiChatApiHandover;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.ChatStateHandler;
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
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    public static final String COMPLETELY_LOST_RESULT = "Erm... What?";

    private static final String LOGFROM = "chatlogic";
    private static final double JUST_ABOVE_ZERO = 0.00001d;
    private static final String SYSANY = "sys.any";
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
    private final AiStrings aiStrings;
    private LogMap telemetryMap;
    private ChatState chatState;

    @Inject
    public ChatLogic(final Config config, final JsonSerializer jsonSerializer, final AIChatServices chatServices,
                     final Tools tools, final ILogger logger, final IMemoryIntentHandler intentHandler,
                     final IEntityRecognizer entityRecognizer, final ChatLogger chatLogger, final WebHooks webHooks,
                     final ChatStateHandler chatStateHandler, final AiStrings aiStrings) {
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
        this.aiStrings = aiStrings;
    }

    public ApiResult chat(final UUID aiid, final UUID devId, final String question, final String chatId,
                          Map<String, String> clientVariables) {
        try {
            this.telemetryMap = LogMap.map("ChatOrigin", "API/Console");
            String passthrough = this.chatServices.getAIPassthroughUrl(devId, aiid);
            if (passthrough != null) {
                return chatPassthrough(aiid, devId, chatId, question, clientVariables, passthrough).setSuccessStatus();
            } else {
                return chatCall(aiid, devId, question, chatId, clientVariables).setSuccessStatus();
            }
        } catch (ChatFailedException fail) {
            return fail.getApiError();
        } catch (ChatStateHandler.ChatStateUserException ex) {
            return ApiError.getBadRequest(ex.getMessage());
        } catch (ChatStateHandler.ChatStateException ex) {
            return ApiError.getBadRequest();
        }
    }

    public ApiChat chatPassthrough(final UUID aiid, final UUID devId, final String chatId, final String question,
                                   final Map<String, String> clientVariables, String passthrough)
            throws ChatFailedException, ChatStateHandler.ChatStateException {
        UUID chatUuid = UUID.fromString(chatId);
        final String devIdString = devId.toString();

        ChatResult chatResult = new ChatResult(question);
        final ChatRequestInfo chatInfo = new ChatRequestInfo(devId, aiid, chatUuid, question, clientVariables);
        final long startTime = this.tools.getTimestamp();

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        // Set the timestamp of the request
        apiChat.setTimestamp(startTime);

        // Add telemetry for the request
        this.telemetryMap = this.telemetryMap.put("DevId", devIdString)
                .put("AIID", aiid)
                .put("ChatId", chatUuid)
                .put("Q", question)
                .put("ChatType", "Passthrough");


        try {
            WebHookResponse response = this.webHooks.executePassthroughWebhook(passthrough, chatResult, chatInfo);

            if (response != null) {
                chatResult.setAnswer(response.getText());
            }
        } catch (WebHooks.WebHookExternalException callException) {
            this.chatLogger.logChatError(LOGFROM, devId.toString(), callException, this.telemetryMap);
            throw new ChatFailedException(ApiError.getBadRequest());
        } catch (WebHooks.WebHookException webhookException) {
            this.logger.logUserErrorEvent(LOGFROM,
                    "Error occurred executing WebHook for passthrough",
                    chatInfo.devId.toString(),
                    LogMap.map("AIID", aiid)
                            .put("Error", webhookException.getMessage()));
            this.chatLogger.logChatError(LOGFROM, devId.toString(), webhookException, this.telemetryMap);
            throw new ChatFailedException(ApiError.getInternalServerError());
        }

        // set the chat response time to the whole duration since the start of the request until now
        chatResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);
        apiChat.setResult(chatResult);

        this.telemetryMap.add("RequestDuration", chatResult.getElapsedTime());
        this.telemetryMap.add("ResponseSent", chatResult.getAnswer());
        this.telemetryMap.add("Score", chatResult.getScore());

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, this.telemetryMap);
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString, LogMap.map("AIID", aiid).put("SessionId", chatId));

        return apiChat;
    }

    public ChatResult chatFacebook(final UUID aiid, final UUID devId, final String question, final String chatId,
                                   final String facebookOriginatingUser)
            throws ChatFailedException, ChatStateHandler.ChatStateException {
        this.telemetryMap = LogMap.map("ChatOrigin", "Facebook")
                .put("QFromFacebookUser", facebookOriginatingUser);
        return chatCall(aiid, devId, question, chatId, null).getResult();
    }

    public ApiResult assistantChat(UUID aiid, UUID devId, String question, String chatId) {
        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // Add telemetry for the request
        this.telemetryMap = LogMap.map("DevId", devId)
                .put("AIID", aiid)
                .put("ChatType", "Assistant")
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                .put("ChatId", chatUuid.toString())
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

    public ApiResult handOver(final UUID aiid, final UUID devId, final String chatId, final ChatHandoverTarget target) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            UUID chatUuid = UUID.fromString(chatId);
            this.chatState = this.chatStateHandler.getState(devId, aiid, chatUuid);
            ChatHandoverTarget initialTarget = this.chatState.getChatTarget();
            if (initialTarget == target) {
                this.chatLogger.logUserWarnEvent(LOGFROM, "Handover already set to target", devId.toString(),
                        logMap.put("Current target", target.toString()));
                return ApiError.getBadRequest(String.format("Chat target already set to %s", target.getStringValue()));
            }
            this.chatState.setChatTarget(target);
            this.chatStateHandler.saveState(devId, aiid,chatUuid, this.chatState);
            this.chatLogger.logUserInfoEvent(LOGFROM, "Handover", devId.toString(),
                    logMap.put("Previous target", initialTarget.toString())
                        .put("Current target", target.toString()));
            return new ApiChatApiHandover(chatUuid, target).setSuccessStatus(
                    String.format("Handed over to %s.", target.toString()));
        } catch (ChatStateHandler.ChatStateUserException ex) {
            this.chatLogger.logUserExceptionEvent(LOGFROM, "handover", devId.toString(), ex);
            return ApiError.getBadRequest(ex.getMessage());
        } catch (Exception ex) {
            this.chatLogger.logUserExceptionEvent(LOGFROM, "handover", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    private ApiChat chatCall(final UUID aiid, final UUID devId, final String question, final String chatId,
                             final Map<String, String> clientVariables)
            throws ChatFailedException, ChatStateHandler.ChatStateException {

        final ChatRequestInfo chatInfo = new ChatRequestInfo(devId, aiid, UUID.fromString(chatId),
                question, clientVariables);

        final String devIdString = devId.toString();
        final long startTime = this.tools.getTimestamp();
        final UUID chatUuid = chatInfo.chatId;

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        // Set the timestamp of the request
        apiChat.setTimestamp(startTime);

        this.chatState = this.chatStateHandler.getState(devId, aiid, chatUuid);

        // Add telemetry for the request
        this.telemetryMap = this.telemetryMap.put("DevId", devId)
                .put("AIID", aiid)
                .put("Topic", this.chatState.getTopic())
                .put("History", this.chatState.getHistory())
                .put("ChatType", "Platform")
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                .put("ChatId", chatUuid)
                .put("Q", question)
                .put("Chat target", this.chatState.getChatTarget().toString());


        if (this.chatState.getChatTarget() != ChatHandoverTarget.Ai) {
            ChatResult result = new ChatResult(chatUuid, 1.0, question, "",
                    this.tools.getTimestamp() - startTime, null);
            result.setChatTarget(this.chatState.getChatTarget().getStringValue());
            result.setAnswer(null);
            result.setHistory(null);
            result.setTopicOut(null);
            result.setContext(null);
            result.setTopicIn(null);
            result.setElapsedTime(this.tools.getTimestamp() - startTime);
            apiChat.setResult(result);
            this.telemetryMap.add("RequestDuration", result.getElapsedTime());
        } else {
            processChatRequest(devId, aiid, chatUuid, chatInfo, question, apiChat);
        }

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, this.telemetryMap);
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString, LogMap.map("AIID", aiid).put("SessionId", chatId));
        return apiChat;
    }

    private void processChatRequest(final UUID devId, final UUID aiid, final UUID chatUuid,
                                    final ChatRequestInfo chatInfo, final String question, final ApiChat apiChat)
            throws ChatFailedException, ChatStateHandler.ChatStateException {
        UUID aiidForMemoryIntents = this.chatState.getLockedAiid() == null ? aiid : this.chatState.getLockedAiid();
        List<MemoryIntent> intentsForChat = this.intentHandler.getCurrentIntentsStateForChat(
                aiidForMemoryIntents, chatUuid);

        // For now we should only have one active intent per chat.
        MemoryIntent currentIntent = intentsForChat.isEmpty() ? null : intentsForChat.get(0);

        ChatResult result = new ChatResult(question);
        final String devIdString = devId.toString();
        double minP = 0.0;
        try {

            // Check if we're in the middle of an intent flow and process it.
            if (processIntent(chatInfo, aiidForMemoryIntents, currentIntent, result)) {
                // Intent was handled, confidence is high
                result.setScore(1.0d);
                this.telemetryMap.add("AnsweredBy", "IntentProcessor");
            } else {
                // Otherwise just go through the regular chat flow

                // async start requests to all servers
                this.chatServices.startChatRequests(devId, aiid, chatUuid, question,
                        this.chatState);

                // wait for WNET to return
                result = this.interpretSemanticResult(question, this.chatState.getConfidenceThreshold());

                boolean wnetConfident = false;
                if (result != null) {
                    // are we confident enough with this reply?
                    minP = this.chatServices.getMinPMap().getOrDefault(result.getAiid(), 0.0);
                    if (!this.chatServices.getMinPMap().containsKey(result.getAiid())) {
                        this.logger.logWarning(LOGFROM, String.format(
                                "Could not obtain minP for AIID %s, defaulting to 0.0", result.getAiid()));
                    }
                    wnetConfident = (result.getScore() >= minP && (result.getScore() > JUST_ABOVE_ZERO));
                    this.telemetryMap.add("WNETScore", result.getScore());
                    this.telemetryMap.add("WNETConfident", wnetConfident);

                    if (wnetConfident) {
                        // if we are taking WNET's reply then process intents
                        UUID aiidFromResult = result.getAiid();
                        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                                devId, aiidFromResult, chatUuid, result.getAnswer());
                        if (memoryIntent != null // Intent was recognized
                                && !memoryIntent.isFulfilled()) {

                            // for error logging purposes
                            currentIntent = memoryIntent;

                            this.telemetryMap.add("IntentRecognized", true);

                            if (processIntent(chatInfo, aiidFromResult, memoryIntent, result)) {
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
                    // clear the locked AI
                    this.chatState.setLockedAiid(null);
                    // wait for the AIML server to respond
                    ChatResult aimlResult = this.interpretAimlResult(question, this.chatState.getConfidenceThreshold());

                    boolean aimlConfident = false;
                    // If we don't have AIML available (not linked)
                    if (aimlResult != null) {
                        // are we confident enough with this reply?
                        aimlConfident = aimlResult.getScore() > JUST_ABOVE_ZERO;
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
                        // clear the locked AI
                        this.chatState.setLockedAiid(null);

                        // get a response from the RNN
                        ChatResult rnnResult = null;

                        // if the RNN times out, treat it as if it returned an empty response
                        // i.e. carry on and take the best answer that we have
                        try {
                            rnnResult = this.interpretRnnResult(question,
                                    this.chatState.getConfidenceThreshold());
                        } catch (ChatBackendConnector.AiControllerTimeoutException ex) {
                            // log the timeout if it happens
                            this.telemetryMap.add("RnnTimeout", ex.getMessage());
                        }

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
                            // Fallback to AIML only if we have an answer available (regardless of whether
                            // it's confidence is above minP or not) and there's actually a response
                            if (aimlResult != null && aimlResult.getScore() > JUST_ABOVE_ZERO
                                    && !aimlResult.getAnswer().isEmpty()) {
                                this.telemetryMap.add("FellBackToAIML", "true");
                                this.telemetryMap.add("AnsweredBy", "AIML");
                                result = aimlResult;
                            } else {
                                // TODO we need to figure out something
                                this.telemetryMap.add("AnsweredBy", "NONE");
                                result = getImCompletelyLostChatResult(devId, aiid, chatUuid, question);
                            }
                        }

                        this.telemetryMap.add("RNNAnswered", rnnResult != null);
                    }
                }
            }


        } catch (ChatBackendConnector.AiNotFoundException notFoundException) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not found", devIdString,
                    LogMap.map("Message", notFoundException.getMessage()).put("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, notFoundException, this.telemetryMap);
            throw new ChatFailedException(ApiError.getNotFound("Bot not found"));

        } catch (AIChatServices.AiNotReadyToChat ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not ready", devIdString, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            throw new ChatFailedException(ApiError.getBadRequest(
                    "This bot is not ready to chat. It needs to train and/or be linked to other bots"));

        } catch (WebHooks.WebHookExternalException ex) {
            // if the webhook call fails, log it as a warning. The default chat response will be sent to the user.
            String webHookErrorString = ex.getMessage();
            this.logger.logUserWarnEvent(LOGFROM,
                    "Call to WebHook failed for intent",
                    chatInfo.devId.toString(),
                    LogMap.map("Intent", (currentIntent == null)
                            ? "unknown" : currentIntent.getName())
                            .put("AIID", aiidForMemoryIntents)
                            .put("Error", webHookErrorString));
            this.telemetryMap.add("webHookCallFailure", webHookErrorString);

        } catch (IntentException | ChatBackendConnector.AiControllerException | ServerConnector.AiServicesException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat - " + ex.getClass().getSimpleName(),
                    devIdString, ex, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            throw new ChatFailedException(ApiError.getInternalServerError());
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat", devIdString, e);
            this.chatLogger.logChatError(LOGFROM, devIdString, e, this.telemetryMap);
            throw new ChatFailedException(ApiError.getInternalServerError());
        } finally {
            // once we've picked a result, abandon all the others to prevent hanging threads
            this.chatServices.abandonCalls();
        }

        // prepare to send back a result
        result.setScore(toOneDecimalPlace(result.getScore()));

        // set the chat response time to the whole duration since the start of the request until now
        result.setElapsedTime((this.tools.getTimestamp() - apiChat.getTimestamp()) / 1000.d);
        apiChat.setResult(result);
        result.setChatTarget(this.chatState.getChatTarget().getStringValue());

        this.chatState.setTopic(apiChat.getResult().getTopicOut());
        this.chatState.setHistory(apiChat.getResult().getHistory());
        this.chatStateHandler.saveState(devId, aiid, chatUuid, this.chatState);

        this.telemetryMap.add("MinP", minP);
        this.telemetryMap.add("RequestDuration", result.getElapsedTime());
        this.telemetryMap.add("ResponseSent", result.getAnswer());
        this.telemetryMap.add("Score", result.getScore());
        this.telemetryMap.add("LockedToAi",
                this.chatState.getLockedAiid() == null ? "" : this.chatState.getLockedAiid().toString());
    }

    /**
     * Processes a given intent.
     * @param chatInfo             the chat request info
     * @param aiidForMemoryIntents the aiid for memory intents
     * @param currentIntent        the intent to process
     * @param chatResult           current chat result
     * @return whether there was an intent to process or not
     * @throws IntentException
     */
    private boolean processIntent(final ChatRequestInfo chatInfo, final UUID aiidForMemoryIntents,
                                  final MemoryIntent currentIntent, final ChatResult chatResult)
            throws IntentException, WebHooks.WebHookException {

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

                this.telemetryMap.add("EntityRequested.Name", mv.getName());
                this.telemetryMap.add("EntityRequested.Label", mv.getLabel());
                chatResult.setScore(1.0d);

                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(chatInfo.question,
                        currentIntent.getVariables());
                // Did the recognizer find something for this entity?
                Optional<Pair<String, String>> entityValue = entities.stream()
                        .filter(x -> x.getA().equals(mv.getName())).findFirst();
                if (entityValue.isPresent() || mv.getName().equals(SYSANY)) {
                    handledIntent = processVariables(chatInfo, aiidForMemoryIntents, currentIntent, chatResult,
                            Collections.singletonList(mv), intentsToClear, intentLog);

                } else {
                    // If we have prompted enough, then give up
                    if (mv.getTimesPrompted() >= mv.getTimesToPrompt()) {
                        mv.setRequested(false);
                        handledIntent = false;
                        // clear the intent whenever a mandatory variable is not set within the allowed number of prompts
                        intentsToClear.add(currentIntent);
                    } else {
                        promptForVariable(mv, chatResult, intentLog);
                        handledIntent = true;
                    }
                }

            } else {

                // Populate persistent entities.
                for (MemoryVariable variable : currentIntent.getVariables()) {
                    String persistentValue = this.chatState.getEntityValue(variable.getName());
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
                            currentIntent.getVariables(), intentsToClear, intentLog);

                }
            }

        } catch (WebHooks.WebHookException ex) {
            // If we get a webhook exception it means the variables have been processed and we have fulfilled
            // the intent, thus triggering the webhook. So for now clear the variables so that the intent can be
            // fully processed again.
            this.intentHandler.clearIntents(Collections.singletonList(currentIntent));
            // rethrow for bubbling up
            throw ex;
        }

        chatResult.setIntents(Collections.singletonList(currentIntent));

        if (!intentsToClear.contains(currentIntent)) {
            this.intentHandler.updateStatus(currentIntent);
        }

        // Clear fulfilled intents or intents which have exhausted their prompts, so they can be triggered again
        if (!intentsToClear.isEmpty()) {
            this.intentHandler.clearIntents(intentsToClear);
        }

        intentLog.put("Handled", handledIntent);
        this.telemetryMap.add("Intent", intentLog);

        return handledIntent;
    }

    private boolean processVariables(final ChatRequestInfo chatInfo, final UUID aiidForMemoryIntents,
                                     final MemoryIntent currentIntent,
                                     final ChatResult chatResult, final List<MemoryVariable> memoryVariables,
                                     final List<MemoryIntent> intentsToClear, final Map<String, Object> log)
            throws IntentException, WebHooks.WebHookException {

        boolean handledIntent = false;

        // At this stage we're guaranteed to have variables with different entity types
        // Attempt to retrieve entities from the question
        List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(
                chatInfo.question, memoryVariables);

        if (!entities.isEmpty()) {
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
                this.chatState.setEntityValue(memoryVariable.getName(), memoryVariable.getCurrentValue());
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
                    variable.setCurrentValue(chatInfo.question);
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
                                chatInfo.devId.toString(),
                                LogMap.map("AIID", aiidForMemoryIntents)
                                        .put("Intent", currentIntent.getName())
                                        .put("Variable", variable.getName()));
                        throw new IntentException(
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
                this.telemetryMap.add("IntentNotFulfilled", currentIntent.getName());
                intentsToClear.add(currentIntent);
                allVariablesFilled = false;
            }
        }

        if (allVariablesFilled) {
            notifyIntentFulfilled(chatResult, currentIntent, aiidForMemoryIntents);
            checkAndExecuteWebhook(chatInfo, aiidForMemoryIntents, currentIntent, chatResult, log);
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

    private void checkAndExecuteWebhook(final ChatRequestInfo chatInfo, final UUID aiidForMemoryIntents,
                                        final MemoryIntent currentIntent,
                                        final ChatResult chatResult, final Map<String, Object> log)
            throws WebHooks.WebHookException {
        // If the webhook returns a text response, overwrite the answer.
        WebHook webHook = this.webHooks.getWebHookForIntent(currentIntent, chatInfo.devId);
        if (webHook != null && webHook.isEnabled()) {
            log.put("Webhook run", true);
            WebHookResponse response = this.webHooks.executeIntentWebHook(webHook, currentIntent, chatResult,
                    chatInfo);

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
                        chatInfo.devId.toString(),
                        LogMap.map("Intent", currentIntent.getName()).put("AIID", aiidForMemoryIntents));
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

    private ChatResult getTopScore(final Map<UUID, ChatResult> chatResults, final String question,
                                   final double confidenceThreshold) {
        // Check if the currently locked bot still has an acceptable response
        ChatResult chatResult = null;
        if (this.chatState.getLockedAiid() != null && chatResults.containsKey(this.chatState.getLockedAiid())) {
            ChatResult result = chatResults.get(this.chatState.getLockedAiid());
            if (result.getScore() >= confidenceThreshold) {
                chatResult = result;
                chatResult.setAiid(this.chatState.getLockedAiid());
            }
        }

        if (chatResult == null) {
            for (Map.Entry<UUID, ChatResult> entry : chatResults.entrySet()) {
                if (chatResult == null || entry.getValue().getScore() >= chatResult.getScore()) {
                    chatResult = entry.getValue();
                    chatResult.setAiid(entry.getKey());
                }
            }
        }

        if (chatResult == null) {
            chatResult = new ChatResult(question);
        }

        // lock to this AI
        this.chatState.setLockedAiid(chatResult.getAiid());
        chatResult.setQuery(question);
        return chatResult;
    }

    private ChatResult interpretSemanticResult(final String question, final double confidenceThreshold)
            throws ChatBackendConnector.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitWnet();
        if (allResults == null) {
            return null;
        }
        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question, confidenceThreshold);
        UUID aiid = chatResult.getAiid();
        this.telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        if (chatResult.getAnswer() != null) {
            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            this.telemetryMap.add("WNETResponseNULL", "true");
        }

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())),
                LogMap.map("AIID", aiid).put("ChatId", chatResult.getChatId()));

        this.telemetryMap.add("WNETAnswer", chatResult.getAnswer());
        this.telemetryMap.add("WNETTopicOut", chatResult.getTopicOut());
        this.telemetryMap.add("WNETElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }

    private ChatResult interpretAimlResult(final String question, final double confidenceThreshold)
            throws ChatBackendConnector.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitAiml();
        if (allResults == null) {
            return null;
        }

        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question, confidenceThreshold);
        UUID aiid = chatResult.getAiid();
        this.telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())),
                LogMap.map("AIID", aiid).put("ChatId", chatResult.getChatId()));

        this.telemetryMap.add("AIMLAnswer", chatResult.getAnswer());
        this.telemetryMap.add("AIMLElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }

    private ChatResult interpretRnnResult(final String question, final double confidenceThreshold)
            throws ChatBackendConnector.AiControllerException {

        if (!this.config.isRnnEnabled()) {
            return null;
        }

        Map<UUID, ChatResult> allResults = this.chatServices.awaitRnn();
        if (allResults == null) {
            return null;
        }

        // Get the top score
        ChatResult chatResult = getTopScore(allResults, question, confidenceThreshold);
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
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())),
                LogMap.map("AIID", aiid).put("ChatId", chatResult.getChatId()));

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

    private ChatResult getImCompletelyLostChatResult(final UUID devId, final UUID aiid, final UUID chatId,
                                                     final String question) {
        ChatResult result = new ChatResult(question);
        result.setChatId(chatId);
        result.setScore(0.0);
        try {
            result.setAnswer(this.aiStrings.getRandomDefaultChatResponse(devId, aiid));
        } catch (AiStrings.AiStringsException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Could not get default chat response",
                    devId.toString(), ex);
            result.setAnswer(COMPLETELY_LOST_RESULT);
        }
        result.setContext("");
        result.setTopicOut("");
        return result;
    }

    static class IntentException extends Exception {
        public IntentException(final String message) {
            super(message);
        }
    }

    public static class ChatFailedException extends Exception {

        private ApiError apiError;

        public ChatFailedException(final ApiError apiError) {
            this.apiError = apiError;
        }

        public ApiError getApiError() {
            return this.apiError;
        }
    }

}



