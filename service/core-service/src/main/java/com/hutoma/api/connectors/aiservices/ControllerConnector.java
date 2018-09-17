package com.hutoma.api.connectors.aiservices;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiServerEndpoint;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.ApiServerTrackerInfoMap;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

/**
 * Connector for the AI Backend Management Controller.
 */
public abstract class ControllerConnector {

    private static final String LOGFROM = "controllerconnector";
    private static final String PARAM_SERVER_TYPE = "serverType";
    private static final String PARAM_SERVER_LANGUAGE = "serverLanguage";
    private static final String PARAM_SERVER_VERSION = "serverVersion";
    private static final int CONNECTION_TIMEOUT = 1000; // 1s
    private static final int READ_TIMEOUT = 1000; // 1s
    private static final String CONTROLLER_ENDPOINT = "controller";

    private final Config config;
    private final JerseyClient jerseyClient;
    private final ILogger logger;
    private final Tools tools;

    @Inject
    public ControllerConnector(final Config config,
                               final JerseyClient jerseyClient,
                               final ILogger logger,
                               final Tools tools) {
        this.config = config;
        this.jerseyClient = jerseyClient;
        this.logger = logger;
        this.tools = tools;
    }


    public abstract BackendServerType getServerType();

    void kickQueueProcessor(final SupportedLanguage language, final String serverVersion) {
        this.kickQueue(buildServiceIdentityFromParams(language, serverVersion));
    }

    /**
     * Gets from the controller a training backend endpoint for a given AI.
     *
     * @param aiIdentity       the AIID
     * @param serializer the json serialiser
     * @return the endpoint information
     * @throws NoServerAvailableException if there are no servers available to process this request
     */
    IServerEndpoint getBackendTrainingEndpoint(final AiIdentity aiIdentity, final JsonSerializer serializer)
            throws NoServerAvailableException {
        return getBackendTrainingEndpoint(aiIdentity, this.getServerType(), serializer);
    }

