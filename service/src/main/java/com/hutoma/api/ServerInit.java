package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;

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
    ServiceLocator serviceLocator;
    private Config config;
    private ILogger logger;

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
            case RELOAD_FINISHED:
                if (null != this.config) {
                    this.config.loadPropertiesFile();
                }
                break;
            case INITIALIZATION_START:
            case DESTROY_FINISHED:
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
                        final long finishTime = System.currentTimeMillis();
                        Invocable invocable = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable();
                        LogMap logMap = LogMap.map("Start", this.startTime)
                                .put("Finish", finishTime)
                                .put("Duration", finishTime - this.startTime)
                                .put("Success", requestEvent.isSuccess())
                                .put("Class", invocable.getDefinitionMethod().getDeclaringClass().getSimpleName())
                                .put("Method", invocable.getDefinitionMethod().getName());
                        ServerInit.this.logger.logPerf("perfrequestlistener", "APICall", logMap);
                        break;
                    default: // empty
                        break;
                }
            }
        };
    }

    private void initialise(final ApplicationEvent applicationEvent) {
        this.logger = this.serviceLocator.getService(ILogger.class);
        this.config = this.serviceLocator.getService(Config.class);
        try {
            this.config.validateConfigPresent();
            this.logger.initialize(this.config);
            DatabaseConnectionPool connectionPool = this.serviceLocator.getService(DatabaseConnectionPool.class);
            connectionPool.borrowConnection().close();
            this.logger.logInfo(LOGFROM, "initialisation finished");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "initialisation error: " + e.toString());
        }

        // create the singleton instances so that the timers start
        // and with them the server monitoring
        this.serviceLocator.getService(ControllerWnet.class);
        this.serviceLocator.getService(ControllerRnn.class);
        this.serviceLocator.getService(ControllerAiml.class);
    }
}
