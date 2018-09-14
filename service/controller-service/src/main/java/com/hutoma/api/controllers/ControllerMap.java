package com.hutoma.api.controllers;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ControllerMap {

    private static final String LOGFROM = "controllermap";

    private final Map<Integer, ControllerBase> controllerMap = new HashMap<>();

    private final Provider<ControllerGeneric> controllerProvider;
    private final ControllerAiml controllerAiml;
    private final ILogger logger;

    @Inject
    public ControllerMap(final ControllerAiml controllerAiml,
                         final Provider<ControllerGeneric> controllerProvider,
                         final ILogger logger) {
        this.controllerProvider = controllerProvider;
        this.logger = logger;
        this.controllerAiml = controllerAiml;
    }

    public ControllerBase getControllerFor(final ServiceIdentity server) {
        if (server.getServerType() == BackendServerType.AIML) {
            return this.controllerAiml;
        }

        return getOrCreateController(server);
    }

    public BackendServerType updateAffinity(final UUID sid,
                                            final SupportedLanguage language,
                                            final String version,
                                            final List<UUID> aiList) {
        BackendServerType updated = null;
        if (getControllerFor(ServiceIdentity.getAimlIdent()).updateAffinity(sid, aiList)) {
            updated = BackendServerType.AIML;
        } else if (getControllerFor(new ServiceIdentity(BackendServerType.EMB, language, version))
                .updateAffinity(sid, aiList)) {
            updated = BackendServerType.EMB;
        }
        return updated;
    }

    public void terminateQueues() {
        for (ControllerBase controller : controllerMap.values()) {
            controller.terminateQueue();
        }
    }


    /**
     * Gets an existing controller or creates a new one if it doesn't exist.
     *
     * @param serviceIdentity server identity
     * @return the controller
     */
    private synchronized ControllerBase getOrCreateController(final ServiceIdentity serviceIdentity) {
        if (serviceIdentity.getLanguage() == null) {
            serviceIdentity.setLanguage(SupportedLanguage.EN);
        }
        if (serviceIdentity.getVersion() == null || serviceIdentity.getVersion().isEmpty()) {
            serviceIdentity.setVersion(ServiceIdentity.DEFAULT_VERSION);
        }
        if (!this.controllerMap.containsKey(serviceIdentity.hashCode())) {
            ControllerBase controller = this.controllerProvider.get();
            controller.initialize(serviceIdentity);
            this.logger.logInfo(LOGFROM,
                    String.format("Creating new controller of type %s (lang=%s v=%s)",
                            serviceIdentity.getServerType().value(),
                            serviceIdentity.getLanguage().toString(),
                            serviceIdentity.getVersion()),
                    LogMap.map("Type", serviceIdentity.getServerType().value())
                            .put("Language", serviceIdentity.getLanguage().toString())
                            .put("Version", serviceIdentity.getVersion()));
            this.controllerMap.put(serviceIdentity.hashCode(), controller);
            return controller;
        } else {
            return this.controllerMap.get(serviceIdentity.hashCode());
        }
    }
}
