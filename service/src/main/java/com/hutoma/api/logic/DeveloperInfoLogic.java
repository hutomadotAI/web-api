package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.validation.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
            LogMap logMap = LogMap.map("DevId", requestDevId);
            DeveloperInfo info = this.database.getDeveloperInfo(requestDevId);
            if (info == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - dev not found", authDevId, logMap);
                return ApiError.getNotFound(String.format("Developer %s not found", requestDevId));
            }
            if (authDevId.equalsIgnoreCase(requestDevId)) {
                // Developer is requesting it's own details, so all data can be sent
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - own details", authDevId, logMap);
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                // Developer is requesting details from another developer, so only public info can be sent
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - other dev details", authDevId, logMap);
                return new ApiDeveloperInfo(info.getPublicInfo()).setSuccessStatus();
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetDeveloperInfo", authDevId, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult setDeveloperInfo(final String devId, final String name, final String company, final String email,
                                      final String address, final String postCode, final String city,
                                      final String country, final String website) {
        String[] array = {name, company, email, address, postCode, city, country};
        if (Validate.isAnyNullOrEmpty(array)) {
            List<String> emptyParams = Arrays.stream(array)
                    .filter(x -> x != null && !x.isEmpty())
                    .collect(Collectors.toList());
            String emptyParamAsString = String.join(",", emptyParams);
            this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - null or empty params", devId,
                    LogMap.map("Params", emptyParamAsString));
            return ApiError.getBadRequest(String.format("Parameters %s cannot be null or empty", emptyParamAsString));
        }

        try {
            DeveloperInfo info = this.database.getDeveloperInfo(devId);
            if (info != null) {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - already submitted", devId);
                return ApiError.getBadRequest("Developer info already submitted");
            }
            info = new DeveloperInfo(devId, name, company, email, address, postCode, city, country, website);
            if (this.database.setDeveloperInfo(info)) {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo", devId);
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - dev not found", devId);
                return ApiError.getNotFound(String.format("Developer %s not found", devId));
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "SetDeveloperInfo", devId, ex);
            return ApiError.getInternalServerError();
        }
    }
}
