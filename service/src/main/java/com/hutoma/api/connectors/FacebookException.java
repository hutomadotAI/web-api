package com.hutoma.api.connectors;

import com.google.common.base.Strings;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.JsonSerializer;

public class FacebookException extends Exception {

    public FacebookException() {
    }

    public FacebookException(String genericError) {
        super(genericError);
    }

    /***
     * Analyse the error we get back from Facebook and return
     * the appropriate object type
     * @param httpErrorCode
     * @param httpError
     * @param response
     * @param deserializer
     * @return
     */
    public static FacebookException exceptionMapper(
            int httpErrorCode, String httpError, String response, JsonSerializer deserializer) {
        FacebookErrorResponse errorResponse = null;
        try {
            if (!Strings.isNullOrEmpty(response)) {
                errorResponse = (FacebookErrorResponse)
                        deserializer.deserialize(response, FacebookErrorResponse.class);
            }
        } catch (JsonParseException jpe) {
            // fallthrough to nullcheck
        }
        if ((errorResponse == null) || (errorResponse.error == null)) {
            // if this was a 200 then something else failed
            if ((httpErrorCode == 200)) {
                return new FacebookException("Failed to parse error data");
            }
            // otherwise it's an http error, probably not from facebook
            return new FacebookHttpException(httpErrorCode, httpError);
        }
        // at this point we have data back from Graph API
        if ("OAuthException".equals(errorResponse.error.errorType)
                || "OAuthAccessTokenException".equals(errorResponse.error.errorType)) {
            return new FacebookAuthException(httpErrorCode, httpError,
                    errorResponse.error.errorType,
                    errorResponse.error.message,
                    errorResponse.error.code);
        }
        return new FacebookGraphException(httpErrorCode, httpError,
                errorResponse.error.errorType,
                errorResponse.error.message,
                errorResponse.error.code);
    }

    public static class FacebookHttpException extends FacebookException {

        protected int httpErrorCode;
        protected String httpError;

        public FacebookHttpException(int httpErrorCode, String httpError) {
            this.httpError = httpError;
            this.httpErrorCode = httpErrorCode;
        }

        @Override
        public String getMessage() {
            return String.format("%d: %s", this.httpErrorCode, this.httpError);
        }
    }

    public static class FacebookGraphException extends FacebookHttpException {

        protected String facebookErrorType;
        protected String facebookErrorMessage;
        protected int facebookErrorCode;

        public FacebookGraphException(final int httpErrorCode, final String httpError,
                                      final String facebookErrorType, final String facebookErrorMessage,
                                      final int facebookErrorCode) {
            super(httpErrorCode, httpError);
            this.facebookErrorType = facebookErrorType;
            this.facebookErrorMessage = facebookErrorMessage;
            this.facebookErrorCode = facebookErrorCode;
        }

        @Override
        public String getMessage() {
            return String.format("%s %d: %s",
                    this.facebookErrorType, this.facebookErrorCode, this.facebookErrorMessage);
        }

        public String getFacebookErrorMessage() {
            return this.facebookErrorMessage;
        }
    }

    public static class FacebookAuthException extends FacebookGraphException {
        public FacebookAuthException(final int httpErrorCode, final String httpError,
                                     final String facebookErrorType, final String facebookErrorMessage,
                                     final int facebookErrorCode) {
            super(httpErrorCode, httpError, facebookErrorType, facebookErrorMessage, facebookErrorCode);
        }
    }

    public static class FacebookMissingPermissionsException extends FacebookException {
        public FacebookMissingPermissionsException(final String genericError) {
            super(genericError);
        }
    }

    private class FacebookErrorResponse {
        @SerializedName("error")
        FacebookError error;
    }

    private static class FacebookError {
        @SerializedName("code")
        int code;
        @SerializedName("message")
        String message;
        @SerializedName("type")
        String errorType;
    }
}

