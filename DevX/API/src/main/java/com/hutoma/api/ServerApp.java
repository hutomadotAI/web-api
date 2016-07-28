package com.hutoma.api;

import com.hutoma.api.endpoints.EndpointTryout;
import com.hutoma.api.endpoints.EndpointTryoutTop;
import com.hutoma.api.auth.AuthFilter;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerApp extends ResourceConfig {

    public ServerApp() {

        register(new ServerAppBinder());
        register("org.glassfish.jersey.filter.LoggingFilter;org.glassfish.jersey.media.multipart.MultiPartFeature");
        register(AuthFilter.class);
        registerInstances(new EndpointTryout(), new EndpointTryoutTop());
        //packages(true, "com.hutoma.api");
    }

}
