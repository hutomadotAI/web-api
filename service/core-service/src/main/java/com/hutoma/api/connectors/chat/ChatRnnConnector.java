package com.hutoma.api.connectors.chat;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * RNN request adapter
 */
public class ChatRnnConnector extends ChatBackendConnector {


    @Inject
    public ChatRnnConnector(final JerseyClient jerseyClient, final Tools tools,
                            final Config config, final TrackedThreadSubPool threadSubPool,
                            final ILogger logger, final JsonSerializer serializer,
                            final ControllerConnector controllerConnector) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controllerConnector);
    }

    @Override
    protected String getLogFrom() {
        return "RequestRNN";
    }

    @Override
    protected BackendServerType getServerType() {
        return BackendServerType.RNN;
    }
}
