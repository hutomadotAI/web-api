package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.ResultEventList;
import com.hutoma.api.containers.sub.Status;

import java.net.HttpURLConnection;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiError extends ApiResult {

    /***
     * Create an error result
     * @param code http error code
     * @param info some textual description of what just happened
     * @return an error response
     */
    protected static ApiError getError(int code, String info) {
        return ApiError.getError(code, info, null);
    }

    /***
     * Create an error result with additionalInfo in the form of an array of resultevents
     * @param code http error code
     * @param info some textual description of what just happened
     * @param errors the resultevents
     * @return an error response
     */
    protected static ApiError getError(int code, String info, ResultEventList errors) {
        ApiError error = new ApiError();
        error.status = (new Status()).setCode(code).setInfo(info).setAdditionalInfo(errors);
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
        return ApiError.getInternalServerError("Internal Server Error");
    }

    public static ApiError getPayloadTooLarge() {
        return ApiError.getError(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, "Payload Too Large");
    }

    public static ApiError getInvalidCharacters() {
        return ApiError.getError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Characters");
    }

    /***
     * Create en error response for a bad request, with a list of resultevents to detail what went wrong
     * @param reason textual representation of what went wrong
     * @param errorDetails list of resultevents
     * @return error response
     */
    public static ApiError getBadRequest(String reason, ResultEventList errorDetails) {
        return ApiError.getError(HttpURLConnection.HTTP_BAD_REQUEST, reason, errorDetails);
    }

    /***
     * General case of bad request, with a textual reason
     * @param reason textual representation of what went wrong
     * @return error response
     */
    public static ApiError getBadRequest(String reason) {
        return ApiError.getBadRequest(reason, null);
    }

    /***
     * General case of bad request
     * @return error response
     */
    public static ApiError getBadRequest() {
        return ApiError.getBadRequest("Bad Request", null);
    }
    
    public static ApiError getNotFound(String message) {
        return ApiError.getError(HttpURLConnection.HTTP_NOT_FOUND, message);
    }

    public static ApiError getNotFound() {
        return ApiError.getNotFound("Not Found");
    }

    public static ApiError getRateLimited() {
        return ApiError.getError(429, "Too Many Requests");
    }

    public static ApiError getAccountDisabled() {
        return ApiError.getError(HttpURLConnection.HTTP_FORBIDDEN, "Account Not Valid");
    }

    public static ApiError getConflict(String message) {
        return ApiError.getError(HttpURLConnection.HTTP_CONFLICT, message);
    }
}
