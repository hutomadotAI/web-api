package com.hutoma.api;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hutoma.api.endpoints.EndpointTryout;
import com.hutoma.api.endpoints.EndpointTryoutTop;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerApp extends ResourceConfig {

    Injector guiceInjector;

    public class ServerModule extends AbstractModule {

        @Override
        protected void configure() {
        }
    }

    public ServerApp() {

        guiceInjector = Guice.createInjector(new ServerModule());
        //register(new ServerAppBinder());
        registerInstances(new EndpointTryout(guiceInjector), new EndpointTryoutTop(guiceInjector));
        //packages(true, "com.hutoma.api");
    }
    /*

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        //classes.add(EndpointTryout.class);
        classes.add(EndpointTryoutTop.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singles = new HashSet<>();
        singles.add(new EndpointTryout(guiceInjector));
        return singles;
    }

    @Override
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }
    */

}
