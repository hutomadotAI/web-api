package com.hutoma.api.connectors.db;

import com.hutoma.api.common.FakeProvider;
import com.hutoma.api.common.ILogger;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

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
        this.fakeLogger = mock(ILogger.class);
        this.fakeConnection = mock(Connection.class);
        this.fakeConnection2 = mock(Connection.class);
        this.fakeConnectionPool = mock(DatabaseConnectionPool.class);
        when(this.fakeConnectionPool.borrowConnection()).thenReturn(this.fakeConnection).thenReturn(this.fakeConnection2);
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

        verify(this.call1).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(1)).commit();
        verify(this.fakeConnection, times(0)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.commit();
        }

        verify(this.call1).close();
        verify(this.call2).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(1)).commit();
        verify(this.fakeConnection, times(0)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double_Rollback() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.rollback();
        }

        verify(this.call1).close();
        verify(this.call2).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(0)).commit();
        verify(this.fakeConnection, times(1)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_Double_AutoRollback() throws DatabaseException, SQLException {

        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        }

        verify(this.call1).close();
        verify(this.call2).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(0)).commit();
        verify(this.fakeConnection, times(1)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_FirstCallException() throws DatabaseException, SQLException {

        when(this.call1.executeUpdate()).thenThrow(new DatabaseException(new Exception("test")));
        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        } catch (DatabaseException dde) {
        }

        verify(this.call1).close();
        verify(this.call2, times(0)).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(0)).commit();
        verify(this.fakeConnection, times(1)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    @Test
    public void testTransaction_SecondCallException() throws DatabaseException, SQLException {

        when(this.call2.executeUpdate()).thenThrow(new DatabaseException(new Exception("test")));
        try (DatabaseTransaction transaction = new DatabaseTransaction(this.fakeLogger, this.fakeConnectionPool, this.fakeCallProvider)) {
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
            transaction.getDatabaseCall().initialise("nothing", 0).executeUpdate();
        } catch (DatabaseException dde) {
        }

        verify(this.call2).close();
        verify(this.call1).close();
        verify(this.fakeConnection).close();
        verify(this.fakeConnection, times(0)).commit();
        verify(this.fakeConnection, times(1)).rollback();
        verifyZeroInteractions(this.fakeConnection2);
    }

    TransactionalDatabaseCall mockDBCall() throws DatabaseException {
        TransactionalDatabaseCall call = mock(TransactionalDatabaseCall.class);
        when(call.setTransactionConnection(any())).thenReturn(call);
        when(call.initialise(anyString(), anyInt())).thenReturn(call);
        return call;
    }
}
