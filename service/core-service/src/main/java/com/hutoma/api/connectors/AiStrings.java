package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.containers.ApiAi;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Manages AI related strings.
 */
public class AiStrings {

    private final DatabaseAI database;
    private final JsonSerializer serializer;
    private final Random randomGenerator;

    @Inject
    AiStrings(final DatabaseAI database, final JsonSerializer serializer) {
        this.database = database;
        this.serializer = serializer;
        randomGenerator = new Random();
    }

    public List<String> getDefaultChatResponses(final UUID devId, final UUID aiid) throws AiStringsException {
        try {
            ApiAi ai = this.database.getAI(devId, aiid, this.serializer);
            return ai.getDefaultChatResponses();
        } catch (Exception ex) {
            throw new AiStringsException(ex);
        }
    }

    public String getRandomDefaultChatResponse(final UUID devId, final UUID aiid) throws AiStringsException {
        List<String> responses = this.getDefaultChatResponses(devId, aiid);
        if (responses.isEmpty()) {
            throw new AiStringsException("Empty default response");
        }
        int index = randomGenerator.nextInt(responses.size());
        return responses.get(index);
    }

    public static class AiStringsException extends Exception {
        AiStringsException(final String message) {
            super(message);
        }

        AiStringsException(final Exception ex) {
            super(ex);
        }
    }
}
