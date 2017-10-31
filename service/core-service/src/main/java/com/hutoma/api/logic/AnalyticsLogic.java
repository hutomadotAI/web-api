package com.hutoma.api.logic;

import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.connectors.AnalyticsESConnector;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiListMap;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Analytics logic.
 */
public class AnalyticsLogic {

    private static final String LOGFROM = "analyticslogic";
    private static final String END_DATE_BEFORE_START_MSG = "End date must be after start date";

    static final int DEFAULT_DATE_FROM = 30; // last 30 days

    private final AnalyticsESConnector connector;
    private final Config config;
    private final ILogger logger;
    private final JsonSerializer serializer;


    @Inject
    public AnalyticsLogic(final AnalyticsESConnector connector, final ILogger logger, final Config config,
                          final JsonSerializer serializer) {
        this.connector = connector;
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
    }

    public ApiResult getChatLogs(final UUID devId, final UUID aiid, final String fromTime, final String toTime,
                                 final AnalyticsResponseFormat format) {
        try {
            DateTime dateFrom = getDateFrom(fromTime);
            DateTime dateTo = getDateTo(toTime);
            if (!isDateIntervalValid(devId, aiid, dateFrom, dateTo)) {
                return ApiError.getBadRequest(END_DATE_BEFORE_START_MSG);
            }

            String logs = this.connector.getChatLogs(devId, aiid, dateFrom, dateTo, format);
            return new ApiString(logs).setSuccessStatus();

        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex, LogMap.map("aiid", aiid));
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSessions(final UUID devId, final UUID aiid, final String fromTime, final String toTime) {
        try {
            DateTime dateFrom = getDateFrom(fromTime);
            DateTime dateTo = getDateTo(toTime);
            if (!isDateIntervalValid(devId, aiid, dateFrom, dateTo)) {
                return ApiError.getBadRequest(END_DATE_BEFORE_START_MSG);
            }

            List<Map<String, Object>> listMap = this.connector.getSessions(devId, aiid, dateFrom, dateTo);
            return new ApiListMap(listMap).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex, LogMap.map("aiid", aiid));
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getInteractions(final UUID devId, final UUID aiid, final String fromTime, final String toTime) {
        try {
            DateTime dateFrom = getDateFrom(fromTime);
            DateTime dateTo = getDateTo(toTime);
            if (!isDateIntervalValid(devId, aiid, dateFrom, dateTo)) {
                return ApiError.getBadRequest(END_DATE_BEFORE_START_MSG);
            }

            List<Map<String, Object>> listMap = this.connector.getInteractions(devId, aiid, dateFrom, dateTo);
            return new ApiListMap(listMap).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex, LogMap.map("aiid", aiid));
            return ApiError.getInternalServerError();
        }
    }

    static DateTime getDateFrom(final String fromTime) {
        DateTime dateFrom;
        // If start date is not specified default to 30 days ago
        if (fromTime == null || fromTime.isEmpty()) {
            dateFrom = DateTime.now().minusDays(DEFAULT_DATE_FROM);
        } else {
            dateFrom = DateTime.parse(fromTime);
        }
        // Convert the date to UTC
        return dateFrom.toDateTime(DateTimeZone.UTC);
    }

    static DateTime getDateTo(final String toTime) {
        DateTime dateTo;
        // If end date is not specified, use 'now' otherwise select the last hour+min+sec of the specified day
        if (toTime == null || toTime.isEmpty()) {
            dateTo = DateTime.now();
        } else {
            DateTime tempDate = DateTime.parse(toTime);
            dateTo = new DateTime(tempDate.getYear(), tempDate.getMonthOfYear(), tempDate.getDayOfMonth(),
                    23, 59, 59);
        }
        // Convert the date to UTC
        return dateTo.toDateTime(DateTimeZone.UTC);
    }

    private boolean isDateIntervalValid(final UUID devId, final UUID aiid, final DateTime dateFrom,
                                        final DateTime dateTo) {
        if (dateTo.isBefore(dateFrom)) {
            this.logger.logUserInfoEvent(LOGFROM, "Request for end date prior to start date",
                    devId.toString(), LogMap.map("aiid", aiid).put("Start", dateFrom.toString())
                            .put("End", dateTo.toString()));
            return false;
        }
        return true;
    }
}
