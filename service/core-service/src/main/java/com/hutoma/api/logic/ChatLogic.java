package com.hutoma.api.logic;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiChatApiHandover;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatBaseException;
import com.hutoma.api.logic.chat.ChatWorkflow;
import com.hutoma.api.logic.chat.IChatHandler;
import com.hutoma.api.memory.ChatStateHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

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
    private final LogMap telemetryMap;
    private final ChatWorkflow chatWorkflow;
    private ChatState chatState;


    @Inject
    public ChatLogic(final AIChatServices chatServices, final ChatStateHandler chatStateHandler,
                     final Tools tools, final ILogger logger,
                     final ChatLogger chatLogger,
                     final ChatWorkflow chatWorkflow) {
        this.chatStateHandler = chatStateHandler;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.chatLogger = chatLogger;
        this.chatWorkflow = chatWorkflow;

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
        } catch (WebHooks.WebHookInternalException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Chat - webhook internal",
                    devIdString, ex, LogMap.map("AIID", aiid));
            this.chatLogger.logChatError(LOGFROM, devIdString, ex, this.telemetryMap);
            return ApiError.getInternalServerError();
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
        }

        // log the results
        this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, this.telemetryMap);
        this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString, LogMap.map("AIID", aiid)
                .put("SessionId", apiChatResult.getChatId()));

        this.telemetryMap.clear();

        return apiChatResult.setSuccessStatus();
    }

    private ChatResult callChat(final UUID devId, final UUID aiid, final String chatIdString, final String question,
                                Map<String, String> clientVariables)
            throws NoServerAvailableException, ChatBackendConnector.AiControllerException,
            ServerConnector.AiServicesException, ChatBaseException {

        final long requestStartTimestamp = this.tools.getTimestamp();

        UUID chatId = (chatIdString == null || chatIdString.isEmpty())
                ? UUID.randomUUID() : UUID.fromString(chatIdString);
        ChatRequestInfo requestInfo = new ChatRequestInfo(devId, aiid, chatId, question, clientVariables);
        ChatResult currentResult = new ChatResult(question);
        currentResult.setTimestamp(this.tools.getTimestamp());
        boolean chatAnswered = false;

        this.chatState = this.chatStateHandler.getState(devId, aiid, chatId);
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

        if (chatWorkflow.getHandlers().isEmpty()) {
            this.logger.logError(LOGFROM, "No chat handlers defined");
            throw new ChatFailedException(ApiError.getInternalServerError());
        }

        Map<IChatHandler, Long> processedHandlers = new LinkedHashMap<>();
        for (IChatHandler handler : this.chatWorkflow.getHandlers()) {
            final long startHandler = this.tools.getTimestamp();
            currentResult = handler.doWork(requestInfo, currentResult, this.telemetryMap);
            processedHandlers.put(handler, this.tools.getTimestamp() - startHandler);
            if (handler.chatCompleted()) {
                chatAnswered = true;
                break;
            }
        }

        // log which handlers run and their runtime
        LogMap handlersMap = LogMap.map("AIID", aiid);
        for (Map.Entry<IChatHandler, Long> entry: processedHandlers.entrySet()) {
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
        } catch (ChatStateHandler.ChatStateException | ChatFailedException ex) {
            throw ex;
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

    public static class IntentException extends ChatBaseException {
        public IntentException(final String message) {
            super(message);
        }

        public IntentException(final Throwable cause) {
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



