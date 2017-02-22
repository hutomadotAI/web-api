package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Server connector.
 */
public class ServerConnector {

    private static final String LOGFROM = "serverconnector";

    // As a workaround to bug 1152, we need to allow more time for WNET to process
    // training data.
    // For a 700kB file this is around 20-25s. Reduce this timeout again once WNET
    // processes training data in the training phase, not the upload phase.
    private static final int TIMEOUT_SECONDS = 30;

    protected final JsonSerializer serializer;
    protected final Database database;
    protected final Config config;
    protected final JerseyClient jerseyClient;
    protected final ILogger logger;
    protected final Tools tools;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Inject
    public ServerConnector(final Database database, final ILogger logger, final JsonSerializer serializer,
                           final Tools tools, final Config config, final JerseyClient jerseyClient) {
        this.database = database;
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
        this.config = config;
        this.jerseyClient = jerseyClient;
        this.jerseyClient.register(MultiPartWriter.class);
    }

    public void abandonCalls() {
        this.executor.shutdownNow();
    }

    public static class InvocationResult {
        private final Response response;
        private final String endpoint;
        private final long durationMs;

        public InvocationResult(final Response response, final String endpoint, final long durationMs) {
            this.response = response;
            this.endpoint = endpoint;
            this.durationMs = durationMs;
        }

        public Response getResponse() {
            return this.response;
        }

        public long getDurationMs() {
            return this.durationMs;
        }

        public String getEndpoint() {
            return this.endpoint;
        }
    }

    public static class AiServicesException extends Exception {
        private int responseStatus = 0;

        public AiServicesException(String message) {
            super(message);
        }

        public AiServicesException(String message, int responseStatus) {
            this(message);
            this.responseStatus = responseStatus;
        }

        public int getResponseStatus() {
            return this.responseStatus;
        }
    }

    protected InvocationResult waitFor(String callName, HashMap<String, Future<InvocationResult>> futures)
            throws AiServicesException, ExecutionException, InterruptedException, TimeoutException {
        if (!futures.containsKey(callName)) {
            throw new AiServicesException(String.format("Call %s not found", callName));
        }
        return futures.get(callName).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    protected HashMap<String, Future<InvocationResult>> execute(
            final HashMap<String, Callable<InvocationResult>> callables) {
        this.logger.logDebug(LOGFROM, String.format("Issuing %d requests for %s",
                callables.size(), this.tools.getCallerMethod(3)));
        HashMap<String, Future<InvocationResult>> futures = new HashMap<>();

        // get a named future for a named callable response
        callables.entrySet().forEach((entry) -> {
            futures.put(entry.getKey(), this.executor.submit(entry.getValue()));
        });

        return futures;
    }

    void executeAndWait(final HashMap<String, Callable<InvocationResult>> callables) throws AiServicesException {
        try {
            // start the calls async
            HashMap<String, Future<InvocationResult>> futures = execute(callables);

            List<InvocationResult> errors = new ArrayList<>();
            for (String endpoint : callables.keySet()) {
                // wait for each call to terminate
                InvocationResult response = waitFor(endpoint, futures);

                Response.StatusType statusInfo = response.getResponse().getStatusInfo();
                // aggregate the errors
                if (statusInfo.getStatusCode() != HttpURLConnection.HTTP_OK) {
                    errors.add(response);
                    this.logger.logError(LOGFROM, String.format("Failure status %s (id=%d msg=%s)",
                            endpoint,
                            statusInfo.getStatusCode(),
                            statusInfo.getReasonPhrase()));
                }
            }
            if (!errors.isEmpty()) {
                AiServicesException ex = new AiServicesException("Errors found");
                for (InvocationResult r : errors) {
                    ex.addSuppressed(new AiServicesException(
                            String.format("%s for %s",
                                    r.getResponse().getStatusInfo().getReasonPhrase(),
                                    r.getEndpoint()),
                            r.getResponse().getStatusInfo().getStatusCode()));
                }
                throw ex;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AiServicesException(e.getMessage());
        }
        this.logger.logDebug(LOGFROM, String.format("All %d calls executed successfully", callables.size()));
    }
}
