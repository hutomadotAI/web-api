package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * AIML specialized controller.
 */
public class RequestAiml extends RequestBase {

    ControllerAiml controller;

    @Inject
    public RequestAiml(final JerseyClient jerseyClient, final Tools tools,
                       final Config config, final ThreadSubPool threadSubPool,
                       final ILogger logger, final JsonSerializer serializer,
                       final ControllerAiml controller) {
        super(jerseyClient, tools, config, threadSubPool, logger, serializer);
        this.controller = controller;
    }

    protected List<String> getBackendEndpoints() {
        return Collections.singletonList(this.config.getAimlChatEndpoint());
    }

    protected String getLogFrom() {
        return "RequestAIML";
    }
}