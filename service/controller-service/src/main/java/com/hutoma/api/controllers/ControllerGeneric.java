package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.thread.ThreadSubPool;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

/**
 * Generic backend controller
 */
public class ControllerGeneric extends ControllerBase {

    private final QueueProcessor queueProcessor;
    private ServiceIdentity serviceIdentity; // Helpful for debugging

    @Inject
    ControllerGeneric(final ControllerConfig config,
                      final ThreadSubPool threadSubPool,
                      final ServiceLocator serviceLocator,
                      final AiServiceStatusLogger logger,
                      final QueueProcessor queueProcessor) {
        super(config, threadSubPool, serviceLocator, logger);
        this.queueProcessor = queueProcessor;
    }

    @Override
    public boolean logErrorIfNoTrainingCapacity() {
        return true;
    }

    @Override
    public void kickQueue() {
        this.queueProcessor.kickQueueProcessor();
    }

    @Override
    public void terminateQueue() {
        this.queueProcessor.stop();
    }

    @Override
    public void initialize(final ServiceIdentity serviceIdentity) {
        this.serviceIdentity = serviceIdentity;
        this.queueProcessor.initialise(this, serviceIdentity);
    }
}
