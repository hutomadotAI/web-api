package com.hutoma.api;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.logic.*;
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
        bind(AIDomainLogic.class).to(AIDomainLogic.class);
        bind(ChatLogic.class).to(ChatLogic.class);
        bind(TrainingLogic.class).to(TrainingLogic.class);
        bind(MemoryLogic.class).to(MemoryLogic.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Config.class).to(Config.class).in(Singleton.class);
        bind(Database.class).to(Database.class);
        bind(MessageQueue.class).to(MessageQueue.class);
        bind(Tools.class).to(Tools.class);
        bind(Logger.class).to(Logger.class).in(Singleton.class);
        bind(NeuralNet.class).to(NeuralNet.class);
        bind(SemanticAnalysis.class).to(SemanticAnalysis.class);
        bind(HTMLExtractor.class).to(HTMLExtractor.class);
    }
}
