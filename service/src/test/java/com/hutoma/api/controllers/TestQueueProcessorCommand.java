package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIQueueServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 11/04/2017.
 */
public class TestQueueProcessorCommand {

    private static final java.lang.String SERVERID = "serverid";
    private static final java.lang.String SERVERURL = "serverurl";
    QueueProcessorCommandTest qproc;
    ControllerBase fakeController;
    DatabaseAiStatusUpdates fakeDatabase;
    AIQueueServices fakeQueueServices;
    ServerTracker fakeServerTracker;
    BackendEngineStatus status;
    Config fakeConfig;

    // happy: load status, delete ai
    @Test
    public void testQueueCommand_Delete() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, times(1))
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // fail to load status (exception)
    @Test
    public void testQueueCommand_Delete_StatusException() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenThrow(new Database.DatabaseException("test"));
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        // no backend call
        verify(this.fakeQueueServices, never())
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        // delete from db anyway
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // fail to load status (empty, meaning deleted)
    @Test
    public void testQueueCommand_Delete_AlreadyDeleted() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(null);
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        // no backend call
        verify(this.fakeQueueServices, never())
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        // delete from db anyway
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // load status, fail to delete ai, requeue
    @Test
    public void testQueueCommand_DeleteFailRequeue() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).deleteAiStatus(any(), any());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai with 404, don't requeue
    @Test
    public void testQueueCommand_Delete_Fail404_drop() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 404));
        doThrow(servicesException).when(this.fakeQueueServices)
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never())
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai with 500, requeue
    @Test
    public void testQueueCommand_Delete_Fail500_requeue() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 500));
        doThrow(servicesException).when(this.fakeQueueServices)
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai, fail to requeue
    @Test
    public void testQueueCommand_DeleteFailRequeueFail() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .deleteAIDirect(anyString(), any(), anyString(), anyString());
        doThrow(Database.DatabaseException.class).when(this.fakeDatabase)
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).deleteAiStatus(any(), any());
    }

    // happy: load status, train ai
    @Test
    public void testQueueCommand_Train() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, times(1))
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // fail to load status (exception)
    @Test
    public void testQueueCommand_Train_StatusException() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenThrow(new Database.DatabaseException("test"));
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, never())
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // fail to load status (empty, meaning deleted)
    @Test
    public void testQueueCommand_Train_AlreadyDeleted() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(null);
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, never())
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // load status, fail to train ai, requeue
    @Test
    public void testQueueCommand_TrainFailRequeue() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to train ai with 404, drop
    @Test
    public void testQueueCommand_TrainFail404_drop() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 404));
        doThrow(servicesException).when(this.fakeQueueServices)
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), eq(TrainingStatus.AI_ERROR), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, never())
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to train ai with 500, requeue
    @Test
    public void testQueueCommand_TrainFail500_requeue() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 500));
        doThrow(servicesException).when(this.fakeQueueServices)
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }


    // load status, fail to train ai, fail to requeue
    @Test
    public void testQueueCommand_TrainFailRequeueFail() throws
            Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .startTrainingDirect(anyString(), any(), anyString(), anyString());
        doThrow(Database.DatabaseException.class).when(this.fakeDatabase)
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }


    @Before
    public void setUp() throws Exception {
        this.fakeConfig = mock(Config.class);
        this.fakeController = mock(ControllerBase.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeQueueServices = mock(AIQueueServices.class);
        this.fakeServerTracker = mock(ServerTracker.class);
        this.qproc = new QueueProcessorCommandTest(this.fakeConfig, this.fakeDatabase, this.fakeQueueServices,
                mock(Tools.class));
        this.qproc.initialise(this.fakeController, BackendServerType.WNET);

        this.status = new BackendEngineStatus(
                TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, 0.0, 0.0,
                QueueAction.DELETE,
                "", new DateTime());
        this.status.setDevId(TestDataHelper.DEVID);

        when(this.fakeServerTracker.getServerIdentifier()).thenReturn(SERVERID);
        when(this.fakeServerTracker.getServerUrl()).thenReturn(SERVERURL);
    }

    public class QueueProcessorCommandTest extends QueueProcessor {

        public QueueProcessorCommandTest(final Config config, final DatabaseAiStatusUpdates database, final AIQueueServices queueServices,
                                         final Tools tools) {
            super(config, database, queueServices, tools, mock(ILogger.class));
        }

        @Override
        public void initialise(final ControllerBase controller,
                               final BackendServerType serverType) {
            this.controller = controller;
            this.serverType = serverType;
        }

        @Override
        public void unqueueDelete(final BackendEngineStatus queued, final ServerTracker server) {
            super.unqueueDelete(queued, server);
        }

        @Override
        public void unqueueTrain(final BackendEngineStatus queued, final ServerTracker server) {
            super.unqueueTrain(queued, server);
        }
    }


}
