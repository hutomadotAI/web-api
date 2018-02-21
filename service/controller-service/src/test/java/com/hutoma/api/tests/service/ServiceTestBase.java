package com.hutoma.api.tests.service;

import com.hutoma.api.common.ControllerConfig;
import com.hutoma.api.common.HTMLExtractor;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseBackends;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.connectors.db.TransactionalDatabaseCall;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.thread.IThreadConfig;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.thread.TrackedThreadSubPool;
import com.hutoma.api.validation.ControllerPostFilter;
import com.hutoma.api.validation.ControllerQueryFilter;

import org.glassfish.grizzly.utils.Exceptions;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public abstract class ServiceTestBase extends JerseyTest {

    protected static final UUID DEVID = UUID.fromString("68d5bbd6-9c20-49b3-acca-f996fe65d534");
    protected static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    protected static final BackendServerType AI_ENGINE = BackendServerType.WNET;
    @Mock
    protected DatabaseCall fakeDatabaseCall;
    @Mock
    protected Database fakeDatabase;
    @Mock
    protected DatabaseBackends fakeDatabaseBackends;
    @Mock
    protected DatabaseAiStatusUpdates fakeDatabaseStatusUpdates;
    @Mock
    protected DatabaseTransaction fakeDatabaseTransaction;
    @Mock
    protected TransactionalDatabaseCall fakeTransactionalDatabaseCall;
    @Mock
    protected DatabaseConnectionPool fakeDatabaseConnectionPool;
    @Mock
    protected ILogger fakeLogger;
    @Mock
    protected ControllerConfig fakeConfig;
    @Mock
    protected JerseyClient fakeJerseyClient;
    @Mock
    protected AiServiceStatusLogger fakeServicesStatusLogger;
    @Mock
    protected Tools fakeTools;
    @Mock
    protected ControllerAiml fakeControllerAiml;
    @Mock
    protected ControllerWnet fakeControllerWnet;

    static class InstanceFactory<T> implements Factory<T> {

        private final T instance;

        InstanceFactory(T instance) {
            this.instance = instance;
        }

        @Override
        public T provide() {
            return this.instance;
        }

        @Override
        public void dispose(T t) {
        }
    }

    protected AbstractBinder getDefaultBindings() {
        return new AbstractBinder() {
            @Override
            protected void configure() {

                // Bind all the external dependencies to mocks
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeConfig)).to(ControllerConfig.class).to(IThreadConfig.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseConnectionPool)).to(DatabaseConnectionPool.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabase)).to(Database.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseStatusUpdates)).to(DatabaseAiStatusUpdates.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseBackends)).to(DatabaseBackends.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseTransaction)).to(DatabaseTransaction.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseCall)).to(DatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTransactionalDatabaseCall)).to(TransactionalDatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeJerseyClient)).to(JerseyClient.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTools)).to(Tools.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeControllerAiml)).to(ControllerAiml.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeControllerWnet)).to(ControllerWnet.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeLogger)).to(ILogger.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeServicesStatusLogger)).to(AiServiceStatusLogger.class);

                // Bind all the internal dependencies to real classes
                bind(JsonSerializer.class).to(JsonSerializer.class);
                bind(HTMLExtractor.class).to(HTMLExtractor.class);
                bind(JerseyClient.class).to(JerseyClient.class);
                bind(ThreadPool.class).to(ThreadPool.class);
                bind(ThreadSubPool.class).to(ThreadSubPool.class);
                bind(TrackedThreadSubPool.class).to(TrackedThreadSubPool.class);
                // Bind a mock of HttpServletRequest
                bind(mock(HttpServletRequest.class)).to(HttpServletRequest.class);

                ServiceTestBase.this.addAdditionalBindings(this);
            }
        };
    }

    protected abstract Class<?> getClassUnderTest();

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        return binder;
    }

    @Override
    protected Application configure() {
        // Use the first available port, to support tests running in parallel
        forceSet(TestProperties.CONTAINER_PORT, "0");

        MockitoAnnotations.initMocks(this);

        // Mock all the external dependencies
        this.fakeDatabase = mock(Database.class);
        this.fakeDatabaseStatusUpdates = mock(DatabaseAiStatusUpdates.class);
        this.fakeDatabaseBackends = mock(DatabaseBackends.class);
        this.fakeConfig = mock(ControllerConfig.class);
        this.fakeDatabaseConnectionPool = mock(DatabaseConnectionPool.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeTransactionalDatabaseCall = mock(TransactionalDatabaseCall.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeControllerAiml = mock(ControllerAiml.class);
        this.fakeControllerWnet = mock(ControllerWnet.class);

        when(this.fakeControllerWnet.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerWnet.getSessionServerIdentifier(eq(TestDataHelper.SESSIONID))).thenReturn("wnet@fake");
        when(this.fakeControllerWnet.isPrimaryMaster(eq(TestDataHelper.SESSIONID))).thenReturn(true);

        ResourceConfig rc = new ResourceConfig(getClassUnderTest());
        AbstractBinder binder = this.getDefaultBindings();
        rc.register(binder);
        // Add validation filters
        rc.register(ControllerQueryFilter.class);
        rc.register(ControllerPostFilter.class);

        // Register the multipart handler for supporting uploads
        rc.register(MultiPartFeature.class);

        // Log request and response payload to make debugging easier on errors
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        rc.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");

        rc.register(DebugExceptionMapper.class);

        return rc;
    }

    protected String getTestsBaseLocation() {
        return this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    protected String serializeObject(Object obj) {
        JsonSvcSerializer serializer = new JsonSvcSerializer();
        return serializer.serialize(obj);
    }


    @Provider
    public static class DebugExceptionMapper implements ExtendedExceptionMapper<Throwable> {

        @Override
        public boolean isMappable(Throwable throwable) {
            // ignore these guys and let jersey handle them
            return !(throwable instanceof WebApplicationException);
        }

        @Override
        public Response toResponse(Throwable throwable) {
            return Response.status(500).entity(Exceptions.getStackTraceAsString(throwable)).type("text/plain")
                    .build();
        }
    }
}
