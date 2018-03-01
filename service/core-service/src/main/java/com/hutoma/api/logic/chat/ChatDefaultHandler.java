package com.hutoma.api.logic.chat;

import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.containers.sub.ChatRequestInfo;

import javax.inject.Inject;

public class ChatDefaultHandler implements IChatHandler {

    public static final String COMPLETELY_LOST_RESULT = "Erm... What?";

    private static final String LOGFROM = "chataimlthandler";
    private final AiStrings aiStrings;
    private final ILogger logger;

    @Inject
    public ChatDefaultHandler(final AiStrings aiStrings, final ILogger logger) {
        this.aiStrings = aiStrings;
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
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
        result.setContext("");
        result.setTopicOut("");
        return result;

    }

    @Override
    public boolean chatCompleted() {
        // This should always complete any chat session as it's the default handler
        return true;
    }
}
