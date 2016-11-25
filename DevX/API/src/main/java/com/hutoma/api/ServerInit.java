package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;

import org.glassfish.hk2.api.ServiceLocator;
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

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {
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

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null;
    }

    private void initialise(ApplicationEvent applicationEvent) {
        ILogger logger = this.serviceLocator.getService(ILogger.class);
        this.config = this.serviceLocator.getService(Config.class);
        try {
            this.config.validateConfigPresent();
            logger.initialize(this.config);
            DatabaseConnectionPool connectionPool = this.serviceLocator.getService(DatabaseConnectionPool.class);
            connectionPool.borrowConnection().close();
            logger.logInfo(LOGFROM, "initialisation finished");
        } catch (Exception e) {
            logger.logError(LOGFROM, "initialisation error: " + e.toString());
        }
    }
}
