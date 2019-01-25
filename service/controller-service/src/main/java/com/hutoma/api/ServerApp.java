package com.hutoma.api;

import com.hutoma.api.jersey.SafetyNetExceptionMapper;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Resource configuration for the Controller Service application.
 */
public class ServerApp extends ResourceConfig {

    public ServerApp() {

        // HK2 DI Binder (service locator)
        register(new ServerBinder());

        // for upload support
        register(MultiPartFeature.class);

        // initialisation
        register(ServerInit.class);

        // exception mapper
        register(SafetyNetExceptionMapper.class);
        
        // validation and param extraction
        packages(false, "com.hutoma.api.validation");

        // endpoints
        packages(false, "com.hutoma.api.endpoints");
    }

}
