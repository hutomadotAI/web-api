package com.hutoma.api.connectors;

import java.util.UUID;
import javax.ws.rs.core.Response;

public class InvocationResult {
    private final Response response;
    private final String endpoint;
    private final long durationMs;
    private final long chatCallDurationMs;
    private final int attemptNumber;
    private final UUID aiid;

    public InvocationResult(final Response response, final String endpoint, final long durationMs,
                            final long chatCallDurationMs, final int attemptNumber, final UUID aiid) {
        this.response = response;
        this.endpoint = endpoint;
        this.durationMs = durationMs;
        this.chatCallDurationMs = chatCallDurationMs;
        this.attemptNumber = attemptNumber;
        this.aiid = aiid;
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

    public long getChatCallDurationMs() {
        return chatCallDurationMs;
    }

    public int getattemptNumber() {
        return attemptNumber;
    }
}
