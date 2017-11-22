package com.hutoma.api.logic;

import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.containers.ApiDeveloperInfo;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.validation.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Developer Information logic.
 */
public class DeveloperInfoLogic {

    private static final String LOGFROM = "developerinfologic";

    private final ILogger logger;
    private final DatabaseMarketplace database;

    @Inject
    public DeveloperInfoLogic(final ILogger logger, final DatabaseMarketplace database) {
        this.logger = logger;
        this.database = database;
    }

    public ApiResult getDeveloperInfo(final UUID authDevId, final UUID requestDevId) {
        final String authDevIdString = authDevId.toString();
        final String requestDevIdString = requestDevId.toString();
        try {
            LogMap logMap = LogMap.map("DevId", requestDevId);
            DeveloperInfo info = this.database.getDeveloperInfo(requestDevId);
            if (info == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - dev not found", authDevIdString, logMap);
                return ApiError.getNotFound(String.format("Developer %s not found", requestDevId));
            }
            if (authDevIdString.equalsIgnoreCase(requestDevIdString)) {
                // Developer is requesting it's own details, so all data can be sent
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - own details", authDevIdString, logMap);
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                // Developer is requesting details from another developer, so only public info can be sent
                this.logger.logUserTraceEvent(LOGFROM, "GetDeveloperInfo - other dev details", authDevIdString, logMap);
                return new ApiDeveloperInfo(info.getPublicInfo()).setSuccessStatus();
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetDeveloperInfo", authDevIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult setDeveloperInfo(final UUID devId, final String name, final String company, final String email,
                                      final String address, final String postCode, final String city,
                                      final String country, final String website) {
        String[] array = {name, company, email, address, postCode, city, country};
        final String devIdString = devId.toString();
        if (Validate.isAnyNullOrEmpty(array)) {
            List<String> emptyParams = Arrays.stream(array)
                    .filter(x -> x != null && !x.isEmpty())
                    .collect(Collectors.toList());
            String emptyParamAsString = String.join(",", emptyParams);
            this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - null or empty params", devIdString,
                    LogMap.map("Params", emptyParamAsString));
            return ApiError.getBadRequest(String.format("Parameters %s cannot be null or empty", emptyParamAsString));
        }

        try {
            DeveloperInfo info = this.database.getDeveloperInfo(devId);
            if (info != null) {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - already submitted", devIdString);
                return ApiError.getBadRequest("Developer info already submitted");
            }
            info = new DeveloperInfo(devId, name, company, email, address, postCode, city, country, website);
            if (this.database.setDeveloperInfo(info)) {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo", devIdString);
                return new ApiDeveloperInfo(info).setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "SetDeveloperInfo - dev not found", devIdString);
                return ApiError.getNotFound(String.format("Developer %s not found", devId));
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "SetDeveloperInfo", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }
}
