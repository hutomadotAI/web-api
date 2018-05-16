package com.hutoma.api.logic.chat;

import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class ChatIntentHandler implements IChatHandler {

    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentLogic;
    private boolean answered;

    @Inject
    public ChatIntentHandler(final IMemoryIntentHandler intentHandler,
                             final IntentProcessor intentLogic) {
        this.intentHandler = intentHandler;
        this.intentLogic = intentLogic;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws WebHooks.WebHookException, ChatLogic.IntentException {

        UUID aiidForMemoryIntents = currentResult.getChatState().getLockedAiid() == null
                ? requestInfo.getAiid() : currentResult.getChatState().getLockedAiid();
        List<MemoryIntent> intentsForChat = this.intentHandler.getCurrentIntentsStateForChat(
                currentResult.getChatState());

        // For now we should only have one active intent per chat.
        MemoryIntent currentIntent = intentsForChat.isEmpty() ? null : intentsForChat.get(0);

        if (this.intentLogic.processIntent(requestInfo, aiidForMemoryIntents, currentIntent, currentResult,
                telemetryMap)) {
            // Intent was handled, confidence is high
            currentResult.setScore(1.0d);
            currentResult.setIntents(Collections.singletonList(currentIntent));
            telemetryMap.add("AnsweredBy", "IntentProcessor");
            this.answered = true;
            return currentResult;
        }

        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return this.answered;
    }
}
