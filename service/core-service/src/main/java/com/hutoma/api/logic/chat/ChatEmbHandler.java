package com.hutoma.api.logic.chat;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

public class ChatEmbHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chatembhandler";
    private final ILogger logger;
    private AIChatServices chatServices;

    @Inject
    public ChatEmbHandler(final ILogger logger) {
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException {
        ChatState state = currentResult.getChatState();
        this.chatServices = state.getAiChatServices();

        if (this.chatServices == null) {
            throw new ServerConnector.AiServicesException("No chat services available to retrieve EMB responses");
        }

        // wait for SVM to return
        Map<UUID, ChatResult> allResults = this.chatServices.awaitBackend(BackendServerType.EMB);
        if (allResults == null) {
            telemetryMap.add("EMB.answered", false);
            return currentResult;
        }

        // Get the top score (cannot use super::getTopScore since this affect the locked AI
        // and we want to be able to passthrough the state unaltered
        Optional<ChatResult> chatResultOpt = allResults.values().stream()
                .max(Comparator.comparingDouble(ChatResult::getScore));
        if (chatResultOpt.isPresent()) {
            ChatResult chatResult = chatResultOpt.get();
            telemetryMap.add("EMB.response", chatResult.getAnswer());
            telemetryMap.add("EMB.confidence", chatResult.getScore());
            telemetryMap.add("EMB.elapsed", chatResult.getElapsedTime());
            telemetryMap.add("EMB.answered", true);
        }
        // For now just ignore the response and fallthrough
        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        // For now just ignore the response and fallthrough
        return false;
    }
}

