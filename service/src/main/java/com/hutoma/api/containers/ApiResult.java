package com.hutoma.api.containers;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.sub.Status;

import javax.ws.rs.core.Response;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiResult {

    Status status;

    public ApiResult() {
    }

    public Status getStatus() {
        return this.status;
    }

    public Response.ResponseBuilder getResponse(JsonSerializer serializer) {
        return Response.status(this.status.getCode()).entity(serializer.serialize(this));
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
