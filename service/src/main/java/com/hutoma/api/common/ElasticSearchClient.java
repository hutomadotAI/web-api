package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;

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
}
