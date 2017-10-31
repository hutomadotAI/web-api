package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * WNET request adapter
 */
public class RequestWnet extends RequestBase {

    @Inject
    public RequestWnet(final JerseyClient jerseyClient, final Tools tools,
                       final Config config, final TrackedThreadSubPool threadSubPool,
                       final ILogger logger, final JsonSerializer serializer,
                       final ControllerWnet controller) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controller);
    }

    protected String getLogFrom() {
        return "RequestWNET";
    }
}
