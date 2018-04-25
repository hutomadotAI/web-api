package com.hutoma.api.logic.chat;

import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.LogMap;

import javax.inject.Inject;

public class ChatHandoverHandler implements IChatHandler {

    private final Tools tools;
    private boolean isHandedOver;

    @Inject
    public ChatHandoverHandler(final Tools tools) {
        this.tools = tools;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap) {

        ChatState state = currentResult.getChatState();
        if (state.getChatTarget() != ChatHandoverTarget.Ai) {
            // Handover flag is set

            // Now check if we have a handover timeout
            if (state.getResetHandoverTime() != null) {

                // If we've timed out, then reset the chat target and let the AI respond
                if (state.getResetHandoverTime().getMillis() < this.tools.getTimestamp()) {
                    state.setChatTarget(ChatHandoverTarget.Ai);
                    state.setHandoverResetTime(null);
                    this.isHandedOver = false;
                    return currentResult;
                }
            }

            this.isHandedOver = true;
            return getHandoverEmptyMessage(requestInfo, currentResult);

        } else {
            this.isHandedOver = false;
        }
        return currentResult;
    }

    private static ChatResult getHandoverEmptyMessage(final ChatRequestInfo requestInfo,
                                         final ChatResult currentResult) {
        currentResult.setQuery(requestInfo.getQuestion());
        currentResult.setScore(1.0);
        currentResult.setChatTarget(currentResult.getChatState().getChatTarget().getStringValue());
        currentResult.setAnswer(null);
        currentResult.setHistory(null);
        currentResult.setTopicOut(null);
        currentResult.setContext(null);
        currentResult.setTopicIn(null);
        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return this.isHandedOver;
    }
}
