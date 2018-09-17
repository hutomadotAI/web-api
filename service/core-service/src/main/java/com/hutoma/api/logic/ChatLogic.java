package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatBaseException;
import com.hutoma.api.logic.chat.ChatWorkflow;
import com.hutoma.api.logic.chat.IChatHandler;
import com.hutoma.api.memory.ChatStateHandler;
import org.parboiled.common.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chat logic.
 */
public class ChatLogic {

    private static final String LOGFROM = "chatlogic";
    private final Tools tools;
    private final ILogger logger;
    private final AIChatServices chatServices;
    private final ChatLogger chatLogger;
    private final ChatStateHandler chatStateHandler;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final LogMap telemetryMap;
    private final ChatWorkflow chatWorkflow;
    private final Config config;
    private ChatState chatState;
    private final FeatureToggler featureToggler;


    @Inject
    public ChatLogic(final AIChatServices chatServices,
                     final ChatStateHandler chatStateHandler,
                     final DatabaseEntitiesIntents databaseEntitiesIntents,
                     final Tools tools,
                     final ILogger logger,
                     final ChatLogger chatLogger,
                     final ChatWorkflow chatWorkflow,
                     final Config config,
                     final FeatureToggler featureToggler) {
        this.chatStateHandler = chatStateHandler;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.chatLogger = chatLogger;
        this.chatWorkflow = chatWorkflow;
        this.config = config;
        this.featureToggler = featureToggler;

        this.telemetryMap = new LogMap((Map<String, Object>) null);
    }

    private ApiResult doChat(final UUID devId, final UUID aiid, final String question, final String chatIdString,
                             Map<String, String> clientVariables) {

        String devIdString = devId.toString();
        ChatResult currentResult;
        ApiChat apiChatResult;

        try {
            currentResult = callChat(devId, aiid, chatIdString, question, clientVariables);
            apiChatResult = new ApiChat(currentResult.getChatId(), currentResult.getTimestamp());
            // clean up the chat result to remove excessive detail
            apiChatResult.setResult(ChatResult.getUserViewable(currentResult));

        } catch (ChatFailedException ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - failed", devIdString,
                    LogMap.map("Message", ex.getMessage()).put("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ex.getApiError();
        } catch (ChatBackendConnector.AiNotFoundException notFoundException) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not found", devIdString,
                    LogMap.map("Message", notFoundException.getMessage()).put("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, notFoundException, this.telemetryMap);
            return ApiError.getNotFound("Bot not found");
        } catch (AIChatServices.AiNotReadyToChat ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - AI not ready", devIdString, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getBadRequest(
                    "This bot is not ready to chat. It needs to train and/or be linked to other bots");
        } catch (WebHooks.WebHookExternalException ex) {
            return ApiError.getBadRequest("Error in external webhook call");
        } catch (WebHooks.WebHookInternalException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat - webhook internal",
                    devIdString, ex, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getInternalServerError();
        } catch (IntentUserException | ChatStateHandler.ChatStateUserException ex) {
            this.logger.logUserTraceEvent(LOGFROM, "Chat - " + ex.getMessage(),
                    devIdString, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getBadRequest(ex.getMessage());
        } catch (IntentException ex) {
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

            this.telemetryMap.clear();
        }

        // log the chat trace event
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString, LogMap.map("AIID", aiid)
                .put("SessionId", apiChatResult.getChatId()));

        return apiChatResult.setSuccessStatus();
    }

