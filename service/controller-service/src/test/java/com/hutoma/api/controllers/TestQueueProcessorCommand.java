package com.hutoma.api.controllers;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.AiServiceStatusLogger;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;

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
    ControllerConfig fakeConfig;

    // happy: load status, delete ai
    @Test
    public void testQueueCommand_Delete() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, times(1))
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // fail to load status (exception)
    @Test
    public void testQueueCommand_Delete_StatusException() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenThrow(new DatabaseException("test"));
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        // no backend call
        verify(this.fakeQueueServices, never())
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        // delete from db anyway
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // fail to load status (empty, meaning deleted)
    @Test
    public void testQueueCommand_Delete_AlreadyDeleted() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(null);
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        // no backend call
        verify(this.fakeQueueServices, never())
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        // delete from db anyway
        verify(this.fakeDatabase, times(1)).deleteAiStatus(any(), any());
    }

    // load status, fail to delete ai, requeue
    @Test
    public void testQueueCommand_DeleteFailRequeue() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).deleteAiStatus(any(), any());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai with 404, don't requeue
    @Test
    public void testQueueCommand_Delete_Fail404_drop() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 404));
        doThrow(servicesException).when(this.fakeQueueServices)
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never())
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai with 500, requeue
    @Test
    public void testQueueCommand_Delete_Fail500_requeue() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 500));
        doThrow(servicesException).when(this.fakeQueueServices)
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to delete ai, fail to requeue
    @Test
    public void testQueueCommand_DeleteFailRequeueFail() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .deleteAIDirect(any(), any(), any(), anyString(), anyString());
        doThrow(DatabaseException.class).when(this.fakeDatabase)
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
        this.qproc.unqueueDelete(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, never()).deleteAiStatus(any(), any());
    }

    // happy: load status, train ai
    @Test
    public void testQueueCommand_Train() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, times(1))
                .startTrainingDirect(any(), any(), anyString(), anyString());
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // fail to load status (exception)
    @Test
    public void testQueueCommand_Train_StatusException() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenThrow(new DatabaseException("test"));
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, never())
                .startTrainingDirect(any(), any(), anyString(), anyString());
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // fail to load status (empty, meaning deleted)
    @Test
    public void testQueueCommand_Train_AlreadyDeleted() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(null);
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeQueueServices, never())
                .startTrainingDirect(any(), any(), anyString(), anyString());
        verify(this.fakeDatabase, never()).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }

    // load status, fail to train ai, requeue
    @Test
    public void testQueueCommand_TrainFailRequeue() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .startTrainingDirect(any(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to train ai with 404, drop
    @Test
    public void testQueueCommand_TrainFail404_drop() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 404));
        doThrow(servicesException).when(this.fakeQueueServices)
                .startTrainingDirect(any(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), eq(TrainingStatus.AI_ERROR), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, never())
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to train ai with 500, requeue
    @Test
    public void testQueueCommand_TrainFail500_requeue() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        ServerConnector.AiServicesException servicesException = new ServerConnector.AiServicesException("fake");
        servicesException.addSuppressed(new ServerConnector.AiServicesException("test", 500));
        doThrow(servicesException).when(this.fakeQueueServices)
                .startTrainingDirect(any(), any(), anyString(), anyString());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
        verify(this.fakeDatabase, times(1))
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
    }

    // load status, fail to train ai, fail to requeue
    @Test
    public void testQueueCommand_TrainFailRequeueFail() throws
            DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.status);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeQueueServices)
                .startTrainingDirect(any(), any(), anyString(), anyString());
        doThrow(DatabaseException.class).when(this.fakeDatabase)
                .queueUpdate(any(), any(), anyBoolean(), anyInt(), any());
        this.qproc.unqueueTrain(this.status, this.fakeServerTracker);
        verify(this.fakeDatabase, times(1)).updateAIStatus(
                any(), any(), any(), anyString(), anyDouble(), anyDouble());
    }


    @Before
    public void setUp() throws Exception {
        this.fakeConfig = mock(ControllerConfig.class);
        this.fakeController = mock(ControllerBase.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeQueueServices = mock(AIQueueServices.class);
        this.fakeServerTracker = mock(ServerTracker.class);
        Provider<AIQueueServices> fakeQueueServicesProvider = mock(Provider.class);
        when(fakeQueueServicesProvider.get()).thenReturn(this.fakeQueueServices);
        this.qproc = new QueueProcessorCommandTest(this.fakeConfig, this.fakeDatabase,
                fakeQueueServicesProvider, mock(Tools.class));
        this.qproc.initialise(this.fakeController, new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION));

        this.status = new BackendEngineStatus(
                TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, 0.0, 0.0,
                QueueAction.DELETE,
                "", new DateTime());
        this.status.setDevId(TestDataHelper.DEVID_UUID);

        when(this.fakeServerTracker.getServerIdentifier()).thenReturn(SERVERID);
        when(this.fakeServerTracker.getServerUrl()).thenReturn(SERVERURL);
    }

    public class QueueProcessorCommandTest extends QueueProcessor {

        public QueueProcessorCommandTest(final ControllerConfig config, final DatabaseAiStatusUpdates database, final Provider<AIQueueServices> queueServices,
                                         final Tools tools) {
            super(config, database, queueServices, tools, mock(AiServiceStatusLogger.class));
        }

        @Override
        public void initialise(final ControllerBase controller,
                               final ServiceIdentity serviceIdentity) {
            this.controller = controller;
            this.serviceIdentity = serviceIdentity;
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
