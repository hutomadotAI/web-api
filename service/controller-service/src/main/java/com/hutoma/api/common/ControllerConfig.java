package com.hutoma.api.common;

import com.hutoma.api.logging.ILogger;

import javax.inject.Inject;

public class ControllerConfig extends CommonConfig {

    @Inject
    public ControllerConfig(final ILogger logger) {
        super(logger);
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
}
