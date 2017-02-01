package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by David MG on 01/02/2017.
 */
public class ControllerAiml extends ControllerBase {

    @Inject
    public ControllerAiml(final Config config) {
        super(config);
    }

    @Override
    public List<String> getBackendEndpoints() {
        return Collections.singletonList(this.config.getAimlChatEndpoint());
    }
}
