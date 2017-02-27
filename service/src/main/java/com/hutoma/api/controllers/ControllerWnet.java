package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerWnet extends ControllerBase {

    @Inject
    public ControllerWnet(final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator, final ILogger logger) {
        super(threadSubPool, serviceLocator, logger);
    }

}
