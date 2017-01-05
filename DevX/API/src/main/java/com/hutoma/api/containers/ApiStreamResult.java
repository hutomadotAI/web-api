package com.hutoma.api.containers;

import com.hutoma.api.common.JsonSerializer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * API Result for streaming data.
 */
public class ApiStreamResult extends ApiResult {

    private final StreamingOutput streamingOutput;

    public ApiStreamResult(final StreamingOutput streamingOutput) {
        this.streamingOutput = streamingOutput;
    }

    /**
     * Override the parent's mechanism since when we stream binary data we don't need to
     * use the JSON serializer
     * @param serializer the JSON serializer (not used)
     * @return the response builder
     */
    @Override
    public Response.ResponseBuilder getResponse(JsonSerializer serializer) {
        return Response.status(this.status.getCode()).entity(this.streamingOutput);
    }

    public StreamingOutput getStream() {
        return this.streamingOutput;
    }
}
