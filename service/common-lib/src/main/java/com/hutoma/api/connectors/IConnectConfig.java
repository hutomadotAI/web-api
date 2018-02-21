package com.hutoma.api.connectors;

public interface IConnectConfig {

    /***
     * The total number of milliseconds that we wait for backend
     * non-chat commands to complete.
     * @return
     */
    long getBackendTrainingCallTimeoutMs();

    /***
     * The total number of milliseconds that we wait for a backend connect
     * @return
     */
    long getBackendConnectCallTimeoutMs();
}
