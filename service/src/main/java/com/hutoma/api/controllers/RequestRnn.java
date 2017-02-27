package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;

/**
 * RNN request adapter
 */
public class RequestRnn extends RequestBase {


    @Inject
    public RequestRnn(final JerseyClient jerseyClient, final Tools tools,
                      final Config config, final ThreadSubPool threadSubPool,
                      final ILogger logger, final JsonSerializer serializer,
                      final ControllerRnn controller) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer, controller);
    }

    protected String getLogFrom() {
        return "RequestRNN";
    }
}
