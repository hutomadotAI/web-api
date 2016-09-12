package com.hutoma.api.containers;

import com.hutoma.api.validation.Validate;
import com.hutoma.api.containers.sub.Status;

import java.net.HttpURLConnection;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiError extends ApiResult {

    protected static ApiError getError(int code, String info) {
        ApiError error = new ApiError();
        error.status = (new Status()).setCode(code).setInfo(info);
        return error;
    }

    /**
     * Use with care. Don't give too much information about what is happening internally.
     * For most situations, use the version of this call without a parameter
     * @param reason
     * @return
     */
    public static ApiError getInternalServerError(String reason) {
        return ApiError.getError(HttpURLConnection.HTTP_INTERNAL_ERROR, reason);
    }

    public static ApiError getInternalServerError() {
        return ApiError.getInternalServerError("Internal server error");
    }

    public static ApiError getPayloadTooLarge() {
        return ApiError.getError(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, "Payload too large");
    }

    public static ApiError getBadRequest(String reason) {
        return ApiError.getError(HttpURLConnection.HTTP_BAD_REQUEST, reason);
    }

    public static ApiError getBadRequest() {
        return ApiError.getBadRequest("Bad request");
    }

    public static ApiError getBadRequest(Validate.ParameterValidationException pve) {
        return ApiError.getBadRequest(pve.getMessage());
    }

    public static ApiError getNotFound(String message) {
        return ApiError.getError(HttpURLConnection.HTTP_NOT_FOUND, message);
    }

    public static ApiError getNotFound() {
        return ApiError.getNotFound("not found");
    }

    public static ApiError getNoResponse(String message) {
        return ApiError.getError(HttpURLConnection.HTTP_ACCEPTED, message);
    }

}