    private ChatResult callChat(final UUID devId, final UUID aiid, final String chatIdString, final String question,
                                Map<String, String> clientVariables)
            throws NoServerAvailableException, ChatBackendConnector.AiControllerException,
            ServerConnector.AiServicesException, ChatBaseException {

        final long requestStartTimestamp = this.tools.getTimestamp();

        UUID chatId;
        if (chatIdString == null || chatIdString.isEmpty()) {
            chatId = UUID.randomUUID();
        } else {
            try {
                chatId = UUID.fromString(chatIdString);
            } catch (IllegalArgumentException ex) {
                throw new ChatFailedException(ApiError.getBadRequest("Chat ID invalid"));
            }
        }

        this.chatState = this.chatStateHandler.getState(devId, aiid, chatId);
        AiIdentity aiIdentity = new AiIdentity(devId, aiid, this.chatState.getAi().getLanguage(),
                ServiceIdentity.DEFAULT_VERSION);
        ChatRequestInfo requestInfo = new ChatRequestInfo(aiIdentity, chatId, question, clientVariables);
        ChatResult currentResult = new ChatResult(question);
        currentResult.setTimestamp(this.tools.getTimestamp());
        boolean chatAnswered = false;

        currentResult.setChatState(this.chatState);
        currentResult.setChatId(chatId);

        // Add telemetry for the request
        this.telemetryMap.add("DevId", devId);
        this.telemetryMap.add("AIID", aiid);
        this.telemetryMap.add("Topic", this.chatState.getTopic());
        this.telemetryMap.add("History", this.chatState.getHistory());
        this.telemetryMap.add("ChatType", "Platform");
        // TODO: potentially PII info, we may need to mask this later, but for
        // development purposes log this
        this.telemetryMap.add("ChatId", chatId);
        this.telemetryMap.add("Q", question);
        this.telemetryMap.add("Chat target", this.chatState.getChatTarget().toString());

        if (this.chatWorkflow.getHandlers().isEmpty()) {
            this.logger.logError(LOGFROM, "No chat handlers defined");
            throw new ChatFailedException(ApiError.getInternalServerError());
        }

        Set<String> executedIntents = new HashSet<>();
        Map<IChatHandler, Long> processedHandlers = new LinkedHashMap<>();
        Iterator<IChatHandler> handlerIterator = this.chatWorkflow.getHandlers().iterator();
        while (handlerIterator.hasNext()) {
            IChatHandler handler = handlerIterator.next();
            final long startHandler = this.tools.getTimestamp();
            currentResult = handler.doWork(requestInfo, currentResult, this.telemetryMap);
            processedHandlers.put(handler, this.tools.getTimestamp() - startHandler);
            if (handler.chatCompleted()) {

                // Check if a handler requested for the workflow to restart
                if (currentResult.getChatState() != null && currentResult.getChatState().isRestartChatWorkflow()) {
                    // Guard against infinite recursion
                    if (!currentResult.getChatState().getCurrentIntents().isEmpty()) {
                        if (executedIntents.contains(
                                currentResult.getChatState().getCurrentIntents().get(0).getName())) {
                            throw new IntentUserException(String.format("Recursion detected for intent %s",
                                    currentResult.getChatState().getCurrentIntents().get(0).getName()));
                        } else {
                            executedIntents.add(currentResult.getChatState().getCurrentIntents().get(0).getName());
                        }
                    }


                    // Go back to the first handler
                    handlerIterator = this.chatWorkflow.getHandlers().iterator();
                } else {
                    chatAnswered = true;
                    break;
                }
            }
        }

        // log which handlers run and their runtime
        LogMap handlersMap = LogMap.map("AIID", aiid);
        for (Map.Entry<IChatHandler, Long> entry : processedHandlers.entrySet()) {
            handlersMap.add(String.format("Handler.%s.runtime", entry.getKey().getClass().getSimpleName()),
                    entry.getValue());
        }
        handlersMap.add("Handler.order",
                processedHandlers.keySet().stream()
                        .map(k -> k.getClass().getSimpleName())
                        .collect(Collectors.joining(", ")));
        this.logger.logDebug(LOGFROM, "Processed chat handlers", handlersMap);

        if (!chatAnswered) {
            this.logger.logError(LOGFROM, "Default chat handler not configured");
            throw new ChatFailedException(ApiError.getInternalServerError());
        }

        this.chatState.setTopic(currentResult.getTopicOut());
        this.chatState.setHistory(currentResult.getHistory());
        this.chatStateHandler.saveState(devId, aiid, currentResult.getChatId(), this.chatState);

        // prepare to send back a result
        currentResult.setScore(Tools.toOneDecimalPlace(currentResult.getScore()));

        long requestDurationMs = this.tools.getTimestamp() - requestStartTimestamp;
        // set the chat response time to the whole duration since the start of the request until now
        currentResult.setElapsedTime(requestDurationMs / 1000.d);
        currentResult.setTimestamp(requestStartTimestamp);
        currentResult.setChatTarget(this.chatState.getChatTarget().getStringValue());


        this.telemetryMap.add("RequestDuration", requestDurationMs);
        this.telemetryMap.add("ResponseSent", currentResult.getAnswer());
        this.telemetryMap.add("Score", currentResult.getScore());
        this.telemetryMap.add("LockedToAi",
                this.chatState.getLockedAiid() == null ? "" : this.chatState.getLockedAiid().toString());

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devId.toString(), this.telemetryMap);

        return currentResult;
    }

    public ApiResult chat(final UUID aiid, final UUID devId, final String question, final String chatId,
                          Map<String, String> clientVariables) {
        this.telemetryMap.add("ChatOrigin", "API/Console");
        return doChat(devId, aiid, question, chatId, clientVariables);
    }

    public ChatResult chatFacebook(final UUID aiid, final UUID devId, final String question, final String chatId,
                                   final String facebookOriginatingUser)
            throws ChatBaseException, ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException,
            NoServerAvailableException {
        this.telemetryMap.add("ChatOrigin", "Facebook");
        this.telemetryMap.add("QFromFacebookUser", facebookOriginatingUser);
        try {
            return callChat(devId, aiid, chatId, question, null);
        } finally {
            this.telemetryMap.clear();
        }
    }

    public ApiResult resetChat(final UUID aiid, final UUID devId, final String chatId) {
        try {
            UUID chatUuid = UUID.fromString(chatId);
            this.chatState = this.chatStateHandler.getState(devId, aiid, chatUuid);
            this.chatStateHandler.clear(devId, aiid, chatUuid, this.chatState);
            return new ApiResult().setSuccessStatus("Chat state cleared");
        } catch (ChatStateHandler.ChatStateException ex) {
            this.chatLogger.logUserExceptionEvent(LOGFROM, "resetchat", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
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
            this.chatStateHandler.saveState(devId, aiid, chatUuid, this.chatState);
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

    public ApiResult setContextVariable(
            final UUID aiid, final UUID devId, final String chatId, Map<String, String> variables) {
        try {
            UUID chatUuid = UUID.fromString(chatId);
            if ((variables != null) && (!variables.isEmpty())) {
                this.chatState = this.chatStateHandler.getState(devId, aiid, chatUuid);
                for (Map.Entry<String, String> v : variables.entrySet()) {
                    if (StringUtils.isEmpty(v.getKey())
                            || (StringUtils.isEmpty(v.getValue()))) {
                        return ApiError.getBadRequest("Invalid variable");
                    } else {
                        this.chatState.getChatContext().setValue(v.getKey(), v.getValue());
                    }
                }
                this.chatStateHandler.saveState(devId, aiid, chatUuid, this.chatState);
                return new ApiResult().setSuccessStatus();
            } else {
                // Empty or null map isnt a valid request
                return ApiError.getBadRequest("Missing variables");
            }
        } catch (Exception ex) {
            this.chatLogger.logUserExceptionEvent(LOGFROM, "setContextVariable", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult triggerIntent(final UUID aiid, final UUID devId, final String chatId, final String intentName) {
        try {
            UUID chatUuid = UUID.fromString(chatId);

            if (!this.databaseEntitiesIntents.checkAIBelongsToDevId(devId, aiid)) {
                return ApiError.getNotFound();
            }
            ApiIntent intent = this.databaseEntitiesIntents.getIntent(aiid, intentName);
            if (intent != null) {
                List<MemoryVariable> variables = new ArrayList<>();
                for (IntentVariable intentVariable : intent.getVariables()) {
                    variables.add(new MemoryVariable(intentVariable));
                }
                MemoryIntent memoryIntent = new MemoryIntent(intentName, aiid, chatUuid, variables, false);

                this.chatState = this.chatStateHandler.getState(devId, aiid, chatUuid);
                this.chatState.setInIntentLoop(true);
                List<MemoryIntent> miList = new ArrayList<>();
                miList.add(memoryIntent);
                this.chatState.setCurrentIntents(miList);
                this.chatStateHandler.saveState(devId, aiid, chatUuid, this.chatState);
                return new ApiResult().setSuccessStatus();
            } else {
                return ApiError.getNotFound();
            }

        } catch (Exception ex) {
            this.chatLogger.logUserExceptionEvent(LOGFROM, "triggerIntent", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    public static class IntentException extends ChatBaseException {
        public IntentException(final String message) {
            super(message);
        }

        public IntentException(final Throwable cause) {
            super(cause);
        }
    }

    public static class IntentUserException extends IntentException {
        public IntentUserException(final String message) {
            super(message);
        }

        public IntentUserException(final Throwable cause) {
            super(cause);
        }
    }

    public static class ChatFailedException extends ChatBaseException {

        private ApiError apiError;

        public ChatFailedException(final ApiError apiError) {
            this.apiError = apiError;
        }

        public ApiError getApiError() {
            return this.apiError;
        }
    }

}



