package com.hutoma.api.connectors.db;

import com.hutoma.api.common.FakeProvider;
import com.hutoma.api.logging.ILogger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by David MG on 13/10/2016.
 */
public class TestDatabaseTransaction {

    ILogger fakeLogger;
    DatabaseConnectionPool fakeConnectionPool;
    Connection fakeConnection;
    Connection fakeConnection2;
    FakeProvider<TransactionalDatabaseCall> fakeCallProvider;
    TransactionalDatabaseCall call1;
    TransactionalDatabaseCall call2;
    TransactionalDatabaseCall call3;

    @Before
    public void setup() throws DatabaseException {
        this.fakeLogger = Mockito.mock(ILogger.class);
        this.fakeConnection = Mockito.mock(Connection.class);
        this.fakeConnection2 = Mockito.mock(Connection.class);
        this.fakeConnectionPool = Mockito.mock(DatabaseConnectionPool.class);
        Mockito.when(this.fakeConnectionPool.borrowConnection()).thenReturn(this.fakeConnection).thenReturn(this.fakeConnection2);
        this.call1 = mockDBCall();
        this.call2 = mockDBCall();
        this.call3 = mockDBCall();
        this.fakeCallProvider = new FakeProvider<>(Arrays.asList(this.call1, this.call2, this.call3));
    }

    @Test
    public void testTransaction_Single() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.commit();
        }

        Mockito.verify(this.call1).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.commit();
        }

        Mockito.verify(this.call1).close();
        Mockito.verify(this.call2).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double_Rollback() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.rollback();
        }

        Mockito.verify(this.call1).close();
        Mockito.verify(this.call2).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double_AutoRollback() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        }

        Mockito.verify(this.call1).close();
        Mockito.verify(this.call2).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_FirstCallException() throws DatabaseException, SQLException {

        Mockito.when(this.call1.executeUpdate()).thenThrow(new DatabaseException(new Exception("test")));
        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        } catch (DatabaseException dde) {
        }

        Mockito.verify(this.call1).close();
        Mockito.verify(this.call2, Mockito.times(0)).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_SecondCallException() throws DatabaseException, SQLException {

        Mockito.when(this.call2.executeUpdate()).thenThrow(new DatabaseException(new Exception("test")));
        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        } catch (DatabaseException dde) {
        }

        Mockito.verify(this.call2).close();
        Mockito.verify(this.call1).close();
        Mockito.verify(this.fakeConnection).close();
        Mockito.verify(this.fakeConnection, Mockito.times(0)).commit();
        Mockito.verify(this.fakeConnection, Mockito.times(1)).rollback();
        Mockito.verifyZeroInteractions(this.fakeConnection2);
    }

    TransactionalDatabaseCall mockDBCall() throws DatabaseException {
        TransactionalDatabaseCall call = Mockito.mock(TransactionalDatabaseCall.class);
        Mockito.when(call.setTransactionConnection(Matchers.any())).thenReturn(call);
        Mockito.when(call.initialise(Matchers.anyString(), Matchers.anyInt())).thenReturn(call);
        return call;
    }
}
