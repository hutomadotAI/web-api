package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.validation.Validate;

import javax.inject.Inject;

/**
 * Developer Information logic.
 */
public class DeveloperInfoLogic {

    private static final String LOGFROM = "developerinfologic";

    private final ILogger logger;
    private final Database database;

    @Inject
    public DeveloperInfoLogic(final ILogger logger, final Database database) {
        this.logger = logger;
        this.database = database;
    }

    public ApiResult getDeveloperInfo(final String authDevId, final String requestDevId) {
        try {
            DeveloperInfo info = this.database.getDeveloperInfo(requestDevId);
            if (info == null) {
                return ApiError.getNotFound(String.format("Developer %s not found", requestDevId));
            }
            if (authDevId.equalsIgnoreCase(requestDevId)) {
                // Developer is requesting it's own details, so all data can be sent
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                // Developer is requesting details from another developer, so only public info can be sent
                return new ApiDeveloperInfo(info.getPublicInfo()).setSuccessStatus();
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult setDeveloperInfo(final String devId, final String name, final String company, final String email,
                                      final String address, final String postCode, final String city,
                                      final String country, final String website) {
        if (Validate.isAnyNullOrEmpty(name, company, email, address, postCode, city, country)) {
            return ApiError.getBadRequest("At least one of the required parameters is null or empty");
        }

        try {
            DeveloperInfo info = this.database.getDeveloperInfo(devId);
            if (info != null) {
                return ApiError.getBadRequest("Developer info already submitted");
            }
            info = new DeveloperInfo(devId, name, company, email, address, postCode, city, country, website);
            if (this.database.setDeveloperInfo(info)) {
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                return ApiError.getNotFound(String.format("Developer %s not found", devId));
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }
}
