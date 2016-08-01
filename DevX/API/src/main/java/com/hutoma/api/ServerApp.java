package com.hutoma.api;


import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerApp extends ResourceConfig {

    public ServerApp() {
        System.out.println("ServerApp constructor");
        register(new ServerBinder());
        packages(true, "com.hutoma.api.endpoints");
    }
}
