package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.ServerRegistration;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;


/**
 * Created by David MG on 01/02/2017.
 */
public class ServerTracker implements Callable {

    private static final String LOGFROM = "servertracker";
    private final HashSet<UUID> affinity;
    protected AtomicBoolean runFlag;
    protected UUID serverSessionID;
    protected AtomicBoolean endpointVerified;

    private Config config;
    private Tools tools;
    private JerseyClient jerseyClient;
    private JsonSerializer jsonSerializer;
    private ILogger logger;
    private ServerRegistration registration;
    private long lastValidHeartbeat = 0;
    private long lastHeartbeatAttempt = 0;

    @Inject
    public ServerTracker(final Config config, final Tools tools,
                         final JerseyClient jerseyClient,
                         final JsonSerializer jsonSerializer, final ILogger logger) {
        this.config = config;
        this.tools = tools;
        this.jsonSerializer = jsonSerializer;
        this.logger = logger;

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
    public double getChatLoadFactor() {
        int chatCapacity = this.registration.getChatCapacity();
        if (chatCapacity < 1) {
            return Double.NaN;
        }
        return ((double) getChatAffinityCount()) / ((double) chatCapacity);
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
                        this.endpointVerified.set(true);

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
        this.logger.logDebug(LOGFROM, String.format("ending session %s for %s server",
                this.serverSessionID.toString(), this.registration.getServerType()));
        return null;
    }

    /***
     * Initialisation step.
     * @param registration registration data
     * @return a new session ID to represent this server
     */
    public UUID trackServer(ServerRegistration registration) {
        this.registration = registration;
        this.serverSessionID = this.tools.createNewRandomUUID();
        return this.serverSessionID;
    }

    /***
     * Flag the tracker to stop the heartbeat and terminate.
     * This causes the session to be dropped
     */
    public void endServerSession() {
        this.runFlag.set(false);
    }

    /***
     * The endpoint for this server
     * @return
     */
    public String getServerUrl() {
        return this.registration.getServerUrl();
    }

    public synchronized Set<UUID> getChatAffinity() {
        return new HashSet<>(this.affinity);
    }

    public synchronized void addChatAffinityEntry(UUID aiid) {
        this.affinity.add(aiid);
    }

    public synchronized void clearChatAffinity() {
        this.affinity.clear();
    }

    private synchronized int getChatAffinityCount() {
        return this.affinity.size();
    }

    /***
     * Make a heartbeat call to the server
     * @return true for success, false otherwise
     */
    protected boolean beatHeart() {
        try {
            JerseyWebTarget target = this.jerseyClient
                    .target(this.registration.getServerUrl())
                    .path("heartbeat");

            Response response = target
                    .property(CONNECT_TIMEOUT, (int) this.config.getServerHeartbeatEveryMs())
                    .request()
                    .post(Entity.json(this.jsonSerializer.serialize(new ApiServerAcknowledge(this.serverSessionID))));
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            
            this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s %s failed with error %d",
                    this.registration.getServerType(), this.serverSessionID.toString(), response.getStatus()));
        } catch (Exception e) {
            this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s %s failed with error %s",
                    this.registration.getServerType(), this.serverSessionID.toString(), e.toString()));
        }
        return false;
    }

    /***
     * Textual description of this tracker
     * @return
     */
    String describeServer() {
        return String.format("%s server id:%s cantrain:%d canchat:%d",
                this.registration.getServerType(), this.serverSessionID.toString(),
                this.registration.getTrainingCapacity(),
                this.registration.getChatCapacity());
    }

    /***
     * Description of this tracker for routing
     * @return
     */
    String describeServerRouting() {
        return String.format("%s %s cantrain:%d canchat:%d",
                this.registration.getServerType(), getServerUrl(),
                this.registration.getTrainingCapacity(),
                this.registration.getChatCapacity());
    }
}
