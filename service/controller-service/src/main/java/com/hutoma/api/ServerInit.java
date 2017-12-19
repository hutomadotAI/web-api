package com.hutoma.api;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.LogMap;

import org.fluentd.logger.FluentLogger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;

/**
 * Created by David MG on 02/09/2016.
 */
public class ServerInit implements ApplicationEventListener {

    private static final String LOGFROM = "serverinit";
    @Inject
    private ServiceLocator serviceLocator;

    private AiServiceStatusLogger logger;
    private ControllerWnet wnetController;
    private ControllerRnn rnnController;
    private ControllerAiml aimlController;

    /**
     * Application event handler.
     * @param applicationEvent the application event
     */
    @Override
    public void onEvent(final ApplicationEvent applicationEvent) {
        switch (applicationEvent.getType()) {
            case INITIALIZATION_FINISHED:
                initialise(applicationEvent);
                break;
            case DESTROY_FINISHED:
                this.wnetController.terminateQueue();
                if (rnnController != null) {
                    this.rnnController.terminateQueue();
                }
                this.aimlController.terminateQueue();
                FluentLogger.flushAll();
                FluentLogger.closeAll();
                break;
            case RELOAD_FINISHED:
            case INITIALIZATION_START:

            default:
                break;
        }
    }

    /**
     * Request handler for intercepting all requests performed.
     * @param requestEvent the request event
     * @return the request event listener
     */
    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return new RequestEventListener() {
            private long startTime;

            @Override
            public void onEvent(final RequestEvent requestEvent) {
                switch (requestEvent.getType()) {
                    case RESOURCE_METHOD_START:
                        this.startTime = System.currentTimeMillis();
                        break;
                    case FINISHED:
                        // Only log if we have a method to handle it
                        if (requestEvent.getUriInfo().getMatchedResourceMethod() != null) {
                            final long finishTime = System.currentTimeMillis();
                            Invocable invocable = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable();
                            LogMap logMap = LogMap.map("Start", this.startTime)
                                    .put("Finish", finishTime)
                                    .put("Duration", finishTime - this.startTime)
                                    .put("Success", requestEvent.isSuccess())
                                    .put("Class", invocable.getDefinitionMethod().getDeclaringClass().getSimpleName())
                                    .put("Method", invocable.getDefinitionMethod().getName());
                            ServerInit.this.logger.logPerf("perfrequestlistener", "APICall", logMap);
                        }
                        break;
                    default: // empty
                        break;
                }
            }
        };
    }

    private void initialise(final ApplicationEvent applicationEvent) {
        this.logger = this.serviceLocator.getService(AiServiceStatusLogger.class);
        ControllerConfig config = this.serviceLocator.getService(ControllerConfig.class);
        try {
            this.logger.initialize(config);
            DatabaseConnectionPool connectionPool = this.serviceLocator.getService(DatabaseConnectionPool.class);
            connectionPool.borrowConnection().close();
            this.logger.logInfo(LOGFROM, "initialisation finished");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "initialisation error: " + e.toString());
        }

        // create the singleton instances so that the timers start
        // and with them the server monitoring
        this.wnetController = this.serviceLocator.getService(ControllerWnet.class);
        if (config.isRnnEnabled()) {
            this.rnnController = this.serviceLocator.getService(ControllerRnn.class);
        }
        this.aimlController = this.serviceLocator.getService(ControllerAiml.class);
    }

}
