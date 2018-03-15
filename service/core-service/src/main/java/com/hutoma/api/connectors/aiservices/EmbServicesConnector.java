package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.logging.ILogger;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EmbServicesConnector extends ControllerConnector {

    @Inject
    public EmbServicesConnector(final Config config, final JerseyClient jerseyClient,
                                final ILogger logger, final Tools tools) {
        super(config, jerseyClient, logger, tools);
    }

    @Override
    public BackendServerType getServerType() {
        return BackendServerType.EMB;
    }
}
