package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;

import javax.inject.Inject;

/**
 * Logic for the invite code endpoints.
 */
public class InviteLogic {

    private static final String LOGFROM = "invitelogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final ILogger logger;

    @Inject
    public InviteLogic(Config config, JsonSerializer jsonSerializer, Database database,
                       ILogger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
    }

    /**
     * Determines whether a provided invite code is valid.
     * @param code the invite code to validate.
     * @return an ApiResult denoting the success or failure.
     */
    public ApiResult validCode(final String code) {
        try {
            if (this.database.inviteCodeValid(code)) {
                this.logger.logUserTraceEvent(LOGFROM, "InviteValidCode", AdminLogic.ADMIN_DEVID_LOG);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "InviteValidCode - not found", AdminLogic.ADMIN_DEVID_LOG,
                        "Code", code);
                return ApiError.getNotFound();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "InviteValidCode", AdminLogic.ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Applies the invite code for a new registering user.
     * @param code     the invite code to redeem.
     * @param username the username to associate with the invite code.
     * @return an ApiResult denoting the success or failure.
     */
    public ApiResult redeemCode(String code, String username) {
        try {
            if (this.database.inviteCodeValid(code)) {
                if (this.database.redeemInviteCode(code, username)) {
                    this.logger.logUserTraceEvent(LOGFROM, "InviteRedeemCode", AdminLogic.ADMIN_DEVID_LOG,
                            "Code", code, "Username", username);
                    return new ApiResult().setCreatedStatus("Invite code redeemed.");
                }
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "InviteRedeemCode - invalid code", AdminLogic.ADMIN_DEVID_LOG,
                        "Code", code, "Username", username);
                return ApiError.getBadRequest("Invalid invite code.");
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "InviteRedeemCode", AdminLogic.ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }

        this.logger.logUserTraceEvent(LOGFROM, "InviteRedeemCode - could not redeem code", AdminLogic.ADMIN_DEVID_LOG,
                "Code", code, "Username", username);
        return ApiError.getInternalServerError();
    }
}
