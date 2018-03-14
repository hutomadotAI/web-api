package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.thread.ThreadSubPool;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Embeddings controller
 */
@Singleton
public class ControllerEmb extends ControllerBase {

    private final QueueProcessor queueProcessor;

    @Inject
    ControllerEmb(final ControllerConfig config, final ThreadSubPool threadSubPool,
                  final ServiceLocator serviceLocator, final AiServiceStatusLogger logger,
                  final QueueProcessor queueProcessor) {
        super(config, threadSubPool, serviceLocator, logger);
        this.queueProcessor = queueProcessor;
        this.queueProcessor.initialise(this, BackendServerType.EMB);
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
}
