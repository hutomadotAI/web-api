package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.thread.ThreadSubPool;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerRnn extends ControllerBase {

    @Inject
    public ControllerRnn(final ControllerConfig config, final ThreadSubPool threadSubPool,
                         final ServiceLocator serviceLocator, final AiServiceStatusLogger logger) {
        super(config, threadSubPool, serviceLocator, logger);
    }

    @Override
    public boolean logErrorIfNoTrainingCapacity() {
        return true;
    }

}
