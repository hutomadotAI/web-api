package com.hutoma.api.logic.chat;

import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ChatWnetHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chatwnethandler";
    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentLogic;
    private final ILogger logger;
    private boolean wnetConfident;
    private AIChatServices chatServices;

    @Inject
    public ChatWnetHandler(final IMemoryIntentHandler intentHandler,
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
            throws ChatBackendConnector.AiControllerException, ChatLogic.IntentException,
            WebHooks.WebHookException, ServerConnector.AiServicesException {
        ChatState state = currentResult.getChatState();
        this.chatServices = state.getAiChatServices();

        if (this.chatServices == null) {
            throw new ServerConnector.AiServicesException("No chat services available to retrieve WNET responses");
        }

        // wait for WNET to return
        ChatResult result = this.interpretSemanticResult(state, requestInfo.getQuestion(),
                state.getConfidenceThreshold(), telemetryMap);
        if (result == null) {
            telemetryMap.add("WNETAnswered", false);
            return currentResult;
        }
        result.setChatState(state);
        result.setChatId(currentResult.getChatId());

        this.wnetConfident = false;
        // are we confident enough with this reply?
        double minP = chatServices.getMinPMap().getOrDefault(result.getAiid(), 0.0);
        if (!chatServices.getMinPMap().containsKey(result.getAiid())) {
            this.logger.logWarning(LOGFROM, String.format(
                    "Could not obtain minP for AIID %s, defaulting to 0.0", result.getAiid()));
        }
        this.wnetConfident = (result.getScore() >= minP && (result.getScore() > JUST_ABOVE_ZERO));
        telemetryMap.add("WNETScore", result.getScore());
        telemetryMap.add("WNETConfident", wnetConfident);

        if (this.wnetConfident) {
            // if we are taking WNET's reply then process intents
            UUID aiidFromResult = result.getAiid();
            MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                    requestInfo.getDevId(), aiidFromResult, requestInfo.getChatId(), result.getAnswer());
            if (memoryIntent != null // Intent was recognized
                    && !memoryIntent.isFulfilled()) {

                telemetryMap.add("IntentRecognized", true);

                if (this.intentLogic.processIntent(requestInfo, aiidFromResult, memoryIntent, result, telemetryMap)) {
                    telemetryMap.add("AnsweredBy", "WNET");
                } else {
                    // if intents processing returns false then we need to ignore WNET
                    this.wnetConfident = false;
                }
            } else {
                telemetryMap.add("AnsweredBy", "WNET");
            }
        }

        telemetryMap.add("AnsweredWithConfidence", wnetConfident);
        telemetryMap.add("WNETAnswered", true);

        return result;
    }

    @Override
    public boolean chatCompleted() {
        return this.wnetConfident;
    }

    private ChatResult interpretSemanticResult(final ChatState state, final String question,
                                               final double confidenceThreshold,
                                               final LogMap telemetryMap)
            throws ChatBackendConnector.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitWnet();
        if (allResults == null) {
            return null;
        }
        // Get the top score
        ChatResult chatResult = getTopScore(state, allResults, question, confidenceThreshold);
        UUID aiid = chatResult.getAiid();
        telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        if (chatResult.getAnswer() != null) {
            // remove trailing newline
            chatResult.setAnswer(chatResult.getAnswer().trim());
        } else {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            telemetryMap.add("WNETResponseNULL", "true");
        }

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                Tools.toOneDecimalPlace(chatResult.getElapsedTime()),
                Tools.toOneDecimalPlace(chatResult.getScore())),
                LogMap.map("AIID", aiid).put("ChatId", chatResult.getChatId()));

        telemetryMap.add("WNETAnswer", chatResult.getAnswer());
        telemetryMap.add("WNETTopicOut", chatResult.getTopicOut());
        telemetryMap.add("WNETElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }
}
