package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * AIML specialized controller.
 */
public class AimlController extends AiControllerBase {
    @Inject
    public AimlController(final JerseyClient jerseyClient, final Tools tools, final Config config,
                          final ILogger logger, final JsonSerializer serializer) {
        super(jerseyClient, tools, config, logger, serializer);
    }

    protected List<String> getBackendEndpoints() {
        return Collections.singletonList(this.config.getAimlChatEndpoint());
    }

    protected String getLogFrom() {
        return "AIMLController";
    }
}
