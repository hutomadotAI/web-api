package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiAi;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Manages AI related strings.
 */
public class AiStrings {

    private final Database database;
    private final JsonSerializer serializer;
    private final Random randomGenerator;

    @Inject
    public AiStrings(final Database database, final JsonSerializer serializer) {
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

    public class AiStringsException extends Exception {
        public AiStringsException(final String message) {
            super(message);
        }

        public AiStringsException(final Exception ex) {
            super(ex);
        }
    }
}
