package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logging.CentralLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.ThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public class ServerTracker implements Callable, IServerEndpoint {

    private static final String LOGFROM = "servertracker";
    private final HashSet<UUID> affinity;
    private final ControllerConfig config;
    private final Tools tools;
    private final JerseyClient jerseyClient;
    private final JsonSerializer jsonSerializer;
    private final ILogger logger;
    private final ThreadSubPool threadSubPool;
    private long lastValidHeartbeat = 0;
    private long lastHeartbeatAttempt = 0;
    private String serverIdentity = "(uninitialised)";
    private ServerRegistration registration;

    AtomicBoolean runFlag;
    UUID serverSessionID;
    AtomicBoolean endpointVerified;

    @Inject
    public ServerTracker(final ControllerConfig config, final Tools tools,
                         final JerseyClient jerseyClient,
                         final JsonSerializer jsonSerializer, final ILogger logger,
                         final ThreadSubPool threadSubPool) {
        this.config = config;
        this.tools = tools;
        this.jsonSerializer = jsonSerializer;
        this.logger = logger;
        this.threadSubPool = threadSubPool;

        this.runFlag = new AtomicBoolean();
        this.endpointVerified = new AtomicBoolean(false);
        this.affinity = new HashSet<>();

        this.jerseyClient = jerseyClient;
    }

    /***
     * How loaded is this server in relation to its capacity?
     * @return [0.0, 1.0] affinity divided by chat capacity,
     * or NaN if there is no chat capacity at all
     */
    double getChatLoadFactor() {
        int chatCapacity = this.registration.getChatCapacity();
        if (chatCapacity < 1) {
            return Double.NaN;
        }
        return ((double) getChatAffinityCount()) / ((double) chatCapacity);
    }

    /***
     * Does this server support training or is it chat only?
     * @return
     */
    public boolean canTrain() {
        return this.registration.getTrainingCapacity() > 0;
    }

    /***
     * How many training slots does this server have?
     * @return
     */
    public int getTrainingCapacity() {
        return this.registration.getTrainingCapacity();
    }

    /***
     * True if we have managed at least one ping,
     * therefore the path to the server is correct and the server is reachable
     * @return
     */
    public boolean isEndpointVerified() {
        return this.endpointVerified.get();
    }

    @Override
    public Void call() {

        try {
            // start in run state
            this.runFlag.set(true);

            while (this.runFlag.get()) {
                // how long since we last pinged?
                long timeSinceLastHeartBeatAttempt = this.tools.getTimestamp() - this.lastHeartbeatAttempt;

                // how long should we wait before issuing the next ping?
                long waitTimeForNextHeartbeat = Math.max(this.config.getServerHeartbeatMinimumGapMs(),
                        this.config.getServerHeartbeatEveryMs() - timeSinceLastHeartBeatAttempt);

                try {
                    // wait until it is time
                    this.tools.threadSleep(waitTimeForNextHeartbeat);

                    // we've just come out of sleep; check the run flag again
                    if (this.runFlag.get()) {

                        // log the start of this attempt
                        this.lastHeartbeatAttempt = this.tools.getTimestamp();

                        // ping now
                        boolean success = beatHeart();

                        // note the time of the response
                        long timeNow = this.tools.getTimestamp();

                        // if it worked then everything is ok
                        if (success) {
                            // and we reset the time that we got a valid response to now
                            this.lastValidHeartbeat = timeNow;

                            // if the heartbeat worked then the endpoint is verified
                            if (this.endpointVerified.compareAndSet(false, true)) {
                                this.logger.logDebug(LOGFROM, String.format("endpoint verified for session %s on %s",
                                        this.serverSessionID.toString(), this.serverIdentity));
                            }

                        } else {

                            // if it failed, check how long ago we got the last good ping
                            if ((timeNow - this.lastValidHeartbeat) > this.config.getServerHeartbeatFailureCutOffMs()) {

                                // flag the outer loop to end
                                this.runFlag.set(false);
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    this.logger.logError(LOGFROM, "heartbeat interval wait interrupted");
                    // just go round again and recalculate the wait time
                }
            }
            this.logger.logDebug(LOGFROM, String.format("ending session %s on %s",
                    this.serverSessionID.toString(), this.serverIdentity));
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM,
                    String.format("ServerTracker exception on %s tracker", this.serverIdentity), "", e);
        }
        return null;
    }

    /***
     * Initialisation step.
     * @param registration registration data
     * @return a new session ID to represent this server
     */
    UUID trackServer(final ServerRegistration registration) {
        this.registration = registration;
        this.serverIdentity = String.format("%s@%s",
                this.registration.getServerType().value(),
                this.registration.getServerUrl().trim());
        this.serverSessionID = this.tools.createNewRandomUUID();
        return this.serverSessionID;
    }

    /***
     * Flag the tracker to stop the heartbeat and terminate.
     * This causes the session to be dropped
     */
    void endServerSession() {
        this.runFlag.set(false);
    }

    /***
     * If this session has already been flagged to end (endServerSession above) because it has dropped offline
     * or a new one has connected using the same callback URL
     * then we need to filter it out from lists and not consider it a usable server
     * @return false if the session is about to end
     */
    boolean isSessionNotEnding() {
        return this.runFlag.get();
    }

    /***
     * The endpoint for this server
     * @return
     */
    @Override
    public String getServerUrl() {
        return this.registration.getServerUrl();
    }

    /***
     * Uniquely identifies a server across sessions.
     * @return
     */
    @Override
    public String getServerIdentifier() {
        return this.serverIdentity;
    }

    synchronized Set<UUID> getChatAffinity() {
        return new HashSet<>(this.affinity);
    }

    synchronized void addChatAffinityEntry(UUID aiid) {
        this.affinity.add(aiid);
    }

    synchronized void clearChatAffinity() {
        this.affinity.clear();
    }

    synchronized void removeChatAffinity(UUID aiid) {
        this.affinity.remove(aiid);
    }

    public UUID getSessionID() {
        return this.serverSessionID;
    }

    /***
     * Return the number of chat slots that this server can support
     * @return
     */
    public int getChatCapacity() {
        return this.registration.getChatCapacity();
    }

    private synchronized int getChatAffinityCount() {
        return this.affinity.size();
    }

    /***
     * Thread wrapper for the actually http call to send a heartbeat to the back-end server
     * The web-request and clean-up are dealt with in a separate thread
     * because the call can take up to 15 minutes regardless of timeout settings
     * and this halts the servertracker thread, keeping the server alive artifically
     */
    private static class HeartbeatThreadWrapper implements Callable<Integer> {

        // http call builder with everything already set except for the entity
        private final JerseyInvocation.Builder builder;

        // entity to send with the post call
        private final Entity entity;

        HeartbeatThreadWrapper(final JerseyInvocation.Builder builder, final Entity entity) {
            this.builder = builder;
            this.entity = entity;
        }

        @Override
        public Integer call() throws Exception {
            Response response = null;
            try {
                // make the call
                response = this.builder.post(this.entity);
                // pull out the payload data to avoid memory leaks
                response.bufferEntity();
                // return only the status code
                return response.getStatus();
            } finally {
                // if we had a valid response object then close it
                if (response != null) {
                    response.close();
                }
            }
        }
    }

    /***
     * Make a heartbeat call to the server
     * @return true for success, false otherwise
     */
    protected boolean beatHeart() {
        LogMap logMap = LogMap.map("Op", "heartbeat")
                .put("Url", this.registration.getServerUrl())
                .put("Type", this.registration.getServerType().value())
                .put("Server", this.serverIdentity)
                .put("SessionId", this.serverSessionID.toString());
        try {
            JerseyWebTarget target = this.jerseyClient
                    .target(this.registration.getServerUrl())
                    .path("heartbeat");

            HeartbeatThreadWrapper heartbeatWrapper = new HeartbeatThreadWrapper(target
                    .property(CONNECT_TIMEOUT, (int) this.config.getServerHeartbeatFailureCutOffMs())
                    .property(READ_TIMEOUT, (int) this.config.getServerHeartbeatFailureCutOffMs())
                    .request(),
                    Entity.json(this.jsonSerializer.serialize(new ApiServerAcknowledge(this.serverSessionID))));

            // start the heartbeat call on a separate thread
            Future<Integer> futureResponse = this.threadSubPool.submit(heartbeatWrapper);

            // get the status code result of the heartbeat call
            // but only wait a specified amount of time before failing with a TimeoutException
            int responseStatus = futureResponse.get(this.config.getServerHeartbeatFailureCutOffMs(),
                    TimeUnit.MILLISECONDS);

            // re-check session to see if it was closed while this heartbeat was in progress
            // if so, just bail out here
            if (this.runFlag.get()) {

                if (responseStatus == HttpURLConnection.HTTP_OK) {
                    this.logger.logDebug(LOGFROM,
                            String.format("heartbeat ping to %s succeeded", this.serverIdentity),
                            logMap.put("Status", "success"));
                    return true;
                }

                // bad request from a heartbeat means that the backend server has closed the session
                if (responseStatus == HttpURLConnection.HTTP_BAD_REQUEST) {
                    this.endServerSession();
                    this.logger.logWarning(LOGFROM,
                            String.format("%s has closed the session remotely", this.serverIdentity),
                            logMap.put("Status", Integer.toString(responseStatus)));
                } else {
                    this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s failed with error %d",
                            this.serverIdentity, responseStatus), logMap.put("Status",
                            Integer.toString(responseStatus)));
                }
            }
        } catch (Exception e) {
            this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s failed with error %s",
                    this.serverIdentity, e.toString()),
                    logMap.put("Status", e.getMessage())
                            .put("Stack trace", CentralLogger.getStackTraceAsString(e.getStackTrace())));

        }
        return false;
    }

    /***
     * Textual description of this tracker
     * @return
     */
    String describeServer() {
        return String.format("%s cantrain:%d canchat:%d",
                this.serverIdentity,
                this.registration.getTrainingCapacity(),
                this.registration.getChatCapacity());
    }

}
