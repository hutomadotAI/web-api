package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.logging.ILogger;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WnetServicesConnector extends ControllerConnector {

    @Inject
    public WnetServicesConnector(final Config config, final JsonSerializer serializer, final JerseyClient jerseyClient,
                                final ILogger logger, final Tools tools) {
        super(config, serializer, jerseyClient, logger, tools);
    }

    @Override
    public BackendServerType getServerType() {
        return BackendServerType.WNET;
    }
}
