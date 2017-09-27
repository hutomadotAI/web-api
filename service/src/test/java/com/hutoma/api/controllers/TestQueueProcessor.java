package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIQueueServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseAiStatusUpdates;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.ServerEndpointTrainingSlots;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.inject.Provider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 04/04/2017.
 */
public class TestQueueProcessor {

    private static final String ENDPOINT1 = "e1";
    private static final String ENDPOINT2 = "e2";
    private static final String ENDPOINT3 = "e3";
    private static final String ENDPOINT4 = "e4";

    QueueProcessorTest qproc;
    ControllerBase fakeController;
    DatabaseAiStatusUpdates fakeDatabase;
    AIQueueServices fakeQueueServices;
    Config fakeConfig;
    Tools fakeTools;

    Pair<ArrayList<ServerEndpointTrainingSlots>, HashMap<String, ServerTracker>> fakeData;

    @Test
    public void testQueue_RoundRobinAllocation() throws Database.DatabaseException {
        this.fakeData = create(null, ENDPOINT1, 0, 2, 0, true);
        this.fakeData = create(this.fakeData, ENDPOINT2, 0, 2, 0, true);
        this.fakeData = create(this.fakeData, ENDPOINT3, 0, 2, 0, true);
        this.fakeData = create(this.fakeData, ENDPOINT4, 0, 2, 0, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        HashSet<String> serversSelected = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            this.qproc.processQueue();
            serversSelected.add(this.qproc.getChosenServer());
        }
        // round robin: make sure all four servers have come up in four requests
        Assert.assertEquals(4, serversSelected.size());
    }

    @Test
    public void testQueue_FreeSlot() throws Database.DatabaseException {
        this.fakeData = create(null, ENDPOINT1, 0, 1, 0, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        this.qproc.processQueue();
        Assert.assertEquals(ENDPOINT1, this.qproc.getChosenServer());
    }

    @Test
    public void testQueue_NoFreeSlots() throws Database.DatabaseException {
        this.fakeData = create(null, ENDPOINT1, 1, 1, 0, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.never()).queueTakeNext(any());
    }

    @Test
    public void testQueue_NoSlotsNoServers() throws Database.DatabaseException {
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(new ArrayList<>());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(new HashMap<>());
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.never()).queueTakeNext(any());
    }

    @Test
    public void testQueue_NoServers() throws Database.DatabaseException {
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(new HashMap<>());
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.never()).queueTakeNext(any());
    }

    @Test
    public void testQueue_NoSlotRecovery() throws Database.DatabaseException {
        this.fakeData = create(null, ENDPOINT1, 0, 1, 0, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.never()).recoverInterruptedTraining(any(), anyInt());
    }

    @Test
    public void testQueue_RecoverSlots() throws Database.DatabaseException, InterruptedException {
        this.fakeData = create(null, ENDPOINT1, 0, 1, 1, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        // fake-wait until the 1 second startup recovery delay passes
        this.fakeTools.threadSleep(1000);
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.times(1)).recoverInterruptedTraining(any(), anyInt());
    }

    @Test
    public void testQueue_TooEarlyToRecoverSlots() throws Database.DatabaseException, InterruptedException {
        this.fakeData = create(null, ENDPOINT1, 0, 1, 1, true);
        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());
        // fake-wait for less than one second so we are still within the startup recovery delay
        this.fakeTools.threadSleep(999);
        this.qproc.processQueue();
        verify(this.fakeDatabase, Mockito.never()).recoverInterruptedTraining(any(), anyInt());
    }

    @Before
    public void setup() throws Database.DatabaseException {
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getProcessQueueDelayRecoveryForFirstSeconds()).thenReturn(1);

        this.fakeController = mock(ControllerBase.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeQueueServices = mock(AIQueueServices.class);
        this.fakeTools = new FakeTimerTools();
        Provider<AIQueueServices> fakeQueueServicesProvider = mock(Provider.class);
        when(fakeQueueServicesProvider.get()).thenReturn(this.fakeQueueServices);

        this.qproc = new QueueProcessorTest(this.fakeConfig, this.fakeDatabase, fakeQueueServicesProvider,
                this.fakeTools, mock(AiServiceStatusLogger.class));
        this.qproc.initialise(this.fakeController, BackendServerType.WNET);

        this.fakeData = create(null, ENDPOINT1, 0, 1, 0, true);

        when(this.fakeDatabase.getQueueSlotCounts(any(), anyInt())).thenReturn(this.fakeData.getA());
        when(this.fakeController.getVerifiedEndpointMap()).thenReturn(this.fakeData.getB());

        BackendEngineStatus fakeStatus = mock(BackendEngineStatus.class);
        when(fakeStatus.getQueueAction()).thenReturn(QueueAction.DELETE);
        when(this.fakeDatabase.queueTakeNext(any())).thenReturn(fakeStatus);
    }

    private Pair<ArrayList<ServerEndpointTrainingSlots>, HashMap<String, ServerTracker>> create(
            Pair<ArrayList<ServerEndpointTrainingSlots>, HashMap<String, ServerTracker>> current,
            String name, int usedSlots, int totalSlots, int interruptedSlots, boolean verified) {
        ArrayList<ServerEndpointTrainingSlots> slots = (current == null) ? new ArrayList<>() : current.getA();
        HashMap<String, ServerTracker> servers = (current == null) ? new HashMap<>() : current.getB();
        Pair<ServerEndpointTrainingSlots, ServerTracker> appended =
                createSlots(name, usedSlots, totalSlots, interruptedSlots, verified);
        slots.add(appended.getA());
        servers.put(appended.getB().getServerIdentifier(), appended.getB());
        return (current == null) ?
                new Pair<>(slots, servers) :
                current;
    }

    private Pair<ServerEndpointTrainingSlots, ServerTracker> createSlots(String name, int usedSlots, int totalSlots, int interruptedSlots, boolean verified) {
        ServerEndpointTrainingSlots slots = new ServerEndpointTrainingSlots(name, usedSlots, interruptedSlots);
        ServerTracker tracker = mock(ServerTracker.class);
        when(tracker.canTrain()).thenReturn(true);
        when(tracker.isEndpointVerified()).thenReturn(verified);
        when(tracker.getServerIdentifier()).thenReturn(name);
        when(tracker.getTrainingCapacity()).thenReturn(totalSlots);
        return new Pair<>(slots, tracker);
    }

    public class QueueProcessorTest extends QueueProcessor {

        String chosenServer;

        public QueueProcessorTest(final Config config, final DatabaseAiStatusUpdates database,
                                  final Provider<AIQueueServices> queueServices,
                                  final Tools tools, final AiServiceStatusLogger logger) {
            super(config, database, queueServices, tools, logger);
        }

        @Override
        public void initialise(final ControllerBase controller, final BackendServerType serverType) {
            this.controller = controller;
            this.serverType = serverType;
        }

        @Override
        protected void unqueueDelete(final BackendEngineStatus queued, final ServerTracker server) {
            this.chosenServer = server.getServerIdentifier();
        }

        @Override
        protected void unqueueTrain(final BackendEngineStatus queued, final ServerTracker server) {
            this.chosenServer = server.getServerIdentifier();
        }

        @Override
        public void processQueue() throws Database.DatabaseException {
            super.processQueue();
        }

        public String getChosenServer() {
            return this.chosenServer;
        }
    }
}
