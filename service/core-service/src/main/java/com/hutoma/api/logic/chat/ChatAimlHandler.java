package com.hutoma.api.logic.chat;

import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ChatAimlHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chataimlthandler";
    private final ILogger logger;
    private boolean isAimlConfident;
    private AIChatServices chatServices;

    @Inject
    public ChatAimlHandler(final ILogger logger) {
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ChatBackendConnector.AiControllerException, ServerConnector.AiServicesException {
        ChatState state = currentResult.getChatState();

        this.chatServices = state.getAiChatServices();

        if (this.chatServices == null) {
            throw new ServerConnector.AiServicesException("No chat services available to retrieve AIML responses");
        }

        // clear the locked AI
        state.setLockedAiid(null);
        // wait for the AIML server to respond
        ChatResult aimlResult = this.interpretAimlResult(state, requestInfo.getQuestion(),
                state.getConfidenceThreshold(), telemetryMap);

        if (aimlResult == null) {
            telemetryMap.add("AIMLAnswered", false);
            return currentResult;
        }

        this.isAimlConfident = false;

        // are we confident enough with this reply?
        this.isAimlConfident = aimlResult.getScore() > JUST_ABOVE_ZERO;
        telemetryMap.add("AIMLScore", aimlResult.getScore());
        telemetryMap.add("AIMLConfident", isAimlConfident);
        if (this.isAimlConfident) {
            telemetryMap.add("AnsweredBy", "AIML");
            telemetryMap.add("AnsweredWithConfidence", true);
        }
        aimlResult.setChatState(state);
        telemetryMap.add("AIMLAnswered", true);
        return aimlResult;
    }

    @Override
    public boolean chatCompleted() {
        return this.isAimlConfident;
    }

    private ChatResult interpretAimlResult(final ChatState state, final String question,
                                           final double confidenceThreshold, final LogMap telemetryMap)
            throws ChatBackendConnector.AiControllerException {

        Map<UUID, ChatResult> allResults = this.chatServices.awaitBackend(BackendServerType.AIML);
        if (allResults == null) {
            return null;
        }

        // Get the top score
        ChatResult chatResult = getTopScore(state, allResults, question, confidenceThreshold);
        UUID aiid = chatResult.getAiid();
        telemetryMap.add("ResponseFromAI", aiid == null ? "" : aiid.toString());

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                Tools.toOneDecimalPlace(chatResult.getElapsedTime()), Tools.toOneDecimalPlace(chatResult.getScore())),
                LogMap.map("AIID", aiid).put("ChatId", chatResult.getChatId()));

        telemetryMap.add("AIMLAnswer", chatResult.getAnswer());
        telemetryMap.add("AIMLElapsedTime", chatResult.getElapsedTime());
        return chatResult;
    }
}
