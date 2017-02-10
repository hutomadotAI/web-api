package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

/***
 * Layer below controllerbase.
 * This keeps track of affinity and load for each
 * connected server.
 */
public class ServerMetadata {

    private static final String LOGFROM = "servermeta";
    private final HashMap<UUID, ServerTracker> activeServerSessions;
    private final HashMap<UUID, LinkedHashSet<ServerTracker>> serverAiAffinity;
    protected Config config;
    protected Tools tools;
    protected ServiceLocator serviceLocator;
    protected ILogger logger;
    private int roundRobinIndex;

    public ServerMetadata(final ILogger logger, final Config config, final Tools tools, final ServiceLocator serviceLocator) {
        this.logger = logger;
        this.config = config;
        this.tools = tools;
        this.serviceLocator = serviceLocator;
        this.activeServerSessions = new HashMap<>();
        this.serverAiAffinity = new HashMap<>();
        this.roundRobinIndex = 0;
    }

    /***
     * Process an affinity update request.
     * Deletes all affinity for this server
     * and then generates new affinity according to the aiid list
     * i.e. each update obliterates whataver was there before
     * @param sessionID sessionID
     * @param aiidList list of servers
     * @return true if the session exists for this server type
     */
    public synchronized boolean updateAffinity(UUID sessionID, Collection<UUID> aiidList) {
        ServerTracker tracker = this.activeServerSessions.get(sessionID);
        if (tracker == null) {
            return false;
        }
        clearAffinityForServer(tracker);
        createAffinityForServer(tracker, aiidList);
        return true;
    }

    /***
     * Associate a list of AIs with a server
     * @param tracker
     * @param aiidList
     */
    private synchronized void createAffinityForServer(
            final ServerTracker tracker, final Collection<UUID> aiidList) {
        aiidList.forEach(aiid -> addAffinity(tracker, aiid));
    }

    /***
     * Associate this AI with this server
     * @param tracker
     * @param aiid
     */
    private synchronized void addAffinity(final ServerTracker tracker, final UUID aiid) {
        // add to the affinity table
        LinkedHashSet<ServerTracker> serverList = this.serverAiAffinity.get(aiid);
        if (serverList == null) {
            serverList = new LinkedHashSet<>();
            this.serverAiAffinity.put(aiid, serverList);
        }
        serverList.add(tracker);
        // add to the server's AI affinity list
        tracker.addChatAffinityEntry(aiid);
    }

    /***
     * If nobody is servicing this AIID then we have to pick the best server to assign it to
     * and then add this server-aiid pair to the affinity table
     * @param aiid
     * @return
     * @throws NoServerAvailable
     */
    private synchronized ServerTracker chooseServerToAssignAffinity(final UUID aiid) throws NoServerAvailable {

        String routePickReason;

        // remove the ones that have no chat capacity at all
        // and remove the ones where ping hasn't succeeded yet
        // map into a list of pairs of (ChatCapacity, Server)
        List<Pair<Double, ServerTracker>> candidates
                = this.activeServerSessions.values().stream()
                .filter(server -> server.isEndpointVerified())
                .map(server -> new Pair<>(server.getChatLoadFactor(), server))
                .filter(pair -> !Double.isNaN(pair.getA()))
                .collect(toList());

        // if there are no servers, bail now
        if (candidates.isEmpty()) {
            throw new NoServerAvailable();
        }

        // if anyone is below capacity, find the lowest one
        ServerTracker pick = candidates.stream()
                .filter(x -> x.getA() < 0.999d)
                .min(Comparator.comparingDouble(Pair::getA))
                .map(Pair::getB)
                .orElse(null);

        // if nobody is below capacity
        if (pick == null) {

            // exclude any server that is *over* capacity
            // i.e. find the servers that are exactly at capacity
            List<ServerTracker> randomSelectFrom = candidates.stream()
                    .filter(x -> x.getA() < 1.001d)
                    .map(Pair::getB)
                    .collect(toList());

            // if all the servers are over capacity then just get a list
            if (randomSelectFrom.isEmpty()) {
                routePickReason = "over-cap-round-robin";
                randomSelectFrom = candidates.stream()
                        .map(Pair::getB)
                        .collect(toList());
            } else {
                routePickReason = "cap-round-robin";
            }

            // now pick a server from whatever we have available
            this.roundRobinIndex %= randomSelectFrom.size();
            pick = randomSelectFrom.get(this.roundRobinIndex++);

        } else {
            routePickReason = "free-slots";
        }

        this.logger.logDebug(LOGFROM, String.format("Routing to %s because %s", pick.describeServerRouting(), routePickReason));
        addAffinity(pick, aiid);
        return pick;
    }

    /***
     * Finds the server (if any) that was last used for this AI
     * @param aiid
     * @return a servertracker, or null
     */
    private synchronized ServerTracker existingAffinity(final UUID aiid) {
        LinkedHashSet<ServerTracker> affinityList = this.serverAiAffinity.get(aiid);
        if (affinityList == null) {
            return null;
        }
        // find the first server in the affinity list
        // that has been verified
        return affinityList.stream()
                .filter(server -> server.isEndpointVerified())
                .findFirst().orElse(null);
    }

    /***
     * Clear all affinity to this server
     * @param targetServer
     */
    private synchronized void clearAffinityForServer(final ServerTracker targetServer) {
        targetServer.getChatAffinity().stream()
                .map(aiid -> this.serverAiAffinity.get(aiid))
                .forEach(listOfSessions -> listOfSessions.remove(targetServer));
        targetServer.clearChatAffinity();
    }

    /***
     * When the server list is empty
     * or the server does not support what we are trying to do
     * e.g. chatcapacity=0 and we are trying to chat
     */
    public static class NoServerAvailable extends Exception {
        public NoServerAvailable() {
            super("No server available to process this request");
        }
    }

    /***
     * Adds a newly created session to the list of available servers
     * @param serverSessionID
     * @param tracker
     */
    protected synchronized void addNewSession(UUID serverSessionID, ServerTracker tracker) {
        // check any other valid sessions
        this.activeServerSessions.values().stream()
                // to see if any of them have the same callback URL
                .filter(oldTracker -> oldTracker.getServerUrl().equals(tracker.getServerUrl()))
                // and if they do, terminate them.
                .forEach(ServerTracker::endServerSession);

        // add this session to the list
        this.activeServerSessions.put(serverSessionID, tracker);
    }

    /***
     * Removes a session from the list of available servers and
     * clears any affinity that was linked to that session
     * @param serverSessionID
     */
    protected synchronized void deleteSession(final UUID serverSessionID) {
        ServerTracker removedServer = this.activeServerSessions.remove(serverSessionID);
        clearAffinityForServer(removedServer);
    }

    /***
     * Gets a server to service a request
     * The server that processed the aiid last would get it.
     * If no server processed it last then a new one is assigned.
     * @param aiid
     * @return
     * @throws NoServerAvailable
     */
    protected synchronized ServerTracker getServerFor(UUID aiid) throws NoServerAvailable {
        ServerTracker server = existingAffinity(aiid);
        if (server == null) {
            server = chooseServerToAssignAffinity(aiid);
        }
        return server;
    }

    protected ServerTracker createNewServerTracker() {
        return this.serviceLocator.getService(ServerTracker.class);
    }
}
