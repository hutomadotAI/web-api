package com.hutoma.api;

import com.hutoma.api.logic.AdminLogic;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Created by David MG on 25/07/2016.
 */
public class ServerAppBinder extends AbstractBinder {

    @Override
    protected void configure() {
        System.out.println("ServerAppBinder configure");
        bind(AdminLogic.class).to(AdminLogic.class);
        //bind(TestService.class).to(TestService.class);
        //bind(TestBusinessLogic.class).to(TestBusinessLogic.class);
    }
}
