package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.Doc2ChatServicesConnector;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.ITrackedThreadSubPool;
import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Embeddings SVM request adapter
 */
public class ChatDoc2ChatConnector extends ChatBackendConnector {

    @Inject
    public ChatDoc2ChatConnector(final JerseyClient jerseyClient,
                                 final Tools tools,
                                 final Config config,
                                 final ITrackedThreadSubPool threadSubPool,
                                 final ILogger logger,
                                 final JsonSerializer serializer,
                                 final Doc2ChatServicesConnector controllerConnector,
                                 final Provider<ChatBackendRequester> requesterProvider) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controllerConnector, requesterProvider);
    }

    @Override
    protected String getLogFrom() {
        return "RequestDoc2Chat";
    }

    @Override
    protected BackendServerType getServerType() {
        return BackendServerType.DOC2CHAT;
    }
}
