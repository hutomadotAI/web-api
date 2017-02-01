package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by David MG on 31/01/2017.
 */
public class ControllerRnn extends ControllerBase {

    @Inject
    public ControllerRnn(final Config config) {
        super(config);
    }

    @Override
    public List<String> getBackendEndpoints() {
        return Collections.singletonList(this.config.getRnnChatEndpoint());
    }
}
