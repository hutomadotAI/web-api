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
        return status;
    }

    public Response.ResponseBuilder getResponse(JsonSerializer serializer) {
        return Response.status(status.getCode()).entity(serializer.serialize(this));
    }

    public ApiResult setSuccessStatus() {
        this.status = Status.getSuccess();
        return this;
    }

    public ApiResult setSuccessStatus(String message) {
        this.status = Status.getSuccess(message);
        return this;
    }

}