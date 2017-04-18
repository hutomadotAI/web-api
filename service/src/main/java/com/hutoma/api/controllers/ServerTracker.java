package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
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
    private final Config config;
    private final Tools tools;
    private final JerseyClient jerseyClient;
    private final JsonSerializer jsonSerializer;
    private final ILogger logger;
    protected AtomicBoolean runFlag;
    protected UUID serverSessionID;
    protected AtomicBoolean endpointVerified;
    private ServerRegistration registration;
    private long lastValidHeartbeat = 0;
    private long lastHeartbeatAttempt = 0;
    private String serverIdentity = "(uninitialised)";

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

    /***
     * Uniquely identifies a server across sessions.
     * @return
     */
    public String getServerIdentifier() {
        return this.serverIdentity;
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
    public UUID trackServer(ServerRegistration registration) {
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

    public UUID getSessionID() {
        return this.serverSessionID;
    }

    private synchronized int getChatAffinityCount() {
        return this.affinity.size();
    }

    /***
     * Make a heartbeat call to the server
     * @return true for success, false otherwise
     */
    protected boolean beatHeart() {
        LogMap logMap = LogMap.map("Op", "heartbeat")
                .put("Url", this.registration.getServerUrl())
                .put("Type", this.registration.getServerType())
                .put("Server", this.serverIdentity)
                .put("SessionId", this.serverSessionID);
        try {
            JerseyWebTarget target = this.jerseyClient
                    .target(this.registration.getServerUrl())
                    .path("heartbeat");

            Response response = target
                    .property(CONNECT_TIMEOUT, (int) this.config.getServerHeartbeatEveryMs())
                    .request()
                    .post(Entity.json(this.jsonSerializer.serialize(new ApiServerAcknowledge(this.serverSessionID))));
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                this.logger.logDebug(LOGFROM,
                        String.format("heartbeat ping to %s succeeded", this.serverIdentity),
                        logMap.put("Status", "success"));
                return true;
            }

            this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s failed with error %d",
                    this.serverIdentity, response.getStatus()), logMap.put("Status", response.getStatus()));
        } catch (Exception e) {
            this.logger.logWarning(LOGFROM, String.format("heartbeat ping to %s failed with error %s",
                    this.serverIdentity, e.toString()), logMap.put("Status", e.toString()));
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
