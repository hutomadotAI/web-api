package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;

import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Created by David MG on 08/08/2016.
 */
public class SemanticAnalysis {

    private final Config config;
    private final ILogger logger;
    private final JsonSerializer serializer;
    private final Tools tools;
    private Future<Response> responseFuture;

    @Inject
    public SemanticAnalysis(Config config, ILogger logger, JsonSerializer serializer, Tools tools) {
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
    }

    public void startAnswerRequest(String devid, UUID aiid, UUID chatId, String topic, String history,
                                   String question, float minP) throws SemanticAnalysisException {
        UrlBuilder url = UrlBuilder.fromString(this.config.getWNetServer())
                .addParameter("q", question)
                .addParameter("aiid", aiid.toString())
                .addParameter("dev_id", devid)
                .addParameter("uid", chatId.toString())
                .addParameter("min_p", Float.toString(minP))
                .addParameter("multiprocess", "yes")
                .addParameter("nproc", "8")
                .addParameter("topic", topic)
                .addParameter("nproc", this.config.getWnetNumberOfCPUS())
                .addParameter("history", history);
        this.responseFuture = ClientBuilder.newClient().target(url.toString()).request().async().get();
    }

    public ChatResult getAnswerResult() throws SemanticAnalysisException {
        ChatResult result = null;
        try {
            String content = this.responseFuture.get().readEntity(String.class);
            result = new ChatResult((ChatResult) this.serializer.deserialize(content, ChatResult.class));
        } catch (Exception e) {
            throw new SemanticAnalysisException(e);
        }
        return result;
    }

    public static class SemanticAnalysisException extends Exception {
        public SemanticAnalysisException(Throwable cause) {
            super(cause);
        }
    }

}