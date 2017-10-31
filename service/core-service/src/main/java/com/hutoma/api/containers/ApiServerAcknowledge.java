package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Response sent by the API to the back-end server
 * To give it a session ID
 */
public class ApiServerAcknowledge extends ApiResult {

    @SerializedName("server_session_id")
    UUID serverSessionID;

    public ApiServerAcknowledge(final UUID serverSessionID) {
        this.serverSessionID = serverSessionID;
    }
}
