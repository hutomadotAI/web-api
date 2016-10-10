package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by David MG on 08/08/2016.
 */
public class SemanticAnalysis {

    private final String LOGFROM = "wnetconnector";
    Config config;
    Logger logger;
    JsonSerializer serializer;
    Tools tools;
    Future<Response> responseFuture;

    @Inject
    public SemanticAnalysis(Config config, Logger logger, JsonSerializer serializer, Tools tools) {
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
    }

    public void startAnswerRequest(String devid, UUID aiid, UUID chatId, String topic, String history, String q, float min_p) throws SemanticAnalysisException {
        UrlBuilder url = UrlBuilder.fromString(this.config.getWNetServer())
            .addParameter("q", q)
            .addParameter("aiid", aiid.toString())
            .addParameter("dev_id", devid)
            .addParameter("uid", chatId.toString())
            .addParameter("min_p", Float.toString(min_p))
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
            result = (ChatResult) this.serializer.deserialize(content, ChatResult.class);
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