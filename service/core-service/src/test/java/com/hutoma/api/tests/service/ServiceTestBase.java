package com.hutoma.api.tests.service;

import com.hutoma.api.access.AuthFilter;
import com.hutoma.api.access.RateLimitCheck;
import com.hutoma.api.access.Role;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.FacebookChatHandler;
import com.hutoma.api.logic.LanguageLogic;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import com.hutoma.api.thread.*;
import com.hutoma.api.validation.PostFilter;
import com.hutoma.api.validation.QueryFilter;
import com.hutoma.api.validation.Validate;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public abstract class ServiceTestBase extends JerseyTest {

    private static final String AUTH_ENCODING_KEY = "U0hBUkVEX1NFQ1JFVA==";
    protected static final UUID DEVID = UUID.fromString("68d5bbd6-9c20-49b3-acca-f996fe65d534");
    protected static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    protected static final BackendServerType AI_ENGINE = BackendServerType.EMB;
    static final MultivaluedHashMap<String, Object> noDevIdHeaders = new MultivaluedHashMap<>();
    @SuppressWarnings("unchecked")
    protected static final MultivaluedHashMap<String, Object> defaultHeaders = getDevIdAuthHeaders(Role.ROLE_PLAN_1,
            DEVID);
    @Mock
    DatabaseCall fakeDatabaseCall;
    @Mock
    Database fakeDatabase;
    @Mock
    protected DatabaseAI fakeDatabaseAi;
    @Mock
    DatabaseMarketplace fakeDatabaseMarketplace;
    @Mock
    protected DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    @Mock
    DatabaseIntegrations fakeDatabaseIntegrations;
    @Mock
    DatabaseBackends fakeDatabaseBackends;
    @Mock
    DatabaseTransaction fakeDatabaseTransaction;
    @Mock
    TransactionalDatabaseCall fakeTransactionalDatabaseCall;
    @Mock
    DatabaseConnectionPool fakeDatabaseConnectionPool;
    @Mock
    protected ILogger fakeLogger;
    @Mock
    protected Config fakeConfig;
    @Mock
    JerseyClient fakeJerseyClient;
    @Mock
    AIChatServices fakeAiChatServices;
    @Mock
    AIServices fakeAiServices;
    @Mock
    protected Tools fakeTools;
    @Mock
    WebHooks fakeWebHooks;
    @Mock
    AccessLogger fakeAccessLogger;
    @Mock
    EntityRecognizerService fakeEntityRecognizer;
    @Mock
    FacebookConnector fakefacebookConnector;
    @Mock
    AiStrings fakeAiStrings;
    @Mock
    FeatureToggler fakeFeatureToggler;
    @Mock
    LanguageLogic fakeLanguageLogic;

    private static MultivaluedHashMap<String, Object> getDevIdAuthHeaders(final Role role, final UUID devId) {
        return new MultivaluedHashMap<String, Object>() {
            {
                put("Authorization", Collections.singletonList("Bearer " + getDevToken(devId, role)));
            }
        };
    }

    private static String getDevToken(final UUID devId, final Role role) {
        return Jwts.builder().claim("ROLE", role).setSubject(devId.toString()).compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, AUTH_ENCODING_KEY).compact();
    }

    private static String getClientToken(final UUID devId, final UUID aiid) {
        return Jwts.builder().claim("ROLE", Role.ROLE_CLIENTONLY).claim("AIID", aiid.toString())
                .setSubject(devId.toString()).compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, AUTH_ENCODING_KEY).compact();
    }

    protected MultivaluedHashMap<String, Object> getClientAuthHeaders(final UUID devId, final UUID aiid) {
        return new MultivaluedHashMap<String, Object>() {
            {
                put("Authorization", Collections.singletonList("Bearer " + getClientToken(devId, aiid)));
            }
        };
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

    private AbstractBinder getDefaultBindings() {
        return new AbstractBinder() {
            @Override
            protected void configure() {

                // Bind all the external dependencies to mocks
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeConfig)).to(Config.class)
                        .to(IThreadConfig.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseConnectionPool))
                        .to(DatabaseConnectionPool.class).in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabase)).to(Database.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseAi)).to(DatabaseAI.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseMarketplace))
                        .to(DatabaseMarketplace.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseEntitiesIntents))
                        .to(DatabaseEntitiesIntents.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseIntegrations))
                        .to(DatabaseIntegrations.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseBackends))
                        .to(DatabaseBackends.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseTransaction))
                        .to(DatabaseTransaction.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeDatabaseCall)).to(DatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTransactionalDatabaseCall))
                        .to(TransactionalDatabaseCall.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeJerseyClient)).to(JerseyClient.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiChatServices)).to(AIChatServices.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiServices)).to(AIServices.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeTools)).to(Tools.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeWebHooks)).to(WebHooks.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeLogger)).to(ILogger.class)
                        .in(Singleton.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAccessLogger)).to(AccessLogger.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeEntityRecognizer))
                        .to(EntityRecognizerService.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakefacebookConnector))
                        .to(FacebookConnector.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeAiStrings)).to(AiStrings.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeFeatureToggler)).to(FeatureToggler.class);
                bindFactory(new InstanceFactory<>(ServiceTestBase.this.fakeLanguageLogic)).to(LanguageLogic.class);

                // Bind all the internal dependencies to real classes
                bind(JsonSerializer.class).to(JsonSerializer.class);
                bind(HTMLExtractor.class).to(HTMLExtractor.class);
                bind(Validate.class).to(Validate.class);
                bind(RateLimitCheck.class).to(RateLimitCheck.class);
                bind(AIChatServices.class).to(AIChatServices.class);
                bind(JerseyClient.class).to(JerseyClient.class);
                bind(ThreadPool.class).to(ThreadPool.class);
                bind(ThreadSubPool.class).to(IThreadSubPool.class);
                bind(TrackedThreadSubPool.class).to(ITrackedThreadSubPool.class);
                bind(FacebookChatHandler.class).to(FacebookChatHandler.class);
                bind(CsvIntentReader.class).to(CsvIntentReader.class);
                // Bind a mock of HttpServletRequest
                bind(mock(HttpServletRequest.class)).to(HttpServletRequest.class);

                ServiceTestBase.this.addAdditionalBindings(this);
            }
        };
    }

    /**
     * Overrides the default logging.
     * Grizzly uses the standard Java logging which writes to STDERR. This causes
     * some IDEs to fail test runs since the tests wrote to STDERR, even if all the tests pass.
     * This method overrides that behaviour by using a console handler that only writs to STDOUT.
     */
    private void overrideDefaultLogging() {
        Handler handler = new ConsoleHandler() {
            {
                setOutputStream(System.out);
            }
        };
        // Remove all handlers
        LogManager.getLogManager().reset();
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.addHandler(handler);
        rootLogger.setUseParentHandlers(false);
        rootLogger.setLevel(Level.ALL);
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

    protected Invocation.Builder getGsonJerseyClient(final String path) {
        Client client = ClientBuilder.newClient().register(JerseyGsonProvider.class);
        MultivaluedHashMap<String, Object> headers = getDevIdAuthHeaders(Role.ROLE_PLAN_1, DEVID);

        String url = target().getUri() + (path.startsWith("/") ? path.substring(1) : path);
        return client.target(url)
                .request()
                .header(HttpHeaders.AUTHORIZATION, headers.get("Authorization").get(0));
    }

    protected abstract Class<?> getClassUnderTest();

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        return binder;
    }

    @Override
    protected Application configure() {

        overrideDefaultLogging();

        // Use the first available port, to support tests running in parallel
        forceSet(TestProperties.CONTAINER_PORT, "0");

        MockitoAnnotations.initMocks(this);

        // Mock all the external dependencies
        this.fakeDatabase = mock(Database.class);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeDatabaseIntegrations = mock(DatabaseIntegrations.class);
        this.fakeDatabaseBackends = mock(DatabaseBackends.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseConnectionPool = mock(DatabaseConnectionPool.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeTransactionalDatabaseCall = mock(TransactionalDatabaseCall.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeAiChatServices = mock(AIChatServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeAiServices = mock(AIServices.class);
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

        when(this.fakeConfig.getThreadPoolMaxThreads()).thenReturn(16);
        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(5);
        // Set the rate limit frequency to a positive value to allow the tests to run
        when(this.fakeConfig.getRateLimit_QuickRead_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_SaveResource_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_Chat_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_PollStatus_Frequency()).thenReturn(1.0);

        when(this.fakeConfig.getRateLimit_BotstoreMetadata_Frequency()).thenReturn(1.0);
        when(this.fakeConfig.getRateLimit_BotstorePublish_Frequency()).thenReturn(1.0);

        when(this.fakeConfig.getMaxIntentResponses()).thenReturn(10);
        when(this.fakeConfig.getMaxIntentUserSays()).thenReturn(10);

        when(this.fakeConfig.getMaxEntityValuesPerEntity()).thenReturn(100);
        when(this.fakeConfig.getMaxTotalEntityValues()).thenReturn(200);
        when(this.fakeConfig.getLanguagesAvailable()).thenReturn(Arrays.asList("en"));

        when(this.fakeLanguageLogic.getAvailableLanguage(any(String.class), any(), any())).thenReturn(Optional.of(SupportedLanguage.EN));
        when(this.fakeLanguageLogic.getAvailableLanguage(any(Locale.class), any(), any())).thenReturn(Optional.of(SupportedLanguage.EN));

        try {
            when(this.fakeAiStrings.getDefaultChatResponses(any(), any()))
                    .thenReturn(Collections.singletonList(ChatDefaultHandler.COMPLETELY_LOST_RESULT));
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

        // Register the Jersey Gson provider (similarly to what the Core service does)
        rc.register(JerseyGsonProvider.class);

        // Log request and response payload to make debugging easier on errors
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        rc.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");

        rc.register(DebugExceptionMapper.class);

        return rc;
    }

    String getTestsBaseLocation() {
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
            return Response.status(500).entity(Exceptions.getStackTraceAsString(throwable)).type("text/plain").build();
        }
    }
}
