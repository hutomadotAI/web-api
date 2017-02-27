package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * WNET request adapter
 */
public class RequestWnet extends RequestBase {

    @Inject
    public RequestWnet(final JerseyClient jerseyClient, final Tools tools,
                       final Config config, final ThreadSubPool threadSubPool,
                       final ILogger logger, final JsonSerializer serializer,
                       final ControllerWnet controller) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controller);
    }

    protected String getLogFrom() {
        return "RequestWNET";
    }
}
