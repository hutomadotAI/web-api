package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.GsonSerializer;
import com.hutoma.api.common.IJsonSerializer;
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
        bind(GsonSerializer.class).to(IJsonSerializer.class);
        bind(Config.class).to(Config.class);
    }
}
