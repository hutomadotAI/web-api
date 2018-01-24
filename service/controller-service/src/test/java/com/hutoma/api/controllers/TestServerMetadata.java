package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import edu.umd.cs.findbugs.annotations.ExpectWarning;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;

/**
 * Created by David MG on 08/02/2017.
 */
public class TestServerMetadata {

    AiServiceStatusLogger logger;
    ControllerConfig config;
    FakeTimerTools tools;
    ServerMetadataUnderTest test;

    @Before
    public void setUp() throws Exception {
        this.logger = mock(AiServiceStatusLogger.class);
        this.config = mock(ControllerConfig.class);
        this.tools = new FakeTimerTools();
        this.test = new ServerMetadataUnderTest(this.logger, this.config, this.tools, null);
    }

    @Test
    public void serverMetadata_noServer() {
        try {
            this.test.getServerForChat(UUID.randomUUID(), Collections.EMPTY_SET);
        } catch (NoServerAvailableException noServerAvailable) {
            return;
        }
        Assert.fail("expected an exception");
    }

    @Test
    public void serverMetadata_oneServer() throws NoServerAvailableException {
        ServerTracker server = makeServerTracker(1);
        Assert.assertEquals(server, this.test.getServerForChat(UUID.randomUUID(), Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_twoServers() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        Assert.assertEquals(server1, this.test.getServerForChat(UUID.randomUUID(), Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_assignedAffinity() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();
        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_chatCapacityZero() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(0);
        ServerTracker server2 = makeServerTracker(1);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        Assert.assertEquals(0, allocated.stream().filter(x -> x.equals(server1)).count());
        Assert.assertEquals(10, allocated.stream().filter(x -> x.equals(server2)).count());
    }

    @Test
    public void serverMetadata_chatCapacityEqual() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        long s1 = allocated.stream().filter(x -> x.equals(server1)).count();
        long s2 = allocated.stream().filter(x -> x.equals(server2)).count();
        Assert.assertEquals(10, s1 + s2);
        Assert.assertTrue(s1 >= 1 && s2 >= 1); // minimum requirement (test for random allocation)
        Assert.assertTrue(s1 == 5 && s2 == 5); // test for round robin
    }

    @Test
    public void serverMetadata_chatCapacityDistribution() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(4);
        ServerTracker server2 = makeServerTracker(1);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        long s1 = allocated.stream().filter(x -> x.equals(server1)).count();
        long s2 = allocated.stream().filter(x -> x.equals(server2)).count();
        Assert.assertEquals(10, s1 + s2);
        Assert.assertTrue(s1 >= 4 && s2 >= 1);
    }

    @Test
    public void serverMetadata_chatCapacity_assignFreeSlot() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(10);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        ServerTracker server2 = makeServerTracker(10);

        UUID chat1 = UUID.randomUUID();
        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_affinityUpdate_OverridesCapacity() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(10);
        ServerTracker server2 = makeServerTracker(1);

        List<UUID> aiids = get10Uuids();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();
        this.test.updateAffinity(server2SessionID, aiids);

        aiids.stream().forEach(aiid -> {
            try {
                Assert.assertEquals(server2, this.test.getServerForChat(aiid, Collections.EMPTY_SET));
            } catch (NoServerAvailableException noServerAvailable) {
                Assert.fail("no server available for updated affinity");
            }
        });
    }

    @Test
    public void serverMetadata_affinityUpdate_OverridesAssignment() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();

        // crossover allocations
        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat2));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat1));

        Assert.assertEquals(server1, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_affinityUpdate_Invert() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();

        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat2));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat1));

        Assert.assertEquals(server1, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));

        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat1));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat2));

        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_dropSession() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));

        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();
        this.test.deleteSession(server2SessionID);

        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server1, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_affinityUpdate_AfterDrop() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        this.test.deleteSession(server1SessionID);
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_serverSunrise_DontAllocate() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_serverSunrise_IgnoreAffinity() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        UUID server1SessionID = server1.getSessionID();
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_serverSunrise_RestoreAffinityWhenVerified() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = UUID.randomUUID();
        UUID chat2 = UUID.randomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        ((FakeServerTracker) server1).testSetEndpointVerified(true);

        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server1, this.test.getServerForChat(chat2, Collections.EMPTY_SET));
    }

    @Test(expected = NoServerAvailableException.class)
    public void serverMetadata_affinityRetry() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        UUID chat1 = UUID.randomUUID();
        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        this.test.getServerForChat(chat1,
                Collections.singleton(server1.getServerIdentifier()));
    }

    @Test
    public void serverMetadata_affinityRetryNext() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        UUID chat1 = UUID.randomUUID();
        Assert.assertEquals(server1, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
        Assert.assertEquals(server2, this.test.getServerForChat(chat1,
                Collections.singleton(server1.getServerIdentifier())));
    }

    @Test
    public void serverMetadata_affinityRetryNext_Persistent() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        UUID chat1 = UUID.randomUUID();
        Assert.assertEquals(server2, this.test.getServerForChat(chat1,
                Collections.singleton(server1.getServerIdentifier())));
        Assert.assertEquals(server2, this.test.getServerForChat(chat1, Collections.EMPTY_SET));
    }

    @Test
    public void serverMetadata_affinityRetryNext_Cleanup() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        UUID chat1 = UUID.randomUUID();

        this.test.getServerForChat(chat1, Collections.EMPTY_SET);
        Assert.assertEquals(1, server1.getChatAffinity().size());
        Assert.assertEquals(0, server2.getChatAffinity().size());

        this.test.getServerForChat(chat1, Collections.singleton(server1.getServerIdentifier()));
        Assert.assertEquals(0, server1.getChatAffinity().size());
        Assert.assertEquals(1, server2.getChatAffinity().size());

    }

    @Test
    public void serverMetadata_NoTestServer() {
        try {
            this.test.getServerForUpload(UUID.randomUUID());
            Assert.fail("expected an exception");
        } catch (NoServerAvailableException noServerAvailable) {
        }
    }

    @Test
    public void serverMetadata_TestServerAvailable() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerForUpload(UUID.randomUUID()));
    }

    @Test(expected = NoServerAvailableException.class)
    public void serverMetadata_OnlyEndingServerAvailable() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        server1.endServerSession();
        this.test.getServerForUpload(UUID.randomUUID());
    }

    @Test
    public void serverMetadata_TwoTestServersAvailable() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerForUpload(UUID.randomUUID()));
        ServerTracker server2 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerForUpload(UUID.randomUUID()));
        ServerTracker server3 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerForUpload(UUID.randomUUID()));
    }

    @Test
    public void serverMetadata_HandoverWhenSessionEnding() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        ServerTracker server2 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerForUpload(UUID.randomUUID()));
        server1.endServerSession();
        Assert.assertEquals(server2, this.test.getServerForUpload(UUID.randomUUID()));
    }

    @Test
    public void serverMetadata_OnlyServerIsPrimary() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        Assert.assertTrue(this.test.isPrimaryMaster(server1.getSessionID()));
    }

    @Test
    public void serverMetadata_OnlyEndingSessionNotPrimary() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(1, 1);
        server1.endServerSession();
        Assert.assertFalse(this.test.isPrimaryMaster(server1.getSessionID()));
    }

    @Test
    public void serverMetadata_FirstServerPrimary() throws NoServerAvailableException {
        ServerTracker[] trackers = new ServerTracker[16];
        for(int i=0; i<trackers.length; i++) {
            trackers[i] = makeServerTracker(1, 1);
            // all other servers are never the primary
            if (i>0) {
                Assert.assertFalse("" + i + "should not be master",
                        this.test.isPrimaryMaster(trackers[i].getSessionID()));
            }
            // the first server is always the master
            Assert.assertTrue("" + i + " should not have displaced master",
                    this.test.isPrimaryMaster(trackers[0].getSessionID()));
        }
    }

    @Test
    public void serverMetadata_TrainingRouteToMaster() throws NoServerAvailableException {
        ServerTracker server1 = makeServerTracker(0, 1);
        ServerTracker server2 = makeServerTracker(0, 1);
        ServerTracker server3 = makeServerTracker(1, 1);
        Assert.assertEquals(server3, this.test.getServerForUpload(UUID.randomUUID()));
    }

    private List<ServerTracker> getEndpointsFor10Aiids() {
        List<UUID> aiids = get10Uuids();
        return aiids.stream().map(aiid -> {
            try {
                return this.test.getServerForChat(aiid, Collections.EMPTY_SET);
            } catch (NoServerAvailableException noServerAvailable) {
                noServerAvailable.printStackTrace();
            }
            return null;
        }).collect(toList());
    }

    private List<UUID> get10Uuids() {
        return IntStream.range(0, 10)
                .mapToObj(x -> UUID.randomUUID()).collect(toList());
    }

    private ServerTracker makeServerTracker(int chatCapacity) {
        return makeServerTracker(0, chatCapacity);
    }

    private ServerTracker makeServerTracker(int trainingCapacity, int chatCapacity) {
        ServerTracker server = new FakeServerTracker(this.config, this.tools, this.logger);
        UUID uuid = server.trackServer(new ServerRegistration(
                BackendServerType.WNET, "url:" + tools.generateRandomHexString(4), trainingCapacity, chatCapacity));
        this.test.addNewSession(uuid, server);
        return server;
    }

    public class ServerMetadataUnderTest extends ServerMetadata {

        protected ServiceLocator serviceLocator;

        public ServerMetadataUnderTest(final AiServiceStatusLogger logger, final ControllerConfig config, final Tools tools, final ServiceLocator serviceLocator) {
            super(logger);
        }

        @Override
        public synchronized boolean updateAffinity(final UUID sessionID, final Collection<UUID> aiidList) {
            return super.updateAffinity(sessionID, aiidList);
        }

        @Override
        public synchronized void addNewSession(final UUID serverSessionID, final ServerTracker tracker) {
            super.addNewSession(serverSessionID, tracker);
        }

        @Override
        public synchronized void deleteSession(final UUID serverSessionID) {
            super.deleteSession(serverSessionID);
        }
        
    }

    public class FakeServerTracker extends ServerTracker {

        public FakeServerTracker(final ControllerConfig config, final Tools tools, final ILogger logger) {
            super(config, tools, mock(JerseyClient.class), null, logger, null);
            testSetEndpointVerified(true);
            this.runFlag.set(true);
        }

        public void testSetEndpointVerified(boolean flag) {
            this.endpointVerified.set(flag);
        }

        @Override
        public UUID getSessionID() {
            return this.serverSessionID;
        }

    }
}
