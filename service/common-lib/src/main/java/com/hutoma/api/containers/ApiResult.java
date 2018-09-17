package com.hutoma.api.containers;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.sub.Status;

import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

/**
 * API result base class
 */
public class ApiResult {

    protected Status status;

    public ApiResult() {
    }

    public Status getStatus() {
        return this.status;
    }

    public Response.ResponseBuilder getResponse(JsonSerializer serializer) {
         return Response.status(this.status.getCode())
                 .encoding(StandardCharsets.UTF_8.name())
                 .entity(serializer.serialize(this));
    }

    public ApiResult setCreatedStatus() {
        this.status = Status.getCreated();
        return this;
    }

    public ApiResult setCreatedStatus(String message) {
        return setCreatedStatus(message, null);
    }

    public ApiResult setCreatedStatus(String message, Object additionalInfo) {
        this.status = additionalInfo == null
                ? Status.getCreated(message) : Status.getCreated(message, additionalInfo);
        return this;
    }

    public ApiResult setSuccessStatus() {
        this.status = Status.getSuccess();
        return this;
    }

    public ApiResult setSuccessStatus(String message) {
        return setSuccessStatus(message, null);
    }

    public ApiResult setSuccessStatus(String message, Object additionalInfo) {
        this.status = additionalInfo == null
                ? Status.getSuccess(message) : Status.getSuccess(message, additionalInfo);
        return this;
    }

}
