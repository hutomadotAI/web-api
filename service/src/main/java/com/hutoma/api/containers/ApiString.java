package com.hutoma.api.containers;

import com.hutoma.api.common.JsonSerializer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * String API result.
 */
public class ApiString extends ApiResult {

    private final String str;

    public ApiString(final String str) {
        this.str = str;
    }

    /**
     * Override the parent's mechanism since when we stream binary data we don't need to
     * use the JSON serializer
     * @param serializer the JSON serializer (not used)
     * @return the response builder
     */
    @Override
    public Response.ResponseBuilder getResponse(JsonSerializer serializer) {
        return Response.ok().type(MediaType.TEXT_PLAIN).entity(this.str);
    }

    public String getString() {
        return this.str;
    }
}
