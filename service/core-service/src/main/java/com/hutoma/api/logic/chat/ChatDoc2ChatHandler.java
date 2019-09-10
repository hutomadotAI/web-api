package com.hutoma.api.logic.chat;

import com.google.common.annotations.VisibleForTesting;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.IMemoryIntentHandler;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class ChatDoc2ChatHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chatdoc2chathandler";
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentLogic;
    private final ContextVariableExtractor contextVariableExtractor;
    private final FeatureToggler featureToggler;
    private boolean resultsFound;

    @VisibleForTesting
    @Inject
    public ChatDoc2ChatHandler(final IMemoryIntentHandler intentHandler,
                               final IntentProcessor intentLogic,
                               final ContextVariableExtractor contextVariableExtractor,
                               final ILogger logger,
                               final FeatureToggler featureToggler) {
        this.intentHandler = intentHandler;
        this.intentLogic = intentLogic;
        this.contextVariableExtractor = contextVariableExtractor;
        this.logger = logger;
        this.featureToggler = featureToggler;
        this.resultsFound = false;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException {

        ChatResult resultToReturn = currentResult;

        if (this.featureToggler.getStateForAiid(
                requestInfo.getDevId(), requestInfo.getAiid(), "show_knowledge_base"
        ) == FeatureToggler.FeatureState.T1) {
            ChatState state = currentResult.getChatState();
            AIChatServices chatServices = state.getAiChatServices();
            if (chatServices == null) {
                throw new ServerConnector.AiServicesException(
                        "No chat services available to retrieve DOC2CHAT responses");
            }
            Map<UUID, ChatResult> allResults = chatServices.awaitBackend(BackendServerType.DOC2CHAT);
            if (allResults != null) {
                ChatResult chatResult = allResults.get(requestInfo.getAiid());
                if (chatResult.getAnswer() != null) {
                    // remove trailing newline
                    resultToReturn.setAnswer(chatResult.getAnswer().trim());
                    resultToReturn.setScore(chatResult.getScore());
                    this.resultsFound = true;
                }
            }
        }
        return resultToReturn;
    }

    @Override
    public boolean chatCompleted() {
        return this.resultsFound;
    }
}

