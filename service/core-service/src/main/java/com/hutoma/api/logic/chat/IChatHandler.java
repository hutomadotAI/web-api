package com.hutoma.api.logic.chat;

import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.LogMap;

public interface IChatHandler {

    double JUST_ABOVE_ZERO = 0.00001d;

    ChatResult doWork(ChatRequestInfo requestInfo, ChatResult currentResult, LogMap telemetryMap)
            throws ChatBaseException, ServerConnector.AiServicesException,
            ChatBackendConnector.AiControllerException, NoServerAvailableException;

    boolean chatCompleted();
}

