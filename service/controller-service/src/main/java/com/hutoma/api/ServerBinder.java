package com.hutoma.api;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.HTMLExtractor;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseBackends;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.connectors.db.IDatabaseConfig;
import com.hutoma.api.connectors.db.TransactionalDatabaseCall;
import com.hutoma.api.controllers.AIQueueServices;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.QueueProcessor;
import com.hutoma.api.controllers.ServerTracker;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.logic.AIServicesLogic;
import com.hutoma.api.logic.ControllerLogic;
import com.hutoma.api.thread.IThreadConfig;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.thread.TrackedThreadSubPool;

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
        public void dispose(JerseyClient client) {
        }
    }

    @Override
    protected void configure() {

        // infrastructure
        bind(ControllerConfig.class).to(ControllerConfig.class).to(IDatabaseConfig.class).to(IThreadConfig.class)
                .to(ILoggerConfig.class).in(Singleton.class);
        bind(DatabaseConnectionPool.class).to(DatabaseConnectionPool.class).in(Singleton.class);
        bind(ThreadPool.class).to(ThreadPool.class).in(Singleton.class);
        bind(ThreadSubPool.class).to(ThreadSubPool.class);
        bind(TrackedThreadSubPool.class).to(TrackedThreadSubPool.class);
        bind(ServerTracker.class).to(ServerTracker.class);

        // database
        bind(Database.class).to(Database.class);
        bind(DatabaseBackends.class).to(DatabaseBackends.class);
        bind(DatabaseTransaction.class).to(DatabaseTransaction.class);
        bind(DatabaseCall.class).to(DatabaseCall.class);
        bind(TransactionalDatabaseCall.class).to(TransactionalDatabaseCall.class);
        bind(DatabaseAiStatusUpdates.class).to(DatabaseAiStatusUpdates.class);

        // business logic
        bind(ControllerLogic.class).to(ControllerLogic.class);
        bind(AIServicesLogic.class).to(AIServicesLogic.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Tools.class).to(Tools.class);
        bind(HTMLExtractor.class).to(HTMLExtractor.class);
        bind(AIQueueServices.class).to(AIQueueServices.class);

        // Jersey HTTP client
        bindFactory(JerseyClientFactory.class).to(JerseyClient.class);


        // Controller
        bind(ControllerWnet.class).to(ControllerWnet.class).in(Singleton.class);
        bind(ControllerRnn.class).to(ControllerRnn.class).in(Singleton.class);
        bind(ControllerAiml.class).to(ControllerAiml.class).in(Singleton.class);
        bind(QueueProcessor.class).to(QueueProcessor.class);

        // AI Services Status specialized logger
        bind(AiServiceStatusLogger.class).to(AiServiceStatusLogger.class).to(ILogger.class).in(Singleton.class);
    }
}
