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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.*;

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
    private final String findEntityUrl;

    @Inject
    public EntityRecognizerService(final ILogger logger, final JsonSerializer serializer, final Config config,
                                   final JerseyClient jerseyClient) {
        this.logger = logger;
        this.serializer = serializer;
        this.config = config;
        this.jerseyClient = jerseyClient;
        this.entityRecognizerUrl = this.config.getEntityRecognizerUrl();
        this.findEntityUrl = this.config.getFindEntityUrl();
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

    public String findEntities(final String question) {
        Response response = null;
        String json = null;
        try {
            response = this.jerseyClient.target(this.findEntityUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(javax.ws.rs.client.Entity.entity(question, MediaType.APPLICATION_JSON));
            response.bufferEntity();
            json = response.readEntity(String.class);
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                this.logger.logError(LOGFROM,
                        "Error connecting to entity recognizer at " + this.findEntityUrl,
                        LogMap.map("server", this.entityRecognizerUrl)
                                .put("Status", Integer.toString(response.getStatus())));
            } else {
                return json;
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            throw ex;
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return json;
    }
}

