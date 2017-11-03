package com.hutoma.api;

import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.common.AccessLogger;
import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.CentralLogger;
import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.HTMLExtractor;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.AIQueueServices;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.connectors.AnalyticsESConnector;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.facebook.FacebookMachineID;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.QueueProcessor;
import com.hutoma.api.controllers.RequestAiml;
import com.hutoma.api.controllers.RequestRnn;
import com.hutoma.api.controllers.RequestWnet;
import com.hutoma.api.controllers.ServerTracker;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.logic.*;
import com.hutoma.api.memory.ChatStateHandler;
import com.hutoma.api.memory.ExternalEntityRecognizer;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.thread.IThreadConfig;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.thread.TrackedThreadSubPool;
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
        public void dispose(JerseyClient client) {
        }
    }

    @Override
    protected void configure() {

        // infrastructure
        bind(Config.class).to(Config.class).in(Singleton.class);
        bind(Config.class).to(IDatabaseConfig.class).to(IThreadConfig.class).to(ILoggerConfig.class).in(Singleton.class);
        bind(DatabaseConnectionPool.class).to(DatabaseConnectionPool.class).in(Singleton.class);
        bind(ThreadPool.class).to(ThreadPool.class).in(Singleton.class);
        bind(ThreadSubPool.class).to(ThreadSubPool.class);
        bind(TrackedThreadSubPool.class).to(TrackedThreadSubPool.class);
        bind(ServerTracker.class).to(ServerTracker.class);

        // logging
        bind(CentralLogger.class).to(ILogger.class).in(Singleton.class);
        // Chat requires specialized logging to support analytics
        bind(ChatLogger.class).to(ChatLogger.class).in(Singleton.class);
        // AI Services Status specialized logger
        bind(AiServiceStatusLogger.class).to(AiServiceStatusLogger.class).in(Singleton.class);
        // API Access specialized logger
        bind(AccessLogger.class).to(AccessLogger.class).in(Singleton.class);

        // database
        bind(Database.class).to(Database.class);
        bind(DatabaseAI.class).to(DatabaseAI.class);
        bind(DatabaseUser.class).to(DatabaseUser.class);
        bind(DatabaseMarketplace.class).to(DatabaseMarketplace.class);
        bind(DatabaseEntitiesIntents.class).to(DatabaseEntitiesIntents.class);
        bind(DatabaseAiStatusUpdates.class).to(DatabaseAiStatusUpdates.class);
        bind(DatabaseIntegrations.class).to(DatabaseIntegrations.class);
        bind(DatabaseBackends.class).to(DatabaseBackends.class);

        bind(DatabaseTransaction.class).to(DatabaseTransaction.class);
        bind(DatabaseCall.class).to(DatabaseCall.class);
        bind(TransactionalDatabaseCall.class).to(TransactionalDatabaseCall.class);

        // business logic
        bind(AdminLogic.class).to(AdminLogic.class);
        bind(AILogic.class).to(AILogic.class);
        bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        bind(ChatLogic.class).to(ChatLogic.class);
        bind(MemoryIntentHandler.class).to(MemoryIntentHandler.class).to(IMemoryIntentHandler.class);
        bind(ExternalEntityRecognizer.class).to(ExternalEntityRecognizer.class).to(IEntityRecognizer.class);
        bind(TrainingLogic.class).to(TrainingLogic.class);
        bind(EntityLogic.class).to(EntityLogic.class);
        bind(IntentLogic.class).to(IntentLogic.class);
        bind(DeveloperInfoLogic.class).to(DeveloperInfoLogic.class);
        bind(AIIntegrationLogic.class).to(AIIntegrationLogic.class);
        bind(InviteLogic.class).to(InviteLogic.class);
        bind(AIServicesLogic.class).to(AIServicesLogic.class);
        bind(FacebookIntegrationLogic.class).to(FacebookIntegrationLogic.class);
        bind(AnalyticsLogic.class).to(AnalyticsLogic.class);
        bind(AiStrings.class).to(AiStrings.class);

        // other
        bind(JsonSerializer.class).to(JsonSerializer.class);
        bind(Tools.class).to(Tools.class);
        bind(HTMLExtractor.class).to(HTMLExtractor.class);
        bind(Validate.class).to(Validate.class);
        bind(RateLimitCheck.class).to(RateLimitCheck.class);
        bind(ChatStateHandler.class).to(ChatStateHandler.class);
        bind(WebHooks.class).to(WebHooks.class);
        bind(FacebookConnector.class).to(FacebookConnector.class);
        bind(FacebookMachineID.class).to(FacebookMachineID.class).in(Singleton.class);
        bind(FacebookChatHandler.class).to(FacebookChatHandler.class);
        bind(AnalyticsESConnector.class).to(AnalyticsESConnector.class);

        // backend facing related structures
        bind(AIServices.class).to(AIServices.class);
        bind(AIQueueServices.class).to(AIQueueServices.class);
        bind(AIChatServices.class).to(AIChatServices.class);
        bind(RequestWnet.class).to(RequestWnet.class);
        bind(RequestRnn.class).to(RequestRnn.class);
        bind(RequestAiml.class).to(RequestAiml.class);
        bind(ControllerWnet.class).to(ControllerWnet.class).in(Singleton.class);
        bind(ControllerRnn.class).to(ControllerRnn.class).in(Singleton.class);
        bind(ControllerAiml.class).to(ControllerAiml.class).in(Singleton.class);
        bind(QueueProcessor.class).to(QueueProcessor.class);
        bind(EntityRecognizerService.class).to(EntityRecognizerService.class);

        // UI
        bind(UILogic.class).to(UILogic.class);
        bind(DatabaseUI.class).to(DatabaseUI.class);

        // Jersey HTTP client
        bindFactory(JerseyClientFactory.class).to(JerseyClient.class);
    }
}