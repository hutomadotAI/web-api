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

    public ApiResult getDeveloperInfo(final String devId) {
        try {
            DeveloperInfo info = this.database.getDeveloperInfo(devId);
            return new ApiDeveloperInfo(info).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            return ApiError.getNotFound(String.format("Developer %s not found", devId));
        }
    }

    public ApiResult setDeveloperInfo(final String devId, final String name, final String company, final String email,
                                      final String address, final String postCode, final String city,
                                      final String country, final String website) {
        if (Validate.isAnyNullOrEmpty(name, company, email, address, postCode, city, country)) {
            return ApiError.getBadRequest("At least one of the required parameters is null or empty");
        }

        DeveloperInfo info = new DeveloperInfo(devId, name, company, email, address, postCode, city, country, website);
        try {
            if (this.database.setDeveloperInfo(info)) {
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                return ApiError.getNotFound(String.format("Developer %s not found", devId));
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }
}
