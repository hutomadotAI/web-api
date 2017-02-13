package com.hutoma.api.controller;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.controllers.ControllerBase.RequestFor;
import com.hutoma.api.controllers.ServerMetadata;
import com.hutoma.api.controllers.ServerTracker;

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

    ILogger logger;
    Config config;
    FakeTimerTools tools;
    ServerMetadataUnderTest test;

    @Before
    public void setUp() throws Exception {
        this.logger = mock(ILogger.class);
        this.config = mock(Config.class);
        this.tools = new FakeTimerTools();
        this.test = new ServerMetadataUnderTest(this.logger, this.config, this.tools, null);
    }

    @Test
    public void serverMetadata_noServer() {
        try {
            this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Chat);
        } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
            return;
        }
        Assert.fail("expected an exception");
    }

    @Test
    public void serverMetadata_oneServer() throws ServerMetadata.NoServerAvailable {
        ServerTracker server = makeServerTracker(1);
        Assert.assertEquals(server, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Chat));
    }

    @Test
    public void serverMetadata_twoServers() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        Assert.assertEquals(server1, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Chat));
    }

    @Test
    public void serverMetadata_assignedAffinity() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();
        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_chatCapacityZero() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(0);
        ServerTracker server2 = makeServerTracker(1);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        Assert.assertEquals(0, allocated.stream().filter(x -> x.equals(server1)).count());
        Assert.assertEquals(10, allocated.stream().filter(x -> x.equals(server2)).count());
    }

    @Test
    public void serverMetadata_chatCapacityEqual() throws ServerMetadata.NoServerAvailable {
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
    public void serverMetadata_chatCapacityDistribution() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(4);
        ServerTracker server2 = makeServerTracker(1);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        long s1 = allocated.stream().filter(x -> x.equals(server1)).count();
        long s2 = allocated.stream().filter(x -> x.equals(server2)).count();
        Assert.assertEquals(10, s1 + s2);
        Assert.assertTrue(s1 >= 4 && s2 >= 1);
    }

    @Test
    public void serverMetadata_chatCapacity_assignFreeSlot() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(10);
        List<ServerTracker> allocated = getEndpointsFor10Aiids();
        ServerTracker server2 = makeServerTracker(10);

        UUID chat1 = this.tools.createNewRandomUUID();
        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_affinityUpdate_OverridesCapacity() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(10);
        ServerTracker server2 = makeServerTracker(1);

        List<UUID> aiids = get10Uuids();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();
        this.test.updateAffinity(server2SessionID, aiids);

        aiids.stream().forEach(aiid -> {
            try {
                Assert.assertEquals(server2, this.test.getServerFor(aiid, RequestFor.Chat));
            } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
                Assert.fail("no server available for updated affinity");
            }
        });
    }

    @Test
    public void serverMetadata_affinityUpdate_OverridesAssignment() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();

        // crossover allocations
        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat2));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat1));

        Assert.assertEquals(server1, this.test.getServerFor(chat2, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_affinityUpdate_Invert() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();

        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat2));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat1));

        Assert.assertEquals(server1, this.test.getServerFor(chat2, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));

        this.test.updateAffinity(server1SessionID, Collections.singletonList(chat1));
        this.test.updateAffinity(server2SessionID, Collections.singletonList(chat2));

        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_dropSession() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));

        UUID server2SessionID = ((FakeServerTracker) server2).getSessionID();
        this.test.deleteSession(server2SessionID);

        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server1, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_affinityUpdate_AfterDrop() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        this.test.deleteSession(server1SessionID);
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_serverSunrise_DontAllocate() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_serverSunrise_IgnoreAffinity() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        Assert.assertEquals(server2, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server2, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_serverSunrise_RestoreAffinityWhenVerified() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1);
        ServerTracker server2 = makeServerTracker(1);
        ((FakeServerTracker) server1).testSetEndpointVerified(false);

        UUID chat1 = this.tools.createNewRandomUUID();
        UUID chat2 = this.tools.createNewRandomUUID();

        UUID server1SessionID = ((FakeServerTracker) server1).getSessionID();
        this.test.updateAffinity(server1SessionID, Arrays.asList(chat1, chat2));

        ((FakeServerTracker) server1).testSetEndpointVerified(true);

        Assert.assertEquals(server1, this.test.getServerFor(chat1, RequestFor.Chat));
        Assert.assertEquals(server1, this.test.getServerFor(chat2, RequestFor.Chat));
    }

    @Test
    public void serverMetadata_NoTestServer() {
        try {
            this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training);
            Assert.fail("expected an exception");
        } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
        }
    }

    @Test
    public void serverMetadata_TestServerAvailable() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training));
    }

    @Test
    public void serverMetadata_TwoTestServersAvailable() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training));
        ServerTracker server2 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training));
        ServerTracker server3 = makeServerTracker(1, 1);
        Assert.assertEquals(server1, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training));
    }

    @Test
    public void serverMetadata_TrainingRouteToMaster() throws ServerMetadata.NoServerAvailable {
        ServerTracker server1 = makeServerTracker(0, 1);
        ServerTracker server2 = makeServerTracker(0, 1);
        ServerTracker server3 = makeServerTracker(1, 1);
        Assert.assertEquals(server3, this.test.getServerFor(this.tools.createNewRandomUUID(), RequestFor.Training));
    }

    private List<ServerTracker> getEndpointsFor10Aiids() {
        List<UUID> aiids = get10Uuids();
        return aiids.stream().map(aiid -> {
            try {
                return this.test.getServerFor(aiid, RequestFor.Chat);
            } catch (ServerMetadata.NoServerAvailable noServerAvailable) {
                noServerAvailable.printStackTrace();
            }
            return null;
        }).collect(toList());
    }

    private List<UUID> get10Uuids() {
        return IntStream.range(0, 10)
                .mapToObj(x -> this.tools.createNewRandomUUID()).collect(toList());
    }

    private ServerTracker makeServerTracker(int chatCapacity) {
        return makeServerTracker(0, chatCapacity);
    }

    private ServerTracker makeServerTracker(int trainingCapacity, int chatCapacity) {
        ServerTracker server = new FakeServerTracker(this.config, this.tools, this.logger);
        UUID uuid = server.trackServer(new ServerRegistration(
                "test", "url1", trainingCapacity, chatCapacity));
        this.test.addNewSession(uuid, server);
        return server;
    }

    public class ServerMetadataUnderTest extends ServerMetadata {

        public ServerMetadataUnderTest(final ILogger logger, final Config config, final Tools tools, final ServiceLocator serviceLocator) {
            super(logger, config, tools, serviceLocator);
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

        @Override
        public synchronized ServerTracker getServerFor(final UUID aiid, final RequestFor requestFor) throws NoServerAvailable {
            return super.getServerFor(aiid, requestFor);
        }

    }

    public class FakeServerTracker extends ServerTracker {

        public FakeServerTracker(final Config config, final Tools tools, final ILogger logger) {
            super(config, tools, mock(JerseyClient.class), null, logger);
            testSetEndpointVerified(true);
        }

        public void testSetEndpointVerified(boolean flag) {
            this.endpointVerified.set(flag);
        }

        public UUID getSessionID() {
            return this.serverSessionID;
        }

    }
}
