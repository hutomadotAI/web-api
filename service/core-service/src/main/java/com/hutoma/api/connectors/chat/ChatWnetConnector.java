package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.connectors.aiservices.WnetServicesConnector;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * WNET request adapter
 */
public class ChatWnetConnector extends ChatBackendConnector {

    @Inject
    public ChatWnetConnector(final JerseyClient jerseyClient, final Tools tools, final Config config,
                             final TrackedThreadSubPool threadSubPool, final ILogger logger,
                             final JsonSerializer serializer, final WnetServicesConnector controllerConnector,
                             final Provider<ChatBackendRequester> requesterProvider) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controllerConnector, requesterProvider);
    }

    @Override
    protected String getLogFrom() {
        return "RequestWNET";
    }

    @Override
    protected BackendServerType getServerType() {
        return BackendServerType.WNET;
    }
}
