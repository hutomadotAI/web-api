package com.hutoma.api;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hutoma.api.endpoints.EndpointTryout;
import com.hutoma.api.logic.LogicTest;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerAppBinder extends AbstractBinder {

    public class ServerModule extends AbstractModule {

        @Override
        protected void configure() {
        }
    }
    @Override
    protected void configure() {
        bind(Guice.createInjector(new ServerModule())).to(Injector.class);
        //bind(TestService.class).to(TestService.class);
        //bind(TestBusinessLogic.class).to(TestBusinessLogic.class);
    }


}
