package com.hutoma.api.logic.chat;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.containers.sub.ChatRequestInfo;

import javax.inject.Inject;

public class ChatRequestTrigger implements IChatHandler {

    private final AIChatServices chatServices;

    @Inject
    public ChatRequestTrigger(final AIChatServices chatServices) {
        this.chatServices = chatServices;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ServerConnector.AiServicesException, ChatBackendConnector.AiControllerException,
            NoServerAvailableException {



        // async start requests to all servers
        this.chatServices.startChatRequests(requestInfo.getAiIdentity(),
                requestInfo.getChatId(), requestInfo.getQuestion(), currentResult.getChatState());
        currentResult.getChatState().setAiChatServices(this.chatServices);

        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        // This never completes the chat
        return false;
    }
}
