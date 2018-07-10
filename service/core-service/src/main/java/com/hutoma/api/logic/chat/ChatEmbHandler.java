package com.hutoma.api.logic.chat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ChatEmbHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chatembhandler";
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentLogic;
    private AIChatServices chatServices;
    private boolean embConfident;

    @VisibleForTesting
    @Inject
    public ChatEmbHandler(final IMemoryIntentHandler intentHandler,
                          final IntentProcessor intentLogic,
                          final ILogger logger) {
        this.intentHandler = intentHandler;
        this.intentLogic = intentLogic;
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException,
            ChatLogic.IntentException, WebHooks.WebHookException {
        ChatState state = currentResult.getChatState();
        this.chatServices = state.getAiChatServices();

        if (this.chatServices == null) {
            throw new ServerConnector.AiServicesException("No chat services available to retrieve EMB responses");
        }

        ChatResult result = getTopResult(state, requestInfo, telemetryMap);
        if (result != null) {

            double minP = chatServices.getMinPMap().getOrDefault(result.getAiid(), 0.0);
            if (!chatServices.getMinPMap().containsKey(result.getAiid())) {
                this.logger.logWarning(LOGFROM, String.format(
                        "Could not obtain minP for AIID %s, defaulting to 0.0", result.getAiid()));
            }
            this.embConfident = (result.getScore() >= minP && (result.getScore() > JUST_ABOVE_ZERO));

            telemetryMap.add("EMB.response", result.getAnswer());
            telemetryMap.add("EMB.confidence", result.getScore());
            telemetryMap.add("EMB.elapsed", result.getElapsedTime());
            telemetryMap.add("EMB.answered", true);

            if (this.embConfident) {
                if (processIntents(requestInfo, result, state, telemetryMap)) {
                    telemetryMap.add("IntentRecognized", true);
                    currentResult.setAnswer(result.getAnswer());
                    currentResult.setIntents(result.getIntents());
                    currentResult.setScore(result.getScore());
                    if (!Strings.isNullOrEmpty(result.getPromptForIntentVariable())) {
                        currentResult.setPromptForIntentVariable(result.getPromptForIntentVariable());
                    }
                } else {
                    currentResult.setAnswer(result.getAnswer());
                    currentResult.setScore(result.getScore());
                }
                // Not really used by EMB byt to maintain compat with WNET
                currentResult.setHistory(result.getHistory());

                telemetryMap.add("AnsweredBy", "EMB");
                markQuestionAnswered(state);
            }

        } else {
            telemetryMap.add("EMB.answered", false);
        }

        return currentResult;
    }

    private boolean processIntents(final ChatRequestInfo requestInfo,
                                   final ChatResult result,
                                   final ChatState state,
                                   final LogMap telemetryMap)
            throws ChatLogic.IntentException, WebHooks.WebHookException {

        UUID aiidFromResult = result.getAiid();
        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                requestInfo.getDevId(), aiidFromResult, requestInfo.getChatId(), result.getAnswer(), state);
        if (memoryIntent != null // Intent was recognized
                && !memoryIntent.isFulfilled()) {

            telemetryMap.add("IntentRecognized", true);

            result.setChatState(state);
            if (this.intentLogic.processIntent(requestInfo, aiidFromResult, memoryIntent, result, telemetryMap)) {
                telemetryMap.add("AnsweredBy", "EMB");

                // Expand entity variables if intent is handled
                extractContextVariables(result);

                markQuestionAnswered(state);
            } else {
                // if intents processing returns false then we need to ignore EMB
                this.embConfident = false;
            }

            return true;
        }

        return false;
    }

    public void extractContextVariables(ChatResult result) {
        if (result.getContext() != null) {
            if (!result.getContext().isEmpty()) {
                String response = result.getAnswer();
                for (Map.Entry<String, String> value : result.getContext().entrySet()) {
                    response = response.replace(String.format("$%s", value.getKey()), value.getValue());
                }
                result.setAnswer(response);
            }
        }
    }

    private ChatResult getTopResult(final ChatState state,
                                    final ChatRequestInfo requestInfo,
                                    final LogMap telemetryMap)
            throws ChatBackendConnector.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitBackend(BackendServerType.EMB);
        if (allResults == null) {
            return null;
        }
        // Get the top score
        ChatResult chatResult = getTopScore(state, allResults, requestInfo.getQuestion(),
                state.getConfidenceThreshold());
        UUID aiid = chatResult.getAiid();
        telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        if (chatResult.getAnswer() != null) {
            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            telemetryMap.add("EMBResponseNULL", "true");
        }

        return chatResult;

    }

    @Override
    public boolean chatCompleted() {
        return this.embConfident;
    }
}

