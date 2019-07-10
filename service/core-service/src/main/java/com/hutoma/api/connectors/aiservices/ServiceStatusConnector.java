package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.containers.ApiServersAvailable;
import com.hutoma.api.logging.ILogger;
import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;


public class ServiceStatusConnector extends ControllerConnector {

    @Inject
    public ServiceStatusConnector(Config config, JerseyClient jerseyClient, ILogger logger, Tools tools) {
        super(config, jerseyClient, logger, tools);
    }

    @Override
    public BackendServerType getServerType() {
        return null;
    }

    /***
     * Call controller instance to get a list of available services
     * @param serializer
     * @return
     */
    public ApiServersAvailable getServiceIdentities(final JsonSerializer serializer) {

        try (Response response = getRequest("/health/services", Collections.emptyMap()).get()) {
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                response.bufferEntity();
                ApiServersAvailable result = (ApiServersAvailable) serializer.deserialize(
                        (InputStream) response.getEntity(), ApiServersAvailable.class);
                return result;
            } else {
                this.logger.logDebug(LOGFROM,
                        String.format("ctrl health/services failed with HTTP %d", response.getStatus()));
            }
        } catch (Exception ex) {
            this.logger.logDebug(LOGFROM,
                    String.format("ctrl health/services failed with %s", ex.toString()));
        }
        return new ApiServersAvailable(Collections.emptyList());
    }

}
