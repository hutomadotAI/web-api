package com.hutoma.api.connectors;

import java.util.UUID;
import javax.ws.rs.core.Response;

/**
 * Created by pedrotei on 30/01/17.
 */
public class InvocationResult {
    private final Response response;
    private final String endpoint;
    private final long durationMs;
    private final UUID aiid;

    public InvocationResult(final UUID aiid, final Response response, final String endpoint, final long durationMs) {
        this.aiid = aiid;
        this.response = response;
        this.endpoint = endpoint;
        this.durationMs = durationMs;
    }

    public Response getResponse() {
        return this.response;
    }

    public long getDurationMs() {
        return this.durationMs;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public UUID getAiid() {
        return this.aiid;
    }
}
