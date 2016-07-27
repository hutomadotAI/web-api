package com.hutoma.api;

import com.hutoma.api.endpoints.EndpointTryout;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerAppBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(EndpointTryout.class).to(EndpointTryout.class);
        //bind(TestService.class).to(TestService.class);
        //bind(TestBusinessLogic.class).to(TestBusinessLogic.class);
    }


}
