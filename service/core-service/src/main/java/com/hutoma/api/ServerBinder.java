package com.hutoma.api;

import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.aiservices.*;
import com.hutoma.api.connectors.chat.*;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.facebook.FacebookMachineID;
import com.hutoma.api.logging.CentralLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.logic.*;
import com.hutoma.api.logic.chat.*;
import com.hutoma.api.memory.*;
import com.hutoma.api.thread.*;
import com.hutoma.api.validation.QueryFilter;
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
        bind(Config.class).to(IDatabaseConfig.class).to(IThreadConfig.class).to(ILoggerConfig.class)
                .to(IConnectConfig.class).in(Singleton.class);
        bind(DatabaseConnectionPool.class).to(DatabaseConnectionPool.class).in(Singleton.class);
        bind(ThreadPool.class).to(ThreadPool.class).in(Singleton.class);
        bind(ThreadSubPool.class).to(IThreadSubPool.class);
        bind(TrackedThreadSubPool.class).to(ITrackedThreadSubPool.class);
        bind(QueryFilter.class).to(QueryFilter.class);
        bind(ServerMonitor.class).to(ServerMonitor.class);

        // logging
        bind(CentralLogger.class).to(ILogger.class).in(Singleton.class);
        // Chat requires specialized logging to support analytics
        bind(ChatLogger.class).to(ChatLogger.class).in(Singleton.class);
        // API Access specialized logger
        bind(AccessLogger.class).to(AccessLogger.class).in(Singleton.class);

        // database
        bind(Database.class).to(Database.class);
        bind(DatabaseAI.class).to(DatabaseAI.class);
        bind(DatabaseUser.class).to(DatabaseUser.class);
        bind(DatabaseMarketplace.class).to(DatabaseMarketplace.class);
        bind(DatabaseEntitiesIntents.class).to(DatabaseEntitiesIntents.class);
        bind(DatabaseIntegrations.class).to(DatabaseIntegrations.class);
        bind(DatabaseTransaction.class).to(DatabaseTransaction.class);
        bind(DatabaseCall.class).to(DatabaseCall.class);
        bind(TransactionalDatabaseCall.class).to(TransactionalDatabaseCall.class);
        bind(DatabaseFeatures.class).to(DatabaseFeatures.class);

        // business logic
        bind(AdminLogic.class).to(AdminLogic.class);
        bind(AILogic.class).to(AILogic.class);
        bind(LanguageLogic.class).to(LanguageLogic.class);
        bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        bind(ChatLogic.class).to(ChatLogic.class);
        bind(MemoryIntentHandler.class).to(MemoryIntentHandler.class).to(IMemoryIntentHandler.class);
        bind(ExternalEntityRecognizer.class).to(ExternalEntityRecognizer.class).to(IEntityRecognizer.class);
        bind(TrainingLogic.class).to(TrainingLogic.class);
        bind(EntityLogic.class).to(EntityLogic.class);
        bind(IntentLogic.class).to(IntentLogic.class);
        bind(DeveloperInfoLogic.class).to(DeveloperInfoLogic.class);
        bind(AIIntegrationLogic.class).to(AIIntegrationLogic.class);
        bind(FacebookIntegrationLogic.class).to(FacebookIntegrationLogic.class);
        bind(AnalyticsLogic.class).to(AnalyticsLogic.class);
        bind(AiStrings.class).to(AiStrings.class);
        bind(IntentProcessor.class).to(IntentProcessor.class);
        bind(ConditionEvaluator.class).to(ConditionEvaluator.class);

        // Chat workflow
        bind(ChatWorkflow.class).to(ChatWorkflow.class).to(Singleton.class);
        bind(ChatEntityValueHandler.class).to(ChatEntityValueHandler.class);
        bind(ChatHandoverHandler.class).to(ChatHandoverHandler.class);
        bind(ChatPassthroughHandler.class).to(ChatPassthroughHandler.class);
        bind(ChatIntentHandler.class).to(ChatIntentHandler.class);
        bind(ChatRequestTrigger.class).to(ChatRequestTrigger.class);
        bind(ChatAimlHandler.class).to(ChatAimlHandler.class);
        bind(ChatEmbHandler.class).to(ChatEmbHandler.class);
        bind(ChatDoc2ChatHandler.class).to(ChatDoc2ChatHandler.class);
        bind(ContextVariableExtractor.class).to(ContextVariableExtractor.class);
        bind(ChatDefaultHandler.class).to(ChatDefaultHandler.class);
        bind(ChatConnectors.class).to(ChatConnectors.class);
        bind(WebhookHandler.class).to(WebhookHandler.class);
        bind(SysAnyCommandsHandler.class).to(SysAnyCommandsHandler.class);

        // Feature toggler
        bind(FeatureToggler.class).to(FeatureToggler.class).in(Singleton.class);

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
        bind(AnalyticsESConnector.class).to(AnalyticsESConnector.class).in(Singleton.class);
        bind(CsvIntentReader.class).to(CsvIntentReader.class);

        // backend facing related structures
        bind(AIServices.class).to(AIServices.class);
        bind(AIChatServices.class).to(AIChatServices.class);
        bind(ChatAimlConnector.class).to(ChatAimlConnector.class);
        bind(ChatEmbConnector.class).to(ChatEmbConnector.class);
        bind(ChatDoc2ChatConnector.class).to(ChatDoc2ChatConnector.class);
        bind(ServiceStatusConnector.class).to(ServiceStatusConnector.class);
        bind(EntityRecognizerService.class).to(EntityRecognizerService.class);
        bind(AimlServicesConnector.class).to(AimlServicesConnector.class);
        bind(EmbServicesConnector.class).to(EmbServicesConnector.class);
        bind(Doc2ChatServicesConnector.class).to(Doc2ChatServicesConnector.class);
        bind(AiServicesQueue.class).to(AiServicesQueue.class);
        bind(ChatBackendRequester.class).to(ChatBackendRequester.class);
        bind(BackendServicesConnectors.class).to(BackendServicesConnectors.class);

        // UI
        bind(UILogic.class).to(UILogic.class);
        bind(DatabaseUI.class).to(DatabaseUI.class);

        // Jersey HTTP client
        bindFactory(JerseyClientFactory.class).to(JerseyClient.class);
    }
}
