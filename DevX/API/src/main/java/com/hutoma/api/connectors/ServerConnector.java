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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Created by pedrotei on 07/11/16.
 */
public class ServerConnector {

    private static final String LOGFROM = "serverconnector";
    protected final JsonSerializer serializer;
    protected final Database database;
    protected final Config config;
    protected final JerseyClient jerseyClient;
    protected final ILogger logger;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Tools tools;

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
        executor.shutdownNow();
    }

    public static class AiServicesException extends Exception {
        public AiServicesException(String message) {
            super(message);
        }
    }

    protected Response waitFor(String callName, HashMap<String, Future<Response>> futures) throws AiServicesException, ExecutionException, InterruptedException {
        if (!futures.containsKey(callName)) {
            throw new AiServicesException(String.format("Call %s not found", callName));
        }
        return futures.get(callName).get();
    }

    protected HashMap<String, Future<Response>> execute(final HashMap<String, Callable<Response>> callables) {
        this.logger.logDebug(LOGFROM, String.format("Issuing %d requests for %s",
                callables.size(), this.tools.getCallerMethod()));
        HashMap<String, Future<Response>> futures = new HashMap<>();

        // get a named future for a named callable response
        callables.entrySet().forEach((entry) -> {
            futures.put(entry.getKey(), this.executor.submit(entry.getValue()));
        });

        return futures;
    }

    void executeAndWait(final HashMap<String, Callable<Response>> callables) throws AiServicesException {
        try {
            // start the calls async
            HashMap<String, Future<Response>> futures = execute(callables);

            List<String> errors = new ArrayList<>();
            for (String endpoint : callables.keySet()) {
                // wait for each call to terminate
                Response response = waitFor(endpoint, futures);

                // aggregate the errors
                if (response.getStatusInfo().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    errors.add(String.format("%d %s",
                            response.getStatusInfo().getStatusCode(),
                            response.getStatusInfo().getReasonPhrase()));
                    this.logger.logError(LOGFROM, String.format("Failure status %s (id=%d msg=%s)",
                            endpoint,
                            response.getStatusInfo().getStatusCode(),
                            response.getStatusInfo().getReasonPhrase()));
                }
            }
            if (!errors.isEmpty()) {
                throw new AiServicesException(errors.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(";")));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new AiServicesException(e.getMessage());
        }
    }
}
