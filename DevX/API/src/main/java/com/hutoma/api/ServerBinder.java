package com.hutoma.api;

import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.TelemetryLogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.connectors.db.TransactionalDatabaseCall;
import com.hutoma.api.logic.AIBotStoreLogic;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.AdminLogic;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.EntityLogic;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.logic.MeshLogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.memory.SimpleEntityRecognizer;
import com.hutoma.api.validation.Validate;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.inject.Singleton;

/**
 * Created by David MG on 28/07/2016.
 */
public class ServerBinder extends AbstractBinder {

    private static class JerseyClientFactory implements Factory<JerseyClient> {
        @Override
        public JerseyClient provide() {
            return JerseyClientBuilder.createClient();
        }

        @Override
        public void dispose(JerseyClient foo) {
            // meh
        }
    }

    @Override
    protected void configure() {

        // infrastructure
        bind(Config.class).to(Config.class).in(Singleton.class);
        bind(DatabaseConnectionPool.class).to(DatabaseConnectionPool.class).in(Singleton.class);
        bind(TelemetryLogger.class).to(TelemetryLogger.class).to(Logger.class).to(ILogger.class).in(Singleton.class);

        // business logic
        bind(AdminLogic.class).to(AdminLogic.class);
        bind(AILogic.class).to(AILogic.class);
        bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        bind(ChatLogic.class).to(ChatLogic.class);
        bind(MemoryIntentHandler.class).to(MemoryIntentHandler.class).to(IMemoryIntentHandler.class);
        bind(SimpleEntityRecognizer.class).to(SimpleEntityRecognizer.class).to(IEntityRecognizer.class);
        bind(TrainingLogic.class).to(TrainingLogic.class);
        bind(EntityLogic.class).to(EntityLogic.class);
        bind(IntentLogic.class).to(IntentLogic.class);
        bind(MeshLogic.class).to(MeshLogic.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Database.class).to(Database.class);
        bind(DatabaseEntitiesIntents.class).to(DatabaseEntitiesIntents.class);
        bind(DatabaseTransaction.class).to(DatabaseTransaction.class);
        bind(DatabaseCall.class).to(DatabaseCall.class);
        bind(TransactionalDatabaseCall.class).to(TransactionalDatabaseCall.class);
        bind(Tools.class).to(Tools.class);
        bind(NeuralNet.class).to(NeuralNet.class);
        bind(SemanticAnalysis.class).to(SemanticAnalysis.class);
        bind(HTMLExtractor.class).to(HTMLExtractor.class);
        bind(Validate.class).to(Validate.class);
        bind(RateLimitCheck.class).to(RateLimitCheck.class);

        // backend facing related structures
        bind(AIServices.class).to(AIServices.class);
        // Jersey client
        bindFactory(JerseyClientFactory.class).to(JerseyClient.class);
    }
}
