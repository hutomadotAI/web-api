package com.hutoma.api;

import com.hutoma.api.common.JerseyGsonProvider;
import com.hutoma.api.jersey.SafetyNetExceptionMapper;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerApp extends ResourceConfig {

    public ServerApp() {

        // HK2 DI Binder (service locator)
        register(new ServerBinder());

        // for upload support
        register(MultiPartFeature.class);

        // initialisation
        register(ServerInit.class);

        // JSON serializer using GSON
        register(JerseyGsonProvider.class);

        // exception mapper
        register(SafetyNetExceptionMapper.class);
        
        // authorization filter
        packages(false, "com.hutoma.api.access");

        // validation and param extraction
        packages(false, "com.hutoma.api.validation");

        // endpoints
        packages(false, "com.hutoma.api.endpoints");
    }

}
