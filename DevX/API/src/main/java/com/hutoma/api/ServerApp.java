package com.hutoma.api;

import com.hutoma.api.auth.AuthFilter;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by David MG on 25/07/2016.
 */
@ApplicationPath("/")
public class ServerApp extends ResourceConfig {

    public ServerApp() {
        System.out.println("ServerApp constructor");

        register(new ServerAppBinder());
        register("org.glassfish.jersey.filter.LoggingFilter;org.glassfish.jersey.media.multipart.MultiPartFeature");
        register(AuthFilter.class);
        packages(true, "com.hutoma.api");

    }

}
