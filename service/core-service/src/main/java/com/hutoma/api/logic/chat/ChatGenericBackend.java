package com.hutoma.api.logic.chat;

import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;

import java.util.Map;
import java.util.UUID;

abstract class ChatGenericBackend {

    ChatResult getTopScore(final ChatState state, final Map<UUID, ChatResult> chatResults, final String question,
                           final double confidenceThreshold) {
        // Check if the currently locked bot still has an acceptable response
        ChatResult chatResult = null;
        UUID lockedAI = state.getLockedAiid();
        if (lockedAI != null && chatResults.containsKey(lockedAI)) {
            ChatResult result = chatResults.get(lockedAI);
            if (result.getScore() >= confidenceThreshold) {
                chatResult = result;
                chatResult.setAiid(lockedAI);
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
        state.setLockedAiid(chatResult.getAiid());
        chatResult.setQuery(question);
        return chatResult;
    }

    static void markQuestionAnswered(final ChatState chatState) {
        chatState.setBadAnswersCount(0);
    }
}
