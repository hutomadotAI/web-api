package com.hutoma.api.tests.service;

import com.hutoma.api.access.AuthFilter;
import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.access.Role;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.AiStrings;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.common.HTMLExtractor;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.FacebookChatHandler;
import com.hutoma.api.thread.IThreadConfig;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.thread.TrackedThreadSubPool;
import com.hutoma.api.validation.PostFilter;
import com.hutoma.api.validation.QueryFilter;
import com.hutoma.api.validation.Validate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

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

import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public abstract class ServiceTestBase extends JerseyTest {

    public static final String AUTH_ENCODING_KEY = "U0hBUkVEX1NFQ1JFVA==";
    protected static final UUID DEVID = UUID.fromString("68d5bbd6-9c20-49b3-acca-f996fe65d534");
    protected static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    protected static final BackendServerType AI_ENGINE = BackendServerType.WNET;
    protected static final MultivaluedHashMap<String, Object> noDevIdHeaders = new MultivaluedHashMap<>();
    @SuppressWarnings("unchecked")
    protected static final MultivaluedHashMap<String, Object> defaultHeaders = getDevIdAuthHeaders(Role.ROLE_PLAN_1, DEVID);
    @Mock
    protected DatabaseCall fakeDatabaseCall;
    @Mock
    protected Database fakeDatabase;
    @Mock
    protected DatabaseAI fakeDatabaseAi;
    @Mock
    protected DatabaseMarketplace fakeDatabaseMarketplace;
    @Mock
    protected DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    @Mock
    protected DatabaseAiStatusUpdates fakeDatabaseStatusUpdates;
    @Mock
    protected DatabaseIntegrations fakeDatabaseIntegrations;
    @Mock
    protected DatabaseBackends fakeDatabaseBackends;
    @Mock
    protected DatabaseTransaction fakeDatabaseTransaction;
    @Mock
    protected TransactionalDatabaseCall fakeTransactionalDatabaseCall;
    @Mock
    protected DatabaseConnectionPool fakeDatabaseConnectionPool;
    @Mock
    protected ILogger fakeLogger;
    @Mock
    protected Config fakeConfig;
    @Mock
    protected JerseyClient fakeJerseyClient;
    @Mock
    protected AIChatServices fakeAiChatServices;
    @Mock
    protected AiServiceStatusLogger fakeServicesStatusLogger;
    @Mock
    protected AIServices fakeAiServices;
    @Mock
    protected Tools fakeTools;
    @Mock
    protected ControllerAiml fakeControllerAiml;
    @Mock
    protected ControllerWnet fakeControllerWnet;
    @Mock
    protected ControllerRnn fakeControllerRnn;
    @Mock
    protected WebHooks fakeWebHooks;
    @Mock
    protected AccessLogger fakeAccessLogger;
    @Mock
    protected EntityRecognizerService fakeEntityRecognizer;
    @Mock
    protected FacebookConnector fakefacebookConnector;
    @Mock
    protected AiStrings fakeAiStrings;

    public static MultivaluedHashMap<String, Object> getDevIdAuthHeaders(final Role role, final UUID devId) {
        return new MultivaluedHashMap<String, Object>() {{
            put("Authorization", Collections.singletonList("Bearer " + getDevToken(devId, role)));
        }};
    }

    public static String getDevToken(final UUID devId, final Role role) {
        return Jwts.builder()
                .claim("ROLE", role)
                .setSubject(devId.toString())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, AUTH_ENCODING_KEY)
                .compact();
    }

    public static String getClientToken(final UUID devId, final UUID aiid) {
        return Jwts.builder()
                .claim("ROLE", Role.ROLE_CLIENTONLY)
                .claim("AIID", aiid.toString())
                .setSubject(devId.toString())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, AUTH_ENCODING_KEY)
                .compact();
    }

    public MultivaluedHashMap<String, Object> getClientAuthHeaders(final UUID devId, final UUID aiid) {
        return new MultivaluedHashMap<String, Object>() {{
            put("Authorization", Collections.singletonList("Bearer " + getClientToken(devId, aiid)));
        }};
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
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeConfig)).to(Config.class).to(IThreadConfig.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseConnectionPool)).to(DatabaseConnectionPool.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabase)).to(Database.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseAi)).to(DatabaseAI.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseMarketplace)).to(DatabaseMarketplace.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseEntitiesIntents)).to(DatabaseEntitiesIntents.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseStatusUpdates)).to(DatabaseAiStatusUpdates.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseIntegrations)).to(DatabaseIntegrations.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseBackends)).to(DatabaseBackends.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseTransaction)).to(DatabaseTransaction.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseCall)).to(DatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTransactionalDatabaseCall)).to(TransactionalDatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeJerseyClient)).to(JerseyClient.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiChatServices)).to(AIChatServices.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiServices)).to(AIServices.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTools)).to(Tools.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeControllerAiml)).to(ControllerAiml.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeControllerWnet)).to(ControllerWnet.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeControllerRnn)).to(ControllerRnn.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeWebHooks)).to(WebHooks.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeLogger)).to(ILogger.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeServicesStatusLogger)).to(AiServiceStatusLogger.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAccessLogger)).to(AccessLogger.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeEntityRecognizer)).to(EntityRecognizerService.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakefacebookConnector)).to(FacebookConnector.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiStrings)).to(AiStrings.class);

                // Bind all the internal dependencies to real classes
                bind(JsonSerializer.class).to(JsonSerializer.class);
                bind(HTMLExtractor.class).to(HTMLExtractor.class);
                bind(Validate.class).to(Validate.class);
                bind(RateLimitCheck.class).to(RateLimitCheck.class);
                bind(AIChatServices.class).to(AIChatServices.class);
                bind(JerseyClient.class).to(JerseyClient.class);
                bind(ThreadPool.class).to(ThreadPool.class);
                bind(ThreadSubPool.class).to(ThreadSubPool.class);
                bind(TrackedThreadSubPool.class).to(TrackedThreadSubPool.class);
                bind(FacebookChatHandler.class).to(FacebookChatHandler.class);
                // Bind a mock of HttpServletRequest
                bind(mock(HttpServletRequest.class)).to(HttpServletRequest.class);

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
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeDatabaseStatusUpdates = mock(DatabaseAiStatusUpdates.class);
        this.fakeDatabaseIntegrations = mock(DatabaseIntegrations.class);
        this.fakeDatabaseBackends = mock(DatabaseBackends.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseConnectionPool = mock(DatabaseConnectionPool.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeTransactionalDatabaseCall = mock(TransactionalDatabaseCall.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeAiChatServices = mock(AIChatServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeControllerAiml = mock(ControllerAiml.class);
        this.fakeControllerWnet = mock(ControllerWnet.class);
        this.fakeControllerRnn = mock(ControllerRnn.class);
        this.fakeWebHooks = mock(WebHooks.class);
        this.fakeAccessLogger = mock(AccessLogger.class);
        this.fakeEntityRecognizer = mock(EntityRecognizerService.class);

        when(this.fakeConfig.getEncodingKey()).thenReturn(AUTH_ENCODING_KEY);
        try {
            when(this.fakeDatabase.checkRateLimit(any(), anyString(), anyDouble(), anyDouble()))
                    .thenReturn(new RateLimitStatus(false, 1.0, true));
            when(this.fakeDatabaseEntitiesIntents.checkRateLimit(any(), anyString(), anyDouble(), anyDouble()))
                    .thenReturn(new RateLimitStatus(false, 1.0, true));
        } catch (DatabaseException e) {
            // this will never happen, but on the zero in a million chance that it does ....
            e.printStackTrace();
        }

        when(this.fakeControllerWnet.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerWnet.getSessionServerIdentifier(eq(TestDataHelper.SESSIONID))).thenReturn("wnet@fake");
        when(this.fakeControllerWnet.isPrimaryMaster(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerRnn.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerRnn.getSessionServerIdentifier(eq(TestDataHelper.SESSIONID))).thenReturn("rnn@fake");
        when(this.fakeControllerRnn.isPrimaryMaster(eq(TestDataHelper.SESSIONID))).thenReturn(true);

        when(this.fakeConfig.getThreadPoolMaxThreads()).thenReturn(16);
        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(5);
        // Set the rate limit frequency to a positive value to allow the tests to run
        when(this.fakeConfig.getRateLimit_QuickRead_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_Chat_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_BotstoreMetadata_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_BotstorePublish_Frequency()).thenReturn(1.0);

        try {
            when(this.fakeAiStrings.getDefaultChatResponses(any(), any())).thenReturn(Collections.singletonList(ChatLogic.COMPLETELY_LOST_RESULT));
        } catch (AiStrings.AiStringsException ex) {
            // this will never happen, but on the zero in a million chance that it does ....
            ex.printStackTrace();
        }

        ResourceConfig rc = new ResourceConfig(getClassUnderTest());
        AbstractBinder binder = this.getDefaultBindings();
        rc.register(binder);
        // Add Auth filter
        rc.register(AuthFilter.class);
        // Add validation filters
        rc.register(QueryFilter.class);
        rc.register(PostFilter.class);
        rc.register(RateLimitCheck.class);

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
