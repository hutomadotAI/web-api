package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.DatabaseConnectionPool;
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

    private final String LOGFROM = "serverinit";

    Config config;

    @Inject
    ServiceLocator serviceLocator;

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {
        switch (applicationEvent.getType()) {
            case INITIALIZATION_FINISHED:
                initialise(applicationEvent);
                break;
            case RELOAD_FINISHED:
                if (null!=config) {
                    config.loadPropertiesFile();
                }
                break;
            case INITIALIZATION_START:
            case DESTROY_FINISHED:
            default:
                break;
        }
    }

    private void initialise(ApplicationEvent applicationEvent) {
        Logger logger = serviceLocator.getService(Logger.class);
        config = serviceLocator.getService(Config.class);
        DatabaseConnectionPool connectionPool = serviceLocator.getService(DatabaseConnectionPool.class);
        try {
            connectionPool.borrowConnection().close();
            logger.logInfo(LOGFROM, "initialisation finished");
        } catch (Exception e) {
            logger.logError(LOGFROM, "initialisation error: " + e.toString());
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null;
    }
}
