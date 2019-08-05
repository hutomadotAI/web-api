package com.hutoma.api.logic.chat;

import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;

/**
 * Chat default handler.
 * Terminates the chat pipeline by getting invoked if no previous handler has signalled
 * that the conversation has been handled.
 */
public class ChatDefaultHandler implements IChatHandler {

    public static final String COMPLETELY_LOST_RESULT = "Erm... What?";

    private static final String LOGFROM = "chataimlthandler";
    private final AiStrings aiStrings;
    private final ILogger logger;

    /**
     * Ctor with parameter injection.
     * @param aiStrings the AIStrings
     * @param logger    the logger
     */
    @Inject
    public ChatDefaultHandler(final AiStrings aiStrings, final ILogger logger) {
        this.aiStrings = aiStrings;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap) {

        // Check if we're handing over the conversation based on number of low score responses
        ChatState state = currentResult.getChatState();
        if (state.getAi().getErrorThresholdHandover() > 0) {

            if (state.getBadAnswersCount() >= state.getAi().getErrorThresholdHandover()) {
                state.setChatTarget(ChatHandoverTarget.Other);
                state.setBadAnswersCount(0);
                if (!Tools.isEmpty(state.getAi().getHandoverMessage())) {
                    currentResult.setAnswer(state.getAi().getHandoverMessage());
                } else {
                    currentResult.setAnswer(null);
                }
                if (state.getAi().getHandoverResetTimeoutSeconds() > 0) {
                    // Set the reset timeout datetime
                    state.setHandoverResetTime(DateTime.now(DateTimeZone.UTC)
                            .plusSeconds(state.getAi().getHandoverResetTimeoutSeconds()));
                }
            } else {
                // we're still within the threshold, just increment the counter
                state.setBadAnswersCount(state.getBadAnswersCount() + 1);
                return sendDefaultErrorMessage(requestInfo, telemetryMap);
            }

        } else {
            return sendDefaultErrorMessage(requestInfo, telemetryMap);
        }

        return currentResult;
    }

    /**
     * Sends the default error message to the caller.
     * @param requestInfo  the request info
     * @param telemetryMap the telemetry structure
     * @return the new chat result
     */
    private ChatResult sendDefaultErrorMessage(final ChatRequestInfo requestInfo,
                                               final LogMap telemetryMap) {
        // TODO we need to figure out something
        telemetryMap.add("AnsweredBy", "NONE");
        ChatResult result = new ChatResult(requestInfo.getQuestion());
        result.setChatId(requestInfo.getChatId());
        result.setScore(0.0);
        try {
            result.setAnswer(this.aiStrings.getRandomDefaultChatResponse(
                    requestInfo.getDevId(), requestInfo.getAiid()));
        } catch (AiStrings.AiStringsException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Could not get default chat response",
                    requestInfo.getDevId().toString(), ex);
            result.setAnswer(COMPLETELY_LOST_RESULT);
        }
        result.setTopicOut("");
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean chatCompleted() {
        // This should always complete any chat session as it's the default handler
        return true;
    }
}
