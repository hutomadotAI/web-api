package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ThreadSubPool;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerRnn extends ControllerBase {

    @Inject
    public ControllerRnn(final Config config, final ThreadSubPool threadSubPool,
                         final ServiceLocator serviceLocator, final AiServiceStatusLogger logger) {
        super(config, threadSubPool, serviceLocator, logger);
    }

}
