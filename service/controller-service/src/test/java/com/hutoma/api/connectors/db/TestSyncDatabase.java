package com.hutoma.api.connectors.db;

import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.AiServiceStatusLogger;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.inject.Provider;

import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.*;

public class TestSyncDatabase {

    FakeRecord currentData = null;
    FakeRecord currentParameters;
    List<FakeRecord> parameterList;

    AiServiceStatusLogger logger = mock(AiServiceStatusLogger.class);
    Provider<DatabaseCall> callProvider;
    Provider<DatabaseTransaction> transactionProvider;

    FakeTimerTools tools;
    JsonSerializer serializer;

    UUID aiid1;
    UUID devid1;

    @Before
    public void setup() {
        this.parameterList = new ArrayList<>();
        this.serializer = new JsonSerializer();
        this.tools = new FakeTimerTools();
        this.currentData = null;
        this.logger = mock(AiServiceStatusLogger.class);
        this.callProvider = mock(Provider.class);
        this.transactionProvider = mock(Provider.class);
        this.aiid1 = this.tools.createNewRandomUUID();
        this.devid1 = this.tools.createNewRandomUUID();
    }

    @Test
    public void noAIs__noBackend_noChanges() throws Exception {
        DatabaseAiStatusUpdates database = fakeDatabaseCalls(Arrays.asList());
        database.synchroniseDBStatuses(getServiceIdentity(), new HashMap<>(), new HashSet<UUID>());
        Assert.assertTrue(this.parameterList.isEmpty());
    }

