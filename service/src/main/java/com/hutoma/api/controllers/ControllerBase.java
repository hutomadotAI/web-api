package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;

import java.util.List;
import javax.inject.Inject;


/***
 * Code common to all controllers.
 * Singleton class that keeps track of registered back-end servers and their affinity
 */
public abstract class ControllerBase {

    protected final Config config;

    @Inject
    public ControllerBase(final Config config) {
        this.config = config;
    }

    protected abstract List<String> getBackendEndpoints();

}
