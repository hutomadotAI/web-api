package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.SvmServicesConnector;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * SVM request adapter
 */
public class ChatSvmConnector extends ChatBackendConnector {

    @Inject
    public ChatSvmConnector(final JerseyClient jerseyClient, final Tools tools, final Config config,
                            final TrackedThreadSubPool threadSubPool, final ILogger logger,
                            final JsonSerializer serializer, final SvmServicesConnector controllerConnector,
                            final Provider<ChatBackendRequester> requesterProvider) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controllerConnector, requesterProvider);
    }

    @Override
    protected String getLogFrom() {
        return "RequestSVM";
    }

    @Override
    protected BackendServerType getServerType() {
        return BackendServerType.SVM;
    }
}