package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.logging.ILogger;
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

    private ILogger logger;
    private ServerMonitor serverMonitor;

    /**
     * Application event handler.
     *
     * @param applicationEvent the application event
     */
    @Override
    public void onEvent(final ApplicationEvent applicationEvent) {
        switch (applicationEvent.getType()) {
            case INITIALIZATION_FINISHED:
                initialise();
                break;
            case DESTROY_FINISHED:
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
     *
     * @param requestEvent the request event
     * @return the request event listener
     */
    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return new RequestEventListener() {
            private long startTime;
            private long startNanoTime;

            @Override
            public void onEvent(final RequestEvent requestEvent) {
                switch (requestEvent.getType()) {
                    case RESOURCE_METHOD_START:
                        this.startTime = System.currentTimeMillis();
                        this.startNanoTime = System.nanoTime();
                        break;
                    case FINISHED:
                        // Only log if we have a method to handle it
                        if (requestEvent.getUriInfo().getMatchedResourceMethod() != null) {
                            final long finishTime = System.currentTimeMillis();
                            final long durationNano = System.nanoTime() - this.startNanoTime;
                            Invocable invocable = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable();
                            LogMap logMap = LogMap.map("Start", this.startTime)
                                    .put("Finish", finishTime)
                                    .put("Duration", durationNano / 1000000.0)
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

    private void initialise() {
        this.logger = this.serviceLocator.getService(ILogger.class);
        Config config = this.serviceLocator.getService(Config.class);
        try {
            config.validateConfigPresent();
            this.logger.initialize(config);
            DatabaseConnectionPool connectionPool = this.serviceLocator.getService(DatabaseConnectionPool.class);
            connectionPool.borrowConnection().close();

            // start the server monitor to check status every few seconds
            this.serverMonitor = this.serviceLocator.getService(ServerMonitor.class);
            this.serverMonitor.initialise();

            this.logger.logInfo(LOGFROM, "initialisation finished");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "initialisation error: " + e.toString());
        }
    }

}
