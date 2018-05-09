package com.hutoma.api.controllers;

import com.hutoma.api.connectors.BackendServerType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ControllerMap {

    private final Map<BackendServerType, ControllerBase> controllerMap = new HashMap<>();

    @Inject
    public ControllerMap(final ControllerAiml controllerAiml,
                         final ControllerEmb controllerEmb) {
        controllerMap.put(BackendServerType.AIML, controllerAiml);
        controllerMap.put(BackendServerType.EMB, controllerEmb);
    }

    public ControllerBase getControllerFor(final BackendServerType server) {
        if (!controllerMap.containsKey(server)) {
            return null;
        }
        return controllerMap.get(server);
    }

    public BackendServerType updateAffinity(final UUID sid, final List<UUID> aiList) {
        BackendServerType updated = null;
        if (getControllerFor(BackendServerType.AIML).updateAffinity(sid, aiList)) {
            updated = BackendServerType.AIML;
        } else if (getControllerFor(BackendServerType.EMB).updateAffinity(sid, aiList)) {
            updated = BackendServerType.EMB;
        }
        return updated;
    }

    public void terminateQueues() {
        for (ControllerBase controller: controllerMap.values()) {
            controller.terminateQueue();
        }
    }
}
