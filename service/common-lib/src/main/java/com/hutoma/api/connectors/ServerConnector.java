package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

/**
 * Server connector.
 */
public class ServerConnector {

    private static final String LOGFROM = "serverconnector";

    protected final JsonSerializer serializer;
    protected final JerseyClient jerseyClient;
    protected final ILogger logger;
    protected final Tools tools;
    protected final TrackedThreadSubPool threadSubPool;
    protected final IConnectConfig connectConfig;

    @Inject
    public ServerConnector(final ILogger logger, IConnectConfig connectConfig,
                           final JsonSerializer serializer,
                           final Tools tools, final JerseyClient jerseyClient,
                           final TrackedThreadSubPool threadSubPool) {
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
        this.jerseyClient = jerseyClient;
        this.threadSubPool = threadSubPool;
        this.jerseyClient.register(MultiPartWriter.class);
        this.connectConfig = connectConfig;
    }

    public void abandonCalls() {
        this.threadSubPool.cancelAll();
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

        public static void throwWithSuppressed(String message, Exception suppressed) throws AiServicesException {
            AiServicesException ex = new AiServicesException(message);
            ex.addSuppressed(suppressed);
            throw ex;
        }

        public int getResponseStatus() {
            return this.responseStatus;
        }
    }

    private InvocationResult waitFor(String callName, HashMap<String, Future<InvocationResult>> futures)
            throws AiServicesException, ExecutionException, InterruptedException, TimeoutException {

        Future<InvocationResult> future = futures.get(callName);
        if (future == null) {
            throw new AiServicesException(String.format("Call %s not found", callName));
        }
        return future.get(connectConfig.getBackendTrainingCallTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    private HashMap<String, Future<InvocationResult>> execute(
            final HashMap<String, Callable<InvocationResult>> callables) {
        this.logger.logDebug(LOGFROM, String.format("Issuing %d requests for %s",
                callables.size(), this.tools.getCallerMethod(3)));
        HashMap<String, Future<InvocationResult>> futures = new HashMap<>();

        // get a named future for a named callable response
        callables.forEach((key, value) -> {
            futures.put(key, this.threadSubPool.submit(value));
        });

        return futures;
    }

    protected void executeAndWait(final HashMap<String, Callable<InvocationResult>> callables)
            throws AiServicesException {

        HashMap<String, Future<InvocationResult>> futures = null;
        try {
            // start the calls async
            futures = execute(callables);

            List<InvocationResult> errors = new ArrayList<>();
            for (String endpoint : callables.keySet()) {
                // wait for each call to terminate
                InvocationResult response = waitFor(endpoint, futures);

                // Consume the input stream
                boolean hasEntity = response.getResponse().bufferEntity();
                Response.StatusType statusInfo = response.getResponse().getStatusInfo();
                // aggregate the errors
                if (statusInfo.getStatusCode() != HttpURLConnection.HTTP_OK) {
                    errors.add(response);
                    LogMap logMap = LogMap.map("Status", statusInfo.getStatusCode())
                            .put("Reason", statusInfo.getReasonPhrase());
                    if (hasEntity) {
                        logMap.add("Response", response.getResponse().getEntity());
                    }
                    this.logger.logError(LOGFROM, String.format("Failure status %s (id=%d msg=%s)",
                            endpoint,
                            statusInfo.getStatusCode(),
                            statusInfo.getReasonPhrase()),
                            logMap);
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
            throw new AiServicesException(e.toString());
        } finally {
            if (futures != null) {
                futures.values().forEach((future) -> {
                    try {
                        // attempt to close the connection
                        Response response = future.isDone()
                                ? future.get().getResponse()
                                : future.get(0, TimeUnit.MILLISECONDS).getResponse();
                        response.bufferEntity(); // It's ok to call buffer entity multiple times
                        response.close();
                    } catch (Exception e) {
                        this.logger.logError(LOGFROM, String.format("Failed to close http connection after use: %s",
                                e.toString()));
                    }
                });
            }
        }
        this.logger.logDebug(LOGFROM, String.format("All %d calls executed successfully", callables.size()));
    }

    /***
     * Assemble endpoint url to start or stop training
     * @param devId
     * @param aiid
     * @param endpoint
     * @param params
     * @return
     * @throws AiServicesException
     */
    protected HashMap<String, Callable<InvocationResult>> getTrainingCallableForEndpoint(
            final UUID devId, final UUID aiid, final String endpoint, Map<String, String> params)
            throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        JerseyWebTarget target = this.jerseyClient.target(endpoint)
                .path(devId.toString())
                .path(aiid.toString())
                .property(CONNECT_TIMEOUT, (int) this.connectConfig.getBackendConnectCallTimeoutMs())
                .property(READ_TIMEOUT, (int) this.connectConfig.getBackendTrainingCallTimeoutMs());
        for (Map.Entry<String, String> param : params.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }

        final JerseyInvocation.Builder builder = target.request();
        callables.put(endpoint, () -> new InvocationResult(builder.post(null), endpoint,
                0, 0, 1, aiid));
        return callables;
    }
}
