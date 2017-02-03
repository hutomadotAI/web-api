package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerRnn extends ControllerBase {

    @Inject
    public ControllerRnn(final Config config, final Tools tools, final ThreadSubPool threadSubPool,
                         final ServiceLocator serviceLocator, final ILogger logger) {
        super(config, tools, threadSubPool, serviceLocator, logger);
    }

    @Override
    public List<String> getFallbackBackendEndpoints() {
        return Collections.singletonList(this.config.getRnnChatEndpoint());
    }
}
