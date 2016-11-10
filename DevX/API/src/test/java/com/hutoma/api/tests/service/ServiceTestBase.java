package com.hutoma.api.tests.service;

import com.hutoma.api.access.AuthFilter;
import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.TelemetryLogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.HTMLExtractor;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseConnectionPool;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.connectors.db.TransactionalDatabaseCall;
import com.hutoma.api.validation.PostFilter;
import com.hutoma.api.validation.QueryFilter;
import com.hutoma.api.validation.Validate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public abstract class ServiceTestBase extends JerseyTest {

    protected static final UUID DEVID = UUID.fromString("68d5bbd6-9c20-49b3-acca-f996fe65d534");
    protected static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    protected static final MultivaluedHashMap<String, Object> noDevIdHeaders = new MultivaluedHashMap<>();
    private static final String AUTH_ENCODING_KEY = "U0hBUkVEX1NFQ1JFVA==";
    @SuppressWarnings("unchecked")
    protected static final MultivaluedHashMap<String, Object> defaultHeaders = new MultivaluedHashMap<String, Object>() {{
        put("Authorization", Collections.singletonList("Bearer " + getDevToken(DEVID, Role.ROLE_PLAN_1)));
    }};

    @Mock
    protected DatabaseCall fakeDatabaseCall;
    @Mock
    protected Database fakeDatabase;
    @Mock
    protected DatabaseTransaction fakeDatabaseTransaction;
    @Mock
    protected TransactionalDatabaseCall fakeTransactionalDatabaseCall;
    @Mock
    protected DatabaseConnectionPool fakeDatabaseConnectionPool;
    @Mock
    protected TelemetryLogger fakeLogger;
    @Mock
    protected NeuralNet fakeNeuralNet;
    @Mock
    protected SemanticAnalysis fakeSemanticAnalysis;
    @Mock
    protected Config fakeConfig;
    @Mock
    protected JerseyClient fakeJerseyClient;


    private static String getDevToken(final UUID devId, final Role role) {
        return Jwts.builder()
                .claim("ROLE", role)
                .setSubject(devId.toString())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, AUTH_ENCODING_KEY)
                .compact();
    }

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
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeConfig)).to(Config.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseConnectionPool)).to(DatabaseConnectionPool.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabase)).to(Database.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseTransaction)).to(DatabaseTransaction.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseCall)).to(DatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTransactionalDatabaseCall)).to(TransactionalDatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeNeuralNet)).to(NeuralNet.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeSemanticAnalysis)).to(SemanticAnalysis.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeLogger)).to(TelemetryLogger.class).to(Logger.class).to(ILogger.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeJerseyClient)).to(JerseyClient.class);

                // Bind all the internal dependencies to real classes
                bind(JsonSerializer.class).to(JsonSerializer.class);
                bind(Tools.class).to(Tools.class);
                bind(HTMLExtractor.class).to(HTMLExtractor.class);
                bind(Validate.class).to(Validate.class);
                bind(RateLimitCheck.class).to(RateLimitCheck.class);

                ServiceTestBase.this.addAdditionalBindings(this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <T> T deserializeResponse(Response response, Class<?> theClass) {
        JsonSvcSerializer serializer = new JsonSvcSerializer();
        return (T) serializer.deserialize((InputStream) response.getEntity(), theClass);
    }

    protected String serializeObject(Object obj) {
        JsonSvcSerializer serializer = new JsonSvcSerializer();
        return serializer.serialize(obj);
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
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseConnectionPool = mock(DatabaseConnectionPool.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeTransactionalDatabaseCall = mock(TransactionalDatabaseCall.class);
        this.fakeNeuralNet = mock(NeuralNet.class);
        this.fakeSemanticAnalysis = mock(SemanticAnalysis.class);
        this.fakeLogger = mock(TelemetryLogger.class);
        this.fakeJerseyClient = mock(JerseyClient.class);

        when(this.fakeConfig.getEncodingKey()).thenReturn(AUTH_ENCODING_KEY);

        ResourceConfig rc = new ResourceConfig(getClassUnderTest());
        AbstractBinder binder = this.getDefaultBindings();
        rc.register(binder);
        // Add Auth filter
        rc.register(AuthFilter.class);
        // Add validation filters
        rc.register(QueryFilter.class);
        rc.register(PostFilter.class);
        return rc;
    }
}
