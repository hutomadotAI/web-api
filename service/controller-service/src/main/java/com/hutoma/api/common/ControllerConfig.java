package com.hutoma.api.common;

import com.hutoma.api.connectors.IConnectConfig;
import com.hutoma.api.connectors.db.IDatabaseConfig;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.thread.IThreadConfig;

import javax.inject.Inject;

public class ControllerConfig extends CommonConfig implements ILoggerConfig, IDatabaseConfig, IThreadConfig, IConnectConfig {

    private static final String LOGFROM = "controllerconfig";

    @Inject
    public ControllerConfig(final AiServiceStatusLogger logger) {
        super(logger);
    }

    /***
     * The total number of milliseconds that we wait for backend
     * non-chat commands to complete.
     * @return
     */
    @Override
    public long getBackendTrainingCallTimeoutMs() {
        return 20000;
    }

    /***
     * The total number of milliseconds that we wait for a backend connect
     * @return
     */
    @Override
    public long getBackendConnectCallTimeoutMs() {
        return 10000;
    }

    @Override
    protected String getLoggingLogfrom() {
        return "controllerconfig";
    }

    @Override
    protected String getEnvPrefix() {
        return "API_";
    }

    /***
     * Do not attempt slot recovery for the first n seconds after the API has started up
     * This gives servers enough time to re-register and reclaim their training tasks
     */
    public int getProcessQueueDelayRecoveryForFirstSeconds() {
        return 2 * 60;
    }

    /***
     * Every n milliseconds we check the queue status to see
     * if there are tasks to run or reschedule
     * @return
     */
    public long getProcessQueueIntervalDefault() {
        return 2 * 1000;
    }

    /***
     * The time to wait if a command needs to be scheduled
     * immediately after this one (in ms)
     * i.e. minimum interval between queue checks
     * @return
     */
    public long getProcessQueueIntervalShort() {
        return 1000;
    }

    /***
     * The time to wait if nothing much is going on
     * and we can wait a while before checking the queue again
     * @return
     */
    public long getProcessQueueIntervalLong() {
        return 10 * 1000;
    }

    /***
     * How far in the future to schedule a command
     * (in seconds)
     */
    public int getProcessQueueScheduleFutureCommand() {
        return 30;
    }

    /***
     * If this many seconds pass and no update is received for an active training slot
     * then we consider it 'interrupted' and reallocate the training job to a server with space
     */
    public int getProcessQueueInterruptedSeconds() {
        return 2 * 60;
    }

    /***
     * However long the last call took, always wait a minimum of n milliseconds
     * before issuing the next ping
     * i.e. if we issue a ping every 2 seconds and the ping takes 2 seconds to complete
     * we would still wait n ms between calls
     * @return n
     */
    public long getServerHeartbeatMinimumGapMs() {
        return 500;
    }

    /***
     * Under normal conditions the controller will ping the server every n milliseconds
     * @return n
     */
    public long getServerHeartbeatEveryMs() {
        return 2 * 1000;
    }

    /***
     * If we haven't received a valid ping for n milliseconds
     * then we write off this server and it has to re-register with us
     * @return n
     */
    public long getServerHeartbeatFailureCutOffMs() {
        return 5 * 1000;
    }

    @Override
    public String getFluentLoggingHost() {
        return getConfigFromProperties("logging_fluent_host", "log-fluent");
    }

    @Override
    public int getFluentLoggingPort() {
        return Integer.parseInt(getConfigFromProperties("logging_fluent_port", "24224"));
    }

    @Override
    public String getDatabaseConnectionString() {
        // if we are using admin or root, log an error and return an empty connection string
        try {
            return IDatabaseConfig.enforceNewDBCredentials(getConfigFromProperties("connection_string", ""));
        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage());
        }
        return "";
    }

    @Override
    public int getDatabaseConnectionPoolMinimumSize() {
        return Integer.parseInt(getConfigFromProperties("dbconnectionpool_min_size", "8"));
    }

    @Override
    public int getDatabaseConnectionPoolMaximumSize() {
        return Integer.parseInt(getConfigFromProperties("dbconnectionpool_max_size", "64"));
    }

    /***
     * The maximum number of active threads in the threadpool
     * after which anyone requesting a thread will get an exception
     * @return
     */
    @Override
    public int getThreadPoolMaxThreads() {
        return 1024;
    }

    /***
     * The time after which an idle thread in the thread pool get be closed
     * @return
     */
    @Override
    public long getThreadPoolIdleTimeMs() {
        return 60 * 1000;
    }
}
