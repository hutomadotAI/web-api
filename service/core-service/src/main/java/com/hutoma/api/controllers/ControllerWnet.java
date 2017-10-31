package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.containers.sub.BackendServerType;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerWnet extends ControllerBase {

    @Inject
    public ControllerWnet(final Config config, final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator, final AiServiceStatusLogger logger,
                          final QueueProcessor queueProcessor) {
        super(config, threadSubPool, serviceLocator, logger);
        this.queueProcessor = queueProcessor;
        this.queueProcessor.initialise(this, BackendServerType.WNET);
    }

    @Override
    public void kickQueueProcessor() {
        this.queueProcessor.kickQueueProcessor();
    }

    @Override
    public boolean logErrorIfNoTrainingCapacity() {
        return true;
    }
}
