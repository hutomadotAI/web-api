package com.hutoma.api;

import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.connectors.db.TransactionalDatabaseCall;
import com.hutoma.api.logic.*;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.memory.SimpleEntityRecognizer;
import com.hutoma.api.validation.Validate;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerBinder extends AbstractBinder {

    @Override
    protected void configure() {

        // infrastructure
        bind(Config.class).to(Config.class).in(Singleton.class);
        bind(DatabaseConnectionPool.class).to(DatabaseConnectionPool.class).in(Singleton.class);
        bind(TelemetryLogger.class).to(TelemetryLogger.class).to(Logger.class).to(ILogger.class).in(Singleton.class);

        // business logic
        bind(AdminLogic.class).to(AdminLogic.class);
        bind(AILogic.class).to(AILogic.class);
        bind(AIDomainLogic.class).to(AIDomainLogic.class);
        bind(ChatLogic.class).to(ChatLogic.class);
        bind(MemoryIntentHandler.class).to(MemoryIntentHandler.class).to(IMemoryIntentHandler.class);
        bind(SimpleEntityRecognizer.class).to(SimpleEntityRecognizer.class).to(IEntityRecognizer.class);
        bind(TrainingLogic.class).to(TrainingLogic.class);
        bind(EntityLogic.class).to(EntityLogic.class);
        bind(IntentLogic.class).to(IntentLogic.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Database.class).to(Database.class);
        bind(DatabaseEntitiesIntents.class).to(DatabaseEntitiesIntents.class);
        bind(DatabaseTransaction.class).to(DatabaseTransaction.class);
        bind(DatabaseCall.class).to(DatabaseCall.class);
        bind(TransactionalDatabaseCall.class).to(TransactionalDatabaseCall.class);
        bind(MessageQueue.class).to(MessageQueue.class);
        bind(Tools.class).to(Tools.class);
        bind(NeuralNet.class).to(NeuralNet.class);
        bind(SemanticAnalysis.class).to(SemanticAnalysis.class);
        bind(HTMLExtractor.class).to(HTMLExtractor.class);
        bind(Validate.class).to(Validate.class);
        bind(RateLimitCheck.class).to(RateLimitCheck.class);

    }
}
