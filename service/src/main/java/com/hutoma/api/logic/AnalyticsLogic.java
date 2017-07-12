package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Analytics logic.
 */
public class AnalyticsLogic {

    private static final String LOGFROM = "analyticslogic";

    private final Config config;
    private final ILogger logger;
    private final JsonSerializer serializer;

    // Immutable map maintains the order of the inserts, so we can use this as the order of fields to show on the CSV
    private static final Map<String, String> CHATLOGS_MAP = ImmutableMap.of(
            "dateTime", "date",
            "params.ChatId", "session",
            "params.Q", "question",
            "params.ResponseSent", "response",
            "params.Score", "score"
    );


    @Inject
    public AnalyticsLogic(final ILogger logger, final Config config, final JsonSerializer serializer) {
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
    }

    private TransportClient getESClient() throws UnknownHostException, MalformedURLException {
        URL url = new URL(config.getElasticSearchAnalyticsUrl());
        String serverName = url.getHost();
        int serverPort = url.getPort();
        Settings settings = Settings.builder()
                .put("cluster.name", "logging-cluster")
                .put("client.transport.ignore_cluster_name", "true").build();
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(
                        serverName),
                        serverPort));
    }

    public ApiResult getChatLogs(final UUID devId, final UUID aiid, final String fromTime, final String toTime,
                                 final AnalyticsResponseFormat format) {
        TransportClient client = null;
        try {
            DateTime dateFrom;
            DateTime dateTo;

            // If start date is not specified default to 30 days ago
            if (fromTime == null || fromTime.isEmpty()) {
                dateFrom = DateTime.now().minusDays(30);
            } else {
                dateFrom = DateTime.parse(fromTime);
            }

            // If end date is not specified, use 'now' otherwise select the last hour+min+sec of the specified day
            if (toTime == null || toTime.isEmpty()) {
                dateTo = DateTime.now();
            } else {
                DateTime tempDate = DateTime.parse(toTime);
                dateTo = new DateTime(tempDate.getYear(), tempDate.getMonthOfYear(), tempDate.getDayOfMonth(),
                        23, 59, 59);
            }

            // Convert the dates to UTC
            dateFrom = dateFrom.toDateTime(DateTimeZone.UTC);
            dateTo = dateTo.toDateTime(DateTimeZone.UTC);

            if (dateTo.isBefore(dateFrom)) {
                this.logger.logUserInfoEvent(LOGFROM, "Request for end date prior to start date",
                        devId.toString(), LogMap.map("aiid", aiid).put("Start", dateFrom.toString())
                                .put("End", dateTo.toString()));
                return ApiError.getBadRequest("End date must be after start date");
            }

            client = getESClient();
            QueryBuilder query = org.elasticsearch.index.query.QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("type.keyword", "TRACE"))
                    .must(QueryBuilders.termQuery("tag.keyword", "chatlogic"))
                    .must(QueryBuilders.termQuery("params.AIID.keyword", aiid.toString()))
                    .must(QueryBuilders.termQuery("params.DevId.keyword", devId.toString()));
            SearchResponse response;
            StringBuilder sb = new StringBuilder();
            final int pageSize = 100;
            int startItem = 0;
            do {
                response = client.prepareSearch("api-chatlog-v1")
                        .setSearchType(SearchType.DEFAULT)
                        .setQuery(query)
                        .setPostFilter(QueryBuilders.rangeQuery("dateTime")
                                .from(dateFrom.toString()).to(dateTo.toString()))
                        .addSort("timestamp", SortOrder.ASC)
                        .setSize(pageSize)
                        .setFrom(startItem)
                        .get();

                boolean includeHeaders = format == AnalyticsResponseFormat.CSV && startItem == 0;
                sb.append(getResultForFormat(response.getHits(), format, CHATLOGS_MAP, includeHeaders));
                startItem += pageSize;
            } while (startItem < response.getHits().totalHits);

            return new ApiString(sb.toString()).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex, LogMap.map("aiid", aiid));
            return ApiError.getInternalServerError();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private String getResultForFormat(final SearchHits hits, final AnalyticsResponseFormat format,
                                      final Map<String, String> fields, final boolean includeHeaders)
            throws AnalyticsResponseException {
        switch (format) {
            case CSV:
                return getResultsAsCsv(hits, fields, includeHeaders);
            case JSON:
                return getResultsAsJson(hits, fields);
            default:
                throw new AnalyticsResponseException("Unsupported format " + format.toString());
        }
    }

    private void flattenMapRec(final Map<String, Object> map, final Map<String, String> stored, final String path) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String newPath = path.isEmpty() ? entry.getKey() : String.format("%s.%s", path, entry.getKey());
            if (entry.getValue() instanceof Map) {
                flattenMapRec(((Map<String, Object>) entry.getValue()), stored, newPath);
            } else {
                stored.put(newPath, entry.getValue().toString());
            }
        }
    }

    private Map<String, String> flattenMap(final Map<String, Object> map) {
        Map<String, String> flatMap = new HashMap<>();
        flattenMapRec(map, flatMap, "");
        return flatMap;
    }

    private Map<String, String> filterMap(final Map<String, String> map, final Map<String, String> filter) {
        Map<String, String> finalMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (filter.containsKey(entry.getKey())) {
                finalMap.put(filter.get(entry.getKey()), entry.getValue());
            }
        }
        return finalMap;
    }

    private List<Map<String, String>> generateResultsMap(final SearchHits hits, final Map<String, String> fields) {
        List<Map<String, String>> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, String> tempMap = flattenMap(hit.getSource());
            list.add(filterMap(tempMap, fields));
        }
        return list;
    }

    private String getResultsAsCsv(final SearchHits hits, final Map<String, String> fields,
                                   final boolean includeHeader) {

        final String EOL = "\r\n";
        List<Map<String, String>> results = generateResultsMap(hits, fields);
        StringBuilder sb = new StringBuilder();
        if (includeHeader) {
            sb.append(StringUtils.join(fields.values(), ","));
            sb.append(EOL);
        }

        for (Map<String, String> result : results) {
            boolean isFirst = true;
            for (String field : fields.values()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(",");
                }
                if (result.containsKey(field)) {
                    sb.append(getCsvFriendlyField(result.get(field)));
                }
            }
            sb.append(EOL);
        }
        return sb.toString();
    }

    private static boolean isNumber(final String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int sz = text.length();
        for (int i = 0; i < sz; ++i) {
            char theChar = text.charAt(i);
            if (!Character.isDigit(theChar) && theChar != '.') {
                return false;
            }
        }
        return true;
    }

    private String getCsvFriendlyField(final String text) {
        final String textQualifier = "\"";
        final String textQualifierEscaped = "\"\"";

        // Check if it's a number
        if (isNumber(text)) {
            return text;
        }

        // Check if it's a date
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setLenient(false);
        try {
            Date date = df.parse(text);
            SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd HH:mm:ss");
            return sdf.format(date);
        } catch (ParseException e) {
            // nothing to do, it's just not a date
        }

        StringBuilder sb = new StringBuilder();
        sb.append(textQualifier);
        sb.append(text.replace(textQualifier, textQualifierEscaped));
        sb.append(textQualifier);
        return sb.toString();
    }

    private String getResultsAsJson(final SearchHits hits, final Map<String, String> fields) {
        List<Map<String, String>> results = generateResultsMap(hits, fields);
        return this.serializer.serialize(results);
    }

    public static class AnalyticsResponseException extends Exception {
        public AnalyticsResponseException(final String message) {
            super(message);
        }
    }
}
