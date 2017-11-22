package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.containers.sub.RecognizedEntity;

import org.glassfish.jersey.client.JerseyClient;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Connector to the Entity Recognizer Service.
 */
public class EntityRecognizerService {

    private static final String LOGFROM = "entityrec";
    private final ILogger logger;
    private final JsonSerializer serializer;
    private final Config config;
    private final JerseyClient jerseyClient;
    private final String entityRecognizerUrl;

    @Inject
    public EntityRecognizerService(final ILogger logger, final JsonSerializer serializer, final Config config,
                                   final JerseyClient jerseyClient) {
        this.logger = logger;
        this.serializer = serializer;
        this.config = config;
        this.jerseyClient = jerseyClient;
        this.entityRecognizerUrl = this.config.getEntityRecognizerUrl();
    }

    /**
     * Gets the list of recognized entities from the given question.
     * @param question the question
     * @return the list of recognized entities
     */
    public List<RecognizedEntity> getEntities(final String question) {
        Response response = null;
        try {
            response = this.jerseyClient.target(this.entityRecognizerUrl)
                    .queryParam("q", question)
                    .request()
                    .get();
            response.bufferEntity();
            String json = response.readEntity(String.class);
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                this.logger.logError(LOGFROM,
                        "Error connecting to entity recognizer at " + this.entityRecognizerUrl,
                        LogMap.map("server", this.entityRecognizerUrl)
                                .put("Status", Integer.toString(response.getStatus())));
            } else {
                List<RecognizedEntity> list = RecognizedEntity.deserialize(json, this.serializer);
                this.logger.logDebug(LOGFROM, String.format("Obtained list of %d recognized entities", list.size()),
                        LogMap.map("NumEntities", list.size()));
                return list;
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return new ArrayList<>();
    }
}