    /**
     * Gets from the controller a training backend endpoint for a given AI.
     *
     * @param aiIdentity            the AIID
     * @param serverType the server type
     * @param serializer      the json serialiser
     * @return the endpoint information
     * @throws NoServerAvailableException if there are no servers available to process this request
     */
    IServerEndpoint getBackendTrainingEndpoint(final AiIdentity aiIdentity,
                                               final BackendServerType serverType,
                                               final JsonSerializer serializer)
            throws NoServerAvailableException {
        if (aiIdentity == null || aiIdentity.getAiid() == null) {
            throw new IllegalArgumentException("aiIdentity");
        }
        ServiceIdentity serviceIdentity = buildServiceIdentityFromAi(aiIdentity);
        LogMap logMap = LogMap.map("AIID", aiIdentity.getAiid().toString())
                .put("RequestFor", RequestFor.Training.toString())
                .put("ServerType", serverType.value())
                .put("Language", aiIdentity.getLanguage())
                .put("Version", aiIdentity.getServerVersion());
        final long startTimestamp = this.tools.getTimestamp();
        try (Response response = getRequest(String.format("/%s/training", aiIdentity.getAiid().toString()),
                    getMapOfServiceIdentity(serviceIdentity))
                    .get()) {
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                ApiServerEndpoint result = (ApiServerEndpoint) serializer.deserialize(
                        (InputStream) response.getEntity(), ApiServerEndpoint.class);

                this.logger.logPerf(LOGFROM, "GetBackendEndpoint", logMap
                        .put("Duration", this.tools.getTimestamp() - startTimestamp));
                return result.asServerEndpoint();
            }

            throw new NoServerAvailableException(String.format("No server available for %s for %s on %s",
                    aiIdentity.getAiid().toString(), RequestFor.Training.toString(), serviceIdentity.toString()));

        } catch (NoServerAvailableException ex) {
            // rethrow
            throw ex;
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex, logMap);
        }
        return null;
    }

    /***
     * For each bot in a list, get a chat endpoint and a valid hashcode for the instance
     * @param multiRequest
     * @return the map of AIIDs and respective responses from the possible endpoints
     * @throws ChatBackendConnector.AiControllerException
     */
    public Map<UUID, ApiServerEndpointMulti.ServerEndpointResponse> getBackendChatEndpointMulti(
            final ServerEndpointRequestMulti multiRequest,
            final JsonSerializer serializer)
            throws ChatBackendConnector.AiControllerException {
        if (multiRequest == null) {
            throw new IllegalArgumentException("multiRequest");
        }
        LogMap logMap = LogMap.map("RequestFor", RequestFor.Chat.toString())
                .put("ServerType", this.getServerType().value());

        // for now just assume the language and server version are all the same
        SupportedLanguage language = multiRequest.getEndpointRequests().get(0).getLanguage();
        String serverVersion = multiRequest.getEndpointRequests().get(0).getServerVersion();

        final long startTimestamp = this.tools.getTimestamp();
        try (Response response = getRequest("/chatEndpoints",
                getMapOfServiceIdentity(buildServiceIdentityFromParams(language, serverVersion)))
                    .post(Entity.json(serializer.serialize(multiRequest)))) {

            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                ApiServerEndpointMulti result = (ApiServerEndpointMulti) serializer.deserialize(
                        (InputStream) response.getEntity(), ApiServerEndpointMulti.class);

                this.logger.logPerf(LOGFROM, "GetServerEndpointMulti", logMap
                        .put("Duration", this.tools.getTimestamp() - startTimestamp));
                return result.getEndpointMap();
            }
            throw new ChatBackendConnector.AiControllerException(
                    String.format("getBackendChatEndpointMulti returned Error %d", response.getStatus()));
        } catch (ChatBackendConnector.AiControllerException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new ChatBackendConnector.AiControllerException(ex);
        }
    }

    Map<String, ServerTrackerInfo> getVerifiedEndpointMap(final SupportedLanguage supportedLanguage,
                                                          final String version,
                                                          final JsonSerializer serializer) {
        return getVerifiedEndpointMap(buildServiceIdentityFromParams(supportedLanguage, version), serializer);
    }

    /**
     * Gets a map of the verified endpoints for a given backend server type.
     *
     * @param serviceIdentity the backend server type
     * @return the map
     */
    private Map<String, ServerTrackerInfo> getVerifiedEndpointMap(final ServiceIdentity serviceIdentity,
                                                                  final JsonSerializer serializer) {
        LogMap logMap = LogMap.map("ServerType", serviceIdentity.getServerType().value())
                .put("Language", serviceIdentity.getLanguage().toString())
                .put("Version", serviceIdentity.getVersion());
        final long startTimestamp = this.tools.getTimestamp();
        try (Response response = getRequest("/endpointMap", getMapOfServiceIdentity(serviceIdentity))
                    .get()) {
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                ApiServerTrackerInfoMap result = (ApiServerTrackerInfoMap) serializer.deserialize(
                        (InputStream) response.getEntity(), ApiServerTrackerInfoMap.class);
                this.logger.logPerf(LOGFROM, "GetVerifiedEndpointMap",
                        logMap.put("Duration", this.tools.getTimestamp() - startTimestamp));
                return result.getMap();
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex, logMap);
        }

        return new HashMap<>();
    }

    private Invocation.Builder getRequest(final String path,
                                          final Map<String, String> queryParams,
                                          final int connectionTimeout,
                                          final int readTimeout) {
        JerseyWebTarget target = this.jerseyClient.target(this.config.getControllerEndpoint())
                .path(CONTROLLER_ENDPOINT);
        if (path != null && !path.isEmpty()) {
            target = target.path(path);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Invocation.Builder request = target.request();
        request.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        request.property(ClientProperties.READ_TIMEOUT, readTimeout);
        return request;
    }

    private Invocation.Builder getRequest(final String path, final Map<String, String> queryParams) {
        return this.getRequest(path, queryParams, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    private void kickQueue(final ServiceIdentity serviceIdentity) {
        LogMap logMap = LogMap.map("ServerType", serviceIdentity.getServerType().value())
                .put("Language", serviceIdentity.getLanguage().toString())
                .put("Version", serviceIdentity.getVersion());
        final long startTimestamp = this.tools.getTimestamp();
        try (Response response = getRequest("/queue", getMapOfServiceIdentity(serviceIdentity))
                    .post(Entity.text(""))) {
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                this.logger.logPerf(LOGFROM, "KickQueue",
                        logMap.put("Duration", this.tools.getTimestamp() - startTimestamp));
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex, logMap);
        }
    }

    private static Map<String, String> getMapOfServiceIdentity(final ServiceIdentity serviceIdentity) {
        return ImmutableMap.of(
                PARAM_SERVER_TYPE, serviceIdentity.getServerType().value(),
                PARAM_SERVER_LANGUAGE, serviceIdentity.getLanguage().name(),
                PARAM_SERVER_VERSION, serviceIdentity.getVersion());
    }

    private ServiceIdentity buildServiceIdentityFromAi(final AiIdentity aiIdentity) {
        return buildServiceIdentityFromParams(aiIdentity.getLanguage(), aiIdentity.getServerVersion());
    }

    private ServiceIdentity buildServiceIdentityFromParams(final SupportedLanguage language,
                                                           final String serverVersion) {
        return new ServiceIdentity(this.getServerType(), language, serverVersion);
    }
}
