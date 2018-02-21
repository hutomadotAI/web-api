package com.hutoma.api.controllers;

import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/***
 * Layer below controllerbase.
 * This keeps track of affinity and load for each
 * connected server.
 */
public class ServerMetadata {

    private static final String LOGFROM = "servermeta";
    private final LinkedHashMap<UUID, ServerTracker> activeServerSessions;
    private final HashMap<UUID, LinkedHashSet<ServerTracker>> serverAiAffinity;
    protected ILogger logger;
    private int roundRobinIndex;

    ServerMetadata(final AiServiceStatusLogger logger) {
        this.logger = logger;
        this.activeServerSessions = new LinkedHashMap<>();
        this.serverAiAffinity = new HashMap<>();
        this.roundRobinIndex = 0;
    }

    /***
     * Check whether the sessionID matches up to a valid, active session
     * @param sessionID
     * @return t/f
     */
    public synchronized boolean isActiveSession(UUID sessionID) {
        ServerTracker tracker = this.activeServerSessions.get(sessionID);
        return (tracker != null) && tracker.isSessionNotEnding();
    }

    /***
     * Returns a descriptive string, unique to the endpoint
     * that persists across sessions
     * in the format servertype@endpoint
     * e.g. wnet@http://ai-wnet:8080/ai
     * @param sessionID
     * @return the endpoint string
     */
    public synchronized String getSessionServerIdentifier(UUID sessionID) {
        ServerTracker tracker = this.activeServerSessions.get(sessionID);
        return (tracker == null) ? null : tracker.getServerIdentifier();
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
     * Check whether the sessionID matches a server session
     * that is currently tagged as the primary master,
     * meaning that it is the earliest connected server
     * with training_capacity more than 0
     * @param serverSessionID sessionId of the connected server
     * @return t/f
     */
    public boolean isPrimaryMaster(UUID serverSessionID) {
        Optional<ServerTracker> primary = getPrimaryMaster();
        // is the current master what we just added?
        return primary.isPresent()
                && primary.get().getSessionID().equals(serverSessionID);
    }

    /***
     * Get the server identifier for the current primary master
     * for display and logging purposes
     * @return the server identifier
     */
    public String getPrimaryMasterIdentifier() {
        Optional<ServerTracker> primary = getPrimaryMaster();
        return primary.isPresent() ? primary.get().getServerIdentifier() : "";
    }

    /***
     * Determine who is the primary master (if any)
     * @return the primary master (if any)
     */
    private synchronized Optional<ServerTracker> getPrimaryMaster() {
        // lookup the current master server
        return this.activeServerSessions.values().stream()
                .filter(ServerTracker::isSessionNotEnding)
                .filter(ServerTracker::canTrain)
                .findFirst();
    }

    /***
     * Gets a map of active, verified servers that are training-capable
     * @return map of verified services
     */
    public synchronized Map<String, ServerTracker> getVerifiedEndpointMap() {
        return this.activeServerSessions.values().stream()
                .filter(ServerTracker::isEndpointVerified)
                .filter(ServerTracker::isSessionNotEnding)
                .collect(Collectors.toMap(ServerTracker::getServerIdentifier, Function.identity()));
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
                .filter(ServerTracker::isEndpointVerified)
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
     * Clear one affinity from this server
     * @param targetServer
     */
    private synchronized void clearSingleAffinityForServer(final ServerTracker targetServer, final UUID aiid) {

        // take the aiid off the server's affinity list
        targetServer.removeChatAffinity(aiid);

        // in the affinity table, clear all the entries for this particular aiid
        LinkedHashSet<ServerTracker> affinityList = this.serverAiAffinity.get(aiid);
        if (affinityList != null) {
            affinityList.clear();
        }
    }

    /***
     * Gets the first connected
     * @param aiid
     * @return the server tracker
     * @throws NoServerAvailableException
     */
    protected synchronized ServerTracker getServerForUpload(final UUID aiid) throws NoServerAvailableException {
        return this.activeServerSessions.values().stream()
                .filter(ServerTracker::isSessionNotEnding)
                .filter(ServerTracker::canTrain)
                .findFirst()
                .orElseThrow(NoServerAvailableException::new);
    }

    /***
     * If nobody is servicing this AIID then we have to pick the best server to assign it to
     * and then add this server-aiid pair to the affinity table
     * @param aiid
     * @param alreadyTried
     * @return the server tracker
     * @throws NoServerAvailableException
     */
    protected synchronized ServerTracker chooseServerToAssignAffinity(final UUID aiid,
                                                                      final Set<String> alreadyTried)
            throws NoServerAvailableException {

        String routePickReason;

        // remove the ones that have no chat capacity at all
        // and remove the ones where ping hasn't succeeded yet
        // and remove the ones that have already been tried and were too busy
        // map into a list of pairs of (ChatCapacity, Server)
        List<Pair<Double, ServerTracker>> candidates
                = this.activeServerSessions.values().stream()
                .filter(ServerTracker::isSessionNotEnding)
                .filter(ServerTracker::isEndpointVerified)
                .filter(tracker -> !alreadyTried.contains(tracker.getServerIdentifier()))
                .map(server -> new Pair<>(server.getChatLoadFactor(), server))
                .filter(pair -> !Double.isNaN(pair.getA()))
                .collect(toList());

        // if there are no servers, bail now
        if (candidates.isEmpty()) {
            throw new NoServerAvailableException();
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

        this.logger.logInfo(LOGFROM,
                String.format("Routing to %s because %s", pick.describeServer(), routePickReason),
                LogMap.map("PreviousTries", alreadyTried.size()));
        addAffinity(pick, aiid);
        return pick;
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
     * @return the server tracker
     * @throws NoServerAvailableException
     */
    protected synchronized ServerTracker getServerForChat(UUID aiid, Set<String> alreadyTried)
            throws NoServerAvailableException {
        // was there a previous affinity entry?
        ServerTracker server = existingAffinity(aiid);

        // has the previous affinity told us it was too busy?
        if ((server != null) && alreadyTried.contains(server.getServerIdentifier())) {
            // if so, remove the affinity and reassign
            clearSingleAffinityForServer(server, aiid);
            server = null;
        }
        // assign a new affinity
        if (server == null) {
            server = chooseServerToAssignAffinity(aiid, alreadyTried);
        }
        return server;
    }

}
