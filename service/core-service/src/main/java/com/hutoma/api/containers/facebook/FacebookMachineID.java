package com.hutoma.api.containers.facebook;

import java.util.concurrent.atomic.AtomicReference;

public class FacebookMachineID {

    private AtomicReference<String> machineId = new AtomicReference<>(null);

    public String getMachineId() {
        return this.machineId.get();
    }

    public boolean setMachineId(String newId) {
        return this.machineId.compareAndSet(null, newId);
    }
}