    @Test
    public void undefinedAIs_noBackend_noChanges() throws Exception {

        HashMap<UUID, ServerAiEntry> registration = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_UNDEFINED);
        List<FakeRecord> records = new ArrayList<>();

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(), registration, new HashSet<UUID>());
        Assert.assertTrue(this.parameterList.isEmpty());
        verify(this.logger, times(1)).logDbSyncUnknownAi(any(), any(), any());
    }

    @Test
    public void excludedAI_not_matched() throws Exception {

        // one AI reported from the backend and the db, but the AI is in the excluded list
        HashMap<UUID, ServerAiEntry> registration = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_TRAINING_COMPLETE);
        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_TRAINING_COMPLETE);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(),
                registration, new HashSet<UUID>() {{
                    add(TestSyncDatabase.this.aiid1);
                }});
        Assert.assertTrue(this.parameterList.isEmpty());

        // so we expect one ai to be flagged as unknown
        verify(this.logger, times(1)).logDbSyncUnknownAi(any(), any(), any());
    }

    @Test
    public void blankAIs_noBackend_noChanges() throws Exception {

        HashMap<UUID, ServerAiEntry> registration = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_UNDEFINED);
        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1, null);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(),
                registration, new HashSet<UUID>());
        Assert.assertTrue(this.parameterList.isEmpty());
    }

    @Test
    public void noAIs_undefinedBackend_noChanges() throws Exception {

        HashMap<UUID, ServerAiEntry> registration = new HashMap<>();
        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_UNDEFINED);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(),
                registration, new HashSet<UUID>());
        Assert.assertTrue(this.parameterList.isEmpty());
    }

    @Test
    public void matchingAI_noChanges() throws Exception {

        HashMap<UUID, ServerAiEntry> registration = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_TRAINING);
        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_TRAINING);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(),
                registration, new HashSet<UUID>());
        Assert.assertTrue(this.parameterList.isEmpty());
    }

    @Test
    public void matchingAI_differentStatus() throws Exception {

        HashMap<UUID, ServerAiEntry> regData = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_TRAINING_COMPLETE);
        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_TRAINING);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(),
                regData, new HashSet<>());

        Assert.assertEquals(1, this.parameterList.size());
        Assert.assertEquals(this.aiid1.toString(), this.parameterList.get(0).getString("3"));
        Assert.assertTrue(this.parameterList.get(0).getString("4").contains(
                TrainingStatus.AI_TRAINING_COMPLETE.value()));
    }

    @Test
    public void matchingAI_differentStatus_KeepTraining() throws Exception {

        // if the database says "training" and the server says either
        // "ready to train" or "training queued" then we keep the "training" status
        UUID aiid2 = this.tools.createNewRandomUUID();
        HashMap<UUID, ServerAiEntry> regData = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_TRAINING_QUEUED);
        addRegistrationData(regData, aiid2, TrainingStatus.AI_READY_TO_TRAIN);

        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_TRAINING);
        addDatabaseData(records, this.devid1, aiid2, TrainingStatus.AI_TRAINING);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(), regData, new HashSet<>());

        Assert.assertEquals(0, this.parameterList.size());
    }

    @Test
    public void matchingAI_differentStatus_Multiple() throws Exception {

        UUID aiid2 = this.tools.createNewRandomUUID();
        UUID devid2 = this.tools.createNewRandomUUID();

        HashMap<UUID, ServerAiEntry> regData = addRegistrationData(null, this.aiid1,
                TrainingStatus.AI_TRAINING_COMPLETE);
        addRegistrationData(regData, aiid2, TrainingStatus.AI_TRAINING_COMPLETE);

        List<FakeRecord> records = addDatabaseData(null, this.devid1, this.aiid1,
                TrainingStatus.AI_TRAINING);
        addDatabaseData(records, devid2, aiid2, TrainingStatus.AI_TRAINING);

        DatabaseAiStatusUpdates database = fakeDatabaseCalls(records);
        database.synchroniseDBStatuses(getServiceIdentity(), regData, new HashSet<UUID>());

        Assert.assertEquals(2, this.parameterList.size());

        Assert.assertEquals(this.aiid1.toString(), this.parameterList.get(0).getString("3"));
        Assert.assertTrue(this.parameterList.get(0).getString("4").contains(
                TrainingStatus.AI_TRAINING_COMPLETE.value()));

        Assert.assertEquals(aiid2.toString(), this.parameterList.get(1).getString("3"));
        Assert.assertTrue(this.parameterList.get(1).getString("4").contains(
                TrainingStatus.AI_TRAINING_COMPLETE.value()));

    }

    public ResultSet fakeResultSetFactory(List<FakeRecord> dataList) throws SQLException {
        ArrayList<FakeRecord> data = new ArrayList<>(dataList);
        ResultSet fakeResultSet = mock(ResultSet.class);

        when(fakeResultSet.next()).thenAnswer((invocation) -> {
            if (data.isEmpty()) {
                return false;
            }
            this.currentData = data.remove(0);
            return true;
        });

        when(fakeResultSet.getString(anyString())).thenAnswer((invocation) -> {
            return this.currentData.getString(invocation.getArgument(0));
        });
        when(fakeResultSet.getDouble(anyString())).thenAnswer((invocation) -> {
            return Double.parseDouble(this.currentData.getString(invocation.getArgument(0)));
        });

        return fakeResultSet;
    }

    private DatabaseAiStatusUpdates fakeDatabaseCalls(List<FakeRecord> records)
            throws DatabaseException {
        DatabaseTransaction transaction = mock(DatabaseTransaction.class);
        when(this.transactionProvider.get()).thenReturn(transaction);
        DatabaseCall databaseCall = mock(DatabaseCall.class);
        when(transaction.getDatabaseCall()).thenReturn(databaseCall);
        when(databaseCall.initialise(anyString(), anyInt())).thenReturn(databaseCall);
        when(databaseCall.add(anyString())).thenAnswer(invocation -> {
            addParameterInvoke(invocation);
            return (databaseCall);
        });
        when(databaseCall.add((UUID) any())).thenAnswer(invocation -> {
            addParameterInvoke(invocation);
            return (databaseCall);
        });
        when(databaseCall.add(anyDouble())).thenAnswer(invocation -> {
            addParameterInvoke(invocation);
            return (databaseCall);
        });
        when(databaseCall.add(anyBoolean())).thenAnswer(invocation -> {
            addParameterInvoke(invocation);
            return (databaseCall);
        });
        when(databaseCall.add((DateTime) any())).thenAnswer(invocation -> {
            addParameterInvoke(invocation);
            return (databaseCall);
        });
        when(databaseCall.executeQuery()).thenAnswer((invocation) -> {
            this.currentParameters = null;
            return fakeResultSetFactory(records);
        });
        when(databaseCall.executeUpdate()).thenAnswer((invocation) -> {
            if (this.currentParameters != null) {
                this.parameterList.add(this.currentParameters);
            }
            this.currentParameters = null;
            return 1;
        });
        return new DatabaseAiStatusUpdates(this.logger, this.callProvider, this.transactionProvider);
    }

    private void addParameterInvoke(final InvocationOnMock invocation) {
        if (this.currentParameters == null) {
            this.currentParameters = new FakeRecord();
        }
        Object o = invocation.getArgument(0);
        this.currentParameters.addNext((o == null) ? null : o.toString());
    }

    // helper function
    private HashMap<UUID, ServerAiEntry> addRegistrationData(HashMap<UUID, ServerAiEntry> map,
                                                             final UUID aiid1, TrainingStatus status) {
        map = (map == null) ? new HashMap<>() : map;
        map.put(aiid1, new ServerAiEntry(aiid1, status, ""));
        return map;
    }

    // helper function
    private List<FakeRecord> addDatabaseData(List<FakeRecord> list, final UUID devid, final UUID aiid,
                                             final TrainingStatus aiTraining) {
        list = (list == null) ? new ArrayList<>() : list;
        list.add(new FakeDBStatus(aiid.toString(), devid.toString(), aiTraining));
        return list;
    }

    // fake database record
    public class FakeRecord {

        HashMap<String, String> data;

        public FakeRecord() {
            this.data = new HashMap<>();
        }

        public void addString(String name, String value) {
            this.data.put(name, value);
        }

        public void addNext(String value) {
            addString(Integer.toString(this.data.size()), value);
        }

        public String getString(String name) {
            return this.data.getOrDefault(name, null);
        }
    }

    // fake database record for dbstatus
    public class FakeDBStatus extends FakeRecord {
        public FakeDBStatus(final String aiid, final String devid, final TrainingStatus trainingStatus) {
            addString("aiid", aiid);
            addString("dev_id", devid);
            addString("training_status",
                    (trainingStatus == null) ? null : trainingStatus.value());
            addString("server_endpoint", "endpoint");
            addString("training_progress", "0.0");
            addString("training_error", "0.0");
            //addString("queue_time", "");
        }
    }

    private static ServiceIdentity getServiceIdentity() {
        return new ServiceIdentity(BackendServerType.EMB, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION);
    }
}
