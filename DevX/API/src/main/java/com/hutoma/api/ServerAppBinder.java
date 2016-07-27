package com.hutoma.api;

import com.google.inject.Injector;
import com.hutoma.api.endpoints.EndpointTryout;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerAppBinder extends AbstractBinder {

    Injector guiceInjector;

    public ServerAppBinder(Injector guiceInjector) {
        this.guiceInjector = guiceInjector;
    }

    @Override
    protected void configure() {
        bind(guiceInjector).to(Injector.class);

        //bind(TestService.class).to(TestService.class);
        //bind(TestBusinessLogic.class).to(TestBusinessLogic.class);
    }


}
