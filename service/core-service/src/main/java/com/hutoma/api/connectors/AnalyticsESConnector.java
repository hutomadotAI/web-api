package com.hutoma.api.connectors;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class AnalyticsESConnector {

    private static final String CHATLOGS_INDEX = "api-chatlog-v1";
    private static final String CHATLOGS_DATETIME_FIELD = "dateTime";
    private static final String RESULT_DATE_FIELD_NAME = "date";
    private static final String RESULT_COUNT_FIELD_NAME = "count";
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final Config config;
    private final JsonSerializer jsonSerializer;

    // Immutable map maintains the order of the inserts, so we can use this as the order of fields to show on the CSV
    private static final Map<String, String> CHATLOGS_MAP = ImmutableMap.of(
            CHATLOGS_DATETIME_FIELD, "date",
            "params.ChatId", "session",
            "params.Q", "question",
            "params.ResponseSent", "response",
            "params.Score", "score"
    );

    @Inject
    public AnalyticsESConnector(final Config config, final JsonSerializer jsonSerializer) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
    }

    public String getChatLogs(final UUID devId, final UUID aiid, final DateTime dateFrom, final DateTime dateTo,
                                 final AnalyticsResponseFormat format)
            throws AnalyticsConnectorException {
        TransportClient client = null;
        try {
            client = getESClient();
            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);
            SearchResponse response;
            StringBuilder sb = new StringBuilder();
            int startItem = 0;
            do {
                response = client.prepareSearch(CHATLOGS_INDEX)
                        .setSearchType(SearchType.DEFAULT)
                        .setQuery(query)
                        .addSort("timestamp", SortOrder.ASC)
                        .setSize(DEFAULT_PAGE_SIZE)
                        .setFrom(startItem)
                        .get();

                boolean includeHeaders = format == AnalyticsResponseFormat.CSV && startItem == 0;
                sb.append(getResultForFormat(response.getHits(), format, CHATLOGS_MAP, includeHeaders));
                startItem += DEFAULT_PAGE_SIZE;
            } while (startItem < response.getHits().totalHits);
            return sb.toString();
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public List<Map<String, Object>> getSessions(final UUID devId, final UUID aiid, final DateTime dateFrom,
                                                 final DateTime dateTo)
            throws AnalyticsConnectorException {
        TransportClient client = null;
        try {
            client = getESClient();
            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);
            SearchRequestBuilder requestBuilder = client.prepareSearch(CHATLOGS_INDEX)
                    .setSearchType(SearchType.DEFAULT)
                    .setQuery(query)
                    .addAggregation(
                            AggregationBuilders.dateHistogram("by-date")
                                    .field(CHATLOGS_DATETIME_FIELD)
                                    .dateHistogramInterval(DateHistogramInterval.DAY)
                                    .subAggregation(
                                            AggregationBuilders.cardinality("ChatId")
                                                    .field("params.ChatId.keyword")
                                                    .precisionThreshold(100)));

            List<Map<String, Object>> listMap = new ArrayList<>();
            SearchResponse response;
            final int pageSize = DEFAULT_PAGE_SIZE;
            int startItem = 0;
            do {
                response = requestBuilder
                        .setSize(pageSize)
                        .setFrom(startItem)
                        .get();

                Histogram dateHistogram = response.getAggregations().get("by-date");
                for (Histogram.Bucket bucket : dateHistogram.getBuckets()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(RESULT_DATE_FIELD_NAME, bucket.getKeyAsString());
                    Cardinality cardinality = bucket.getAggregations().get("ChatId");
                    map.put(RESULT_COUNT_FIELD_NAME, cardinality.getValue());
                    listMap.add(map);
                }
                startItem += pageSize;
            } while (startItem < response.getHits().totalHits);


            return listMap;
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public List<Map<String, Object>> getInteractions(final UUID devId, final UUID aiid, final DateTime dateFrom,
                                     final DateTime dateTo)
            throws AnalyticsConnectorException {
        TransportClient client = null;
        try {
            client = getESClient();
            QueryBuilder query = getChatLogsTermQuery(devId, aiid, dateFrom, dateTo);
            SearchRequestBuilder requestBuilder = client.prepareSearch(CHATLOGS_INDEX)
                    .setSearchType(SearchType.DEFAULT)
                    .setQuery(query)
                    .addAggregation(AggregationBuilders.dateHistogram("by-date")
                            .field(CHATLOGS_DATETIME_FIELD).dateHistogramInterval(DateHistogramInterval.DAY));
            List<Map<String, Object>> listMap = new ArrayList<>();
            SearchResponse response;
            final int pageSize = 100;
            int startItem = 0;
            do {
                response = requestBuilder
                        .setSize(pageSize)
                        .setFrom(startItem)
                        .get();

                Histogram dateHistogram = response.getAggregations().get("by-date");
                for (Histogram.Bucket bucket : dateHistogram.getBuckets()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(RESULT_DATE_FIELD_NAME, bucket.getKeyAsString());
                    map.put(RESULT_COUNT_FIELD_NAME, bucket.getDocCount());
                    listMap.add(map);
                }

                startItem += pageSize;
            } while (startItem < response.getHits().totalHits);
            return listMap;
        } catch (Exception ex) {
            throw new AnalyticsConnectorException(ex);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

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

    private TransportClient getESClient() throws UnknownHostException, MalformedURLException {
        URL url = new URL(this.config.getElasticSearchAnalyticsUrl());
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
        public AnalyticsConnectorException(final String message) {
            super(message);
        }

        public AnalyticsConnectorException(final Exception ex) {
            super(ex);
        }
    }
}
