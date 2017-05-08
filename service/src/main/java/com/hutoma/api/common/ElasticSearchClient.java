package com.hutoma.api.common;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import org.glassfish.jersey.client.JerseyClient;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * HTTP client for elastic search.
 */
public class ElasticSearchClient {

    private static final String DOCUMENT_TYPE = "log";

    private final JerseyClient jerseyClient;
    private final String elasticSearchUrl;

    public ElasticSearchClient(final JerseyClient jerseyClient, final String elasticSearchUrl) {
        this.jerseyClient = jerseyClient;
        this.elasticSearchUrl = elasticSearchUrl;
    }

    /**
     * Upload documents to ES in bulk.
     * ES supports the following pairing pattern:
     * { action1 }
     * { document1 }
     * { action2 }
     * { document2 }
     * The action line instructs what to do regarding the next line which contains the document.
     * @param indexName the index name
     * @param jsonDocs  the list of json documents to upload
     * @return the service response
     */
    public Response uploadDocumentBulk(final String indexName, final List<String> jsonDocs) {
        StringBuilder sb = new StringBuilder();
        for (String doc : jsonDocs) {
            sb.append("{\"index\":{}}\n");
            // NOTE: The JSON document *should not* be pretty-printed as this will create a multi-line entry
            sb.append(doc.replace("\n", ""));
            sb.append("\n");
        }

        // Upload to server/indexName/docType/_bulk
        return this.jerseyClient
                .target(String.format("%s/%s/%s/_bulk", this.elasticSearchUrl, indexName, DOCUMENT_TYPE))
                .request()
                .post(Entity.text(sb.toString()));
    }


    public static class BulkResponse {
        @SerializedName("items")
        private List<LinkedTreeMap<String, Object>> items;

        @SerializedName("errors")
        private boolean errors;

        public List<String> getErrors(final int expectedNumItems) {
            List<String> errList = new ArrayList<>();
            if (expectedNumItems != this.items.size()) {
                errList.add(String.format("Unexpected number of items. Expected: %s, actual: %s",
                        expectedNumItems, this.items.size()));
            }
            if (this.errors) {
                int pos = 1;
                for (LinkedTreeMap<String, Object> item : this.items) {
                    LinkedTreeMap<String, Object> index = (LinkedTreeMap<String, Object>) item.get("index");
                    int status = (int) index.get("status");
                    if (status != HttpURLConnection.HTTP_CREATED) {
                        errList.add(String.format("item at position %d ws not created and has status %d", pos, status));
                    }
                    pos++;
                }
            }
            return errList;
        }

    }
}
