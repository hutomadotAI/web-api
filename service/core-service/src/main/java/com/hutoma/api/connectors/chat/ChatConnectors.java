package com.hutoma.api.connectors.chat;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.containers.AiDevId;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

public class ChatConnectors {

    private static final String LOGFROM = "chatconnectors";
    private Map<BackendServerType, ChatConnectorItem> connectorMap = new LinkedHashMap<>();

    @Inject
    ChatConnectors(final ChatWnetConnector backendWnetConnector,
                          final ChatAimlConnector backendAimlConnector,
                          final ChatSvmConnector backendSvmConnedtor) {
        connectorMap.put(BackendServerType.WNET, new ChatConnectorItem(backendWnetConnector, false));
        connectorMap.put(BackendServerType.AIML, new ChatConnectorItem(backendAimlConnector, false));
        connectorMap.put(BackendServerType.SVM, new ChatConnectorItem(backendSvmConnedtor, true, 1000));
    }

    public Map<UUID, ChatResult> awaitBackend(final BackendServerType serverType, final int remainingTime,
                                              final ILogger logger)
            throws ChatBackendConnector.AiControllerException {
        ChatConnectorItem item = connectorMap.get(serverType);
        if (item.connectorFutures != null) {
            try {
                return item.backendConnector.waitForAll(item.connectorFutures,
                        item.maxRequestMs != 0 ? Math.min(item.maxRequestMs, remainingTime) : remainingTime);
            } catch (ChatBackendConnector.AiControllerException ex) {
                // Ignore all exceptions from shadow connectors
                if (item.isShadow) {
                    logger.logDebug(LOGFROM, String.format("Timeout while connecting to %s", serverType.value()));
                } else {
                    throw ex;
                }
            }
        }
        return null;
    }

    public Set<BackendServerType> canChatWith(final BackendStatus backendStatus) {
        HashSet<BackendServerType> chatSet = new HashSet<>();
        for (BackendServerType serverType: connectorMap.keySet()) {
            TrainingStatus trainingStatus = backendStatus.getEngineStatus(serverType).getTrainingStatus();
            if (trainingStatus == TrainingStatus.AI_TRAINING_COMPLETE) {
                chatSet.add(serverType);
            }
        }
        return chatSet;
    }

    public void issueChatRequests(final BackendServerType serverType, final Map<String, String> chatParams,
                                  final List<AiDevId> ais, final ChatState chatState)
            throws NoServerAvailableException, ChatBackendConnector.AiControllerException {
        ChatConnectorItem item = connectorMap.get(serverType);
        item.connectorFutures = item.backendConnector.issueChatRequests(chatParams, ais, chatState);
    }

    public void abandonCalls() {
        for (ChatConnectorItem item: connectorMap.values()) {
            if (item.connectorFutures != null) {
                item.connectorFutures.forEach(ChatBackendConnector.RequestInProgress::closeRequest);
            }
            item.backendConnector.abandonCalls();
        }
    }

    private static class ChatConnectorItem {
        private ChatBackendConnector backendConnector;
        private List<ChatBackendConnector.RequestInProgress> connectorFutures;
        private int maxRequestMs;
        private boolean isShadow;

        ChatConnectorItem(final ChatBackendConnector backendConnector, final boolean isShadow) {
            this(backendConnector, isShadow, 0);
        }

        ChatConnectorItem(final ChatBackendConnector backendConnector, final boolean isShadow,
                          final int maxRequestMs) {
            this.backendConnector = backendConnector;
            this.isShadow = isShadow;
            this.maxRequestMs = maxRequestMs;
        }
    }
}
