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
    public ControllerMap(final ControllerWnet controllerWnet,
                         final ControllerAiml controllerAiml,
                         final ControllerSvm controllerSvm,
                         final ControllerEmb controllerEmb) {
        controllerMap.put(BackendServerType.AIML, controllerAiml);
        controllerMap.put(BackendServerType.WNET, controllerWnet);
        controllerMap.put(BackendServerType.SVM, controllerSvm);
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
        if (getControllerFor(BackendServerType.WNET).updateAffinity(sid, aiList)) {
            updated = BackendServerType.WNET;
        } else if (getControllerFor(BackendServerType.AIML).updateAffinity(sid, aiList)) {
            updated = BackendServerType.AIML;
        } else if (getControllerFor(BackendServerType.SVM).updateAffinity(sid, aiList)) {
            updated = BackendServerType.SVM;
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
