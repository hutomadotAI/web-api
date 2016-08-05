package com.hutoma.api;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.AdminLogic;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerBinder extends AbstractBinder {

    @Override
    protected void configure() {

        // business logic
        bind(AdminLogic.class).to(AdminLogic.class);
        bind(AILogic.class).to(AILogic.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Config.class).to(Config.class).in(Singleton.class);
        bind(Database.class).to(Database.class);
        bind(MessageQueue.class).to(MessageQueue.class);
        bind(Tools.class).to(Tools.class);
        bind(Logger.class).to(Logger.class).in(Singleton.class);
    }
}
