package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.logging.ILogger;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

public class AimlServicesConnector extends ControllerConnector {

    @Inject
    public AimlServicesConnector(final Config config, final JerseyClient jerseyClient,
                                final ILogger logger, final Tools tools) {
        super(config, jerseyClient, logger, tools);
    }

    @Override
    public BackendServerType getServerType() {
        return BackendServerType.AIML;
    }
}
