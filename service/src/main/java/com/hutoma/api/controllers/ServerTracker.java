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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Created by David MG on 01/02/2017.
 */
public class ServerTracker implements Callable {

    private static final String LOGFROM = "servertracker";
    private Config config;
    private Tools tools;
    private JerseyClient jerseyClient;
    private JsonSerializer jsonSerializer;
    private ILogger logger;

    private ServerRegistration registration;
    private long lastValidHeartbeat = 0;
    private long lastHeartbeatAttempt = 0;
    private UUID serverSessionID;
    private AtomicBoolean runFlag;

    @Inject
    public ServerTracker(final Config config, final Tools tools,
                         final JerseyClient jerseyClient,
                         final JsonSerializer jsonSerializer, final ILogger logger) {
        this.config = config;
        this.tools = tools;
        this.jerseyClient = jerseyClient;
        this.jsonSerializer = jsonSerializer;
        this.logger = logger;
        this.runFlag = new AtomicBoolean();
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

    public UUID trackServer(ServerRegistration registration) {
        this.registration = registration;
        this.serverSessionID = this.tools.createNewRandomUUID();
        return this.serverSessionID;
    }

    public void endServerSession() {
        this.runFlag.set(false);
    }

    public String getServerUrl() {
        return this.registration.getServerUrl();
    }

    protected boolean beatHeart() {
        try {
            JerseyWebTarget target = this.jerseyClient
                    .target(this.registration.getServerUrl())
                    .path("heartbeat");
            Response response = target.request()
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

    String describeServer() {
        return String.format("%s server id:%s cantrain:%d canchat:%d",
                this.registration.getServerType(), this.serverSessionID.toString(),
                this.registration.getTrainingCapacity(),
                this.registration.getChatCapacity());
    }

}
