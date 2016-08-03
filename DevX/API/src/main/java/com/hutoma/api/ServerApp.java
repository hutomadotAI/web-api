package com.hutoma.api;


import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerApp extends ResourceConfig {

    public ServerApp() {
        System.out.println("ServerApp constructor");

        // HK2 DI Binder (service locator)
        register(new ServerBinder());

        // authorization filter
        packages(false, "com.hutoma.api.auth");

        // endpoints
        packages(false, "com.hutoma.api.endpoints");

        // old namespace. this will enable endpoints that have not yet been migrated to the new.
        //packages(true, "hutoma.api.server.ai");
    }
}
