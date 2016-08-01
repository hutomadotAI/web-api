package com.hutoma.api;

import com.hutoma.api.logic.AdminLogic;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerBinder extends AbstractBinder {

    @Override
    protected void configure() {
        System.out.println("Binder configure");
        bind(AdminLogic.class).to(AdminLogic.class);
    }
}
