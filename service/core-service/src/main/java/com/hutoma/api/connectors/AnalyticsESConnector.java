package com.hutoma.api.connectors;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.logging.ILogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnalyticsESConnector {

    private static final String CHATLOGS_INDEX = "api-chatlog-v1";
    private static final String CHATLOGS_DATETIME_FIELD = "dateTime";
    private static final String RESULT_DATE_FIELD_NAME = "date";
    private static final String RESULT_COUNT_FIELD_NAME = "count";
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final String LOGFROM = "analyticsesconnector";

    private final Config config;
    private final JsonSerializer jsonSerializer;
    private RestHighLevelClient restClient;
    private ILogger logger;

    // Immutable map maintains the order of the inserts, so we can use this as the order of fields to show on the CSV
    private static final Map<String, String> CHATLOGS_MAP = ImmutableMap.of(
            CHATLOGS_DATETIME_FIELD, "date",
            "params.ChatId", "session",
            "params.Q", "question",
            "params.ResponseSent", "response",
            "params.Score", "score"
    );

    @Inject
    AnalyticsESConnector(final Config config,
                         final JsonSerializer jsonSerializer,
                         final ILogger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.logger = logger;
    }

    public String getChatLogs(final UUID devId, final UUID aiid, final DateTime dateFrom, final DateTime dateTo,
                              final AnalyticsResponseFormat format)
            throws AnalyticsConnectorException {
        try {
            RestHighLevelClient client = getESClient();
            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);
            SearchResponse response;
            StringBuilder sb = new StringBuilder();
            int startItem = 0;
            SearchRequest searchRequest = new SearchRequest(CHATLOGS_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            do {
                searchSourceBuilder.query(query);
                searchSourceBuilder.from(startItem);
                searchSourceBuilder.size(DEFAULT_PAGE_SIZE);
                searchSourceBuilder.sort(CHATLOGS_DATETIME_FIELD, SortOrder.ASC);
                searchRequest.source(searchSourceBuilder);

                response = client.search(searchRequest);

                boolean includeHeaders = format == AnalyticsResponseFormat.CSV && startItem == 0;
                sb.append(getResultForFormat(response.getHits(), format, CHATLOGS_MAP, includeHeaders));
                startItem += DEFAULT_PAGE_SIZE;
            } while (startItem < response.getHits().totalHits);
            return sb.toString();
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        }
    }

    public List<Map<String, Object>> getSessions(final UUID devId, final UUID aiid, final DateTime dateFrom,
                                                 final DateTime dateTo)
            throws AnalyticsConnectorException {

        try {

            DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("by-date")
                    .field(CHATLOGS_DATETIME_FIELD)
                    .dateHistogramInterval(DateHistogramInterval.DAY)
                    .subAggregation(
                            AggregationBuilders.cardinality("ChatId")
                                    .field("params.ChatId.keyword")
                                    .precisionThreshold(100));

            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.aggregation(aggregation);
            searchSourceBuilder.query(query);

            SearchRequest searchRequest = new SearchRequest(CHATLOGS_INDEX);
            searchRequest.source(searchSourceBuilder);
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));

            List<Map<String, Object>> listMap = new ArrayList<>();
            RestHighLevelClient client = getESClient();
            SearchResponse response = client.search(searchRequest);
            String scrollId = response.getScrollId();

            getSessionsAggregationByDate(response, listMap);
            boolean hasMoreResults = response.getHits().totalHits == DEFAULT_PAGE_SIZE;
            while (hasMoreResults) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueSeconds(30));
                SearchResponse searchScrollResponse = client.searchScroll(scrollRequest);
                scrollId = searchScrollResponse.getScrollId();
                getSessionsAggregationByDate(searchScrollResponse, listMap);
                hasMoreResults = searchScrollResponse.getHits().totalHits == DEFAULT_PAGE_SIZE;
            }
            return listMap;
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        }
    }

    private void getSessionsAggregationByDate(final SearchResponse response, List<Map<String, Object>> listMap) {
        Histogram dateHistogram = response.getAggregations().get("by-date");
        for (Histogram.Bucket bucket : dateHistogram.getBuckets()) {
            Map<String, Object> map = new HashMap<>();
            map.put(RESULT_DATE_FIELD_NAME, bucket.getKeyAsString());
            Cardinality cardinality = bucket.getAggregations().get("ChatId");
            map.put(RESULT_COUNT_FIELD_NAME, cardinality.getValue());
            listMap.add(map);
        }
    }

    public List<Map<String, Object>> getInteractions(final UUID devId, final UUID aiid, final DateTime dateFrom,
                                                     final DateTime dateTo)
            throws AnalyticsConnectorException {
        try {


            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);
            DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("by-date")
                    .field(CHATLOGS_DATETIME_FIELD).dateHistogramInterval(DateHistogramInterval.DAY);

            searchSourceBuilder.query(query);
            searchSourceBuilder.aggregation(aggregation);
            searchSourceBuilder.size(DEFAULT_PAGE_SIZE);

            SearchRequest searchRequest = new SearchRequest(CHATLOGS_INDEX);
            searchRequest.source(searchSourceBuilder);
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));

            List<Map<String, Object>> listMap = new ArrayList<>();
            RestHighLevelClient client = getESClient();
            SearchResponse response = client.search(searchRequest);
            String scrollId = response.getScrollId();
            getInteractionsAggregationByDate(response, listMap);
            boolean hasMoreResults = response.getHits().totalHits == DEFAULT_PAGE_SIZE;
            while (hasMoreResults) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueSeconds(30));
                SearchResponse searchScrollResponse = client.searchScroll(scrollRequest);
                scrollId = searchScrollResponse.getScrollId();
                getInteractionsAggregationByDate(searchScrollResponse, listMap);
                hasMoreResults = searchScrollResponse.getHits().totalHits == DEFAULT_PAGE_SIZE;
            }
            return listMap;
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        }
    }

    private void getInteractionsAggregationByDate(final SearchResponse response, List<Map<String, Object>> listMap) {
        Histogram dateHistogram = response.getAggregations().get("by-date");
        for (Histogram.Bucket bucket : dateHistogram.getBuckets()) {
            Map<String, Object> map = new HashMap<>();
            map.put(RESULT_DATE_FIELD_NAME, bucket.getKeyAsString());
            map.put(RESULT_COUNT_FIELD_NAME, bucket.getDocCount());
            listMap.add(map);
        }
    }

    @SuppressWarnings("unchecked")
    static void flattenMapRec(final Map<String, Object> map, final Map<String, String> stored, final String path) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String newPath = path.isEmpty() ? entry.getKey() : String.format("%s.%s", path, entry.getKey());
            if (entry.getValue() instanceof Map) {

                flattenMapRec(((Map<String, Object>) entry.getValue()), stored, newPath);
            } else {
                stored.put(newPath, entry.getValue() == null ? null : entry.getValue().toString());
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

    private RestHighLevelClient getESClient() throws UnknownHostException, MalformedURLException {
        if (this.restClient == null) {
            URL url = new URL(this.config.getElasticSearchAnalyticsUrl());
            String serverName = url.getHost();
            int serverPort = url.getPort();
            this.restClient = new RestHighLevelClient(
                    RestClient.builder(
                        new HttpHost(serverName, serverPort, "http")));
            try {
                if (!this.restClient.ping()) {
                    this.logger.logError(LOGFROM, "Could not ping ES server");
                    throw new UnknownHostException(String.format("%s:%d", serverName, serverPort));
                }
            } catch (IOException ex) {
                this.logger.logException(LOGFROM, ex);
                UnknownHostException newEx = new UnknownHostException();
                newEx.addSuppressed(ex);
                throw newEx;
            }
        }
        return this.restClient;
    }

    private static QueryBuilder getChatLogsTermQuery(final UUID devId, final UUID aiid, final DateTime from,
                                                     final DateTime to) {
        return org.elasticsearch.index.query.QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("type.keyword", "TRACE"))
                .must(QueryBuilders.termQuery("@log_name.keyword", "API-chatlog-v1.chatlogic"))
                .must(QueryBuilders.termQuery("params.AIID.keyword", aiid.toString()))
                .must(QueryBuilders.termQuery("params.DevId.keyword", devId.toString()))
                .must(QueryBuilders.rangeQuery(CHATLOGS_DATETIME_FIELD).from(from.toString()).to(to.toString()));
    }

    private String getResultForFormat(final SearchHits hits, final AnalyticsResponseFormat format,
                                      final Map<String, String> fields, final boolean includeHeaders)
            throws AnalyticsConnectorException {
        switch (format) {
            case CSV:
                return getResultsAsCsv(hits, fields, includeHeaders);
            case JSON:
                return getResultsAsJson(hits, fields);
            default:
                throw new AnalyticsConnectorException("Unsupported format " + format.toString());
        }
    }

    private List<Map<String, String>> generateResultsMap(final SearchHits hits, final Map<String, String> fields) {
        List<Map<String, String>> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, String> tempMap = flattenMap(hit.getSourceAsMap());
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
                    sb.append(Tools.getCsvFriendlyField(result.get(field)));
                }
            }
            sb.append(EOL);
        }
        return sb.toString();
    }

    private String getResultsAsJson(final SearchHits hits, final Map<String, String> fields) {
        List<Map<String, String>> results = generateResultsMap(hits, fields);
        return this.jsonSerializer.serialize(results);
    }

    public static class AnalyticsConnectorException extends Exception {
        AnalyticsConnectorException(final String message) {
            super(message);
        }

        AnalyticsConnectorException(final Exception ex) {
            super(ex);
        }
    }
}
