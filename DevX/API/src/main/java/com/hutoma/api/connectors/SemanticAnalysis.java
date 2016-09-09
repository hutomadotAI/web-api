package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * Created by David MG on 08/08/2016.
 */
public class SemanticAnalysis {

    Config config;
    Logger logger;
    JsonSerializer serializer;
    Tools tools;

    private final String LOGFROM = "wnetconnector";

    Future<Response> responseFuture;

    public static class SemanticAnalysisException extends Exception {
        public SemanticAnalysisException(Throwable cause) {
            super(cause);
        }
    }

    @Inject
    public SemanticAnalysis(Config config, Logger logger, JsonSerializer serializer, Tools tools) {
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
        this.tools = tools;
    }

    public void startAnswerRequest(String devid, String aiid, String uid, String topic, String history, String q, float min_p) throws SemanticAnalysisException {
        UrlBuilder url = UrlBuilder.fromString(config.getWNetServer())
                .addParameter("q", q)
                .addParameter("aiid", aiid)
                .addParameter("dev_id", devid)
                .addParameter("uid", uid)
                .addParameter("min_p", Float.toString(min_p))
                .addParameter("multiprocess", "yes")
                .addParameter("nproc", "8")
                .addParameter("topic", topic)
                .addParameter("nproc", config.getWnetNumberOfCPUS())
                .addParameter("history", history);
        responseFuture = ClientBuilder.newClient().target(url.toString()).request().async().get();
    }

    public ChatResult getAnswerResult() throws SemanticAnalysisException {
        ChatResult result = null;
        try {
            String content = responseFuture.get().readEntity(String.class);
            result = (ChatResult) serializer.deserialize(content, ChatResult.class);
        } catch (Exception e) {
            throw new SemanticAnalysisException(e);
        }
        return result;
    }

    @Deprecated
    public ChatResult getAnswer(String devid, String aiid, String uid, String history, String topic, String q, float min_p) throws SemanticAnalysisException {
        try {
            UrlBuilder url = UrlBuilder.fromString(config.getWNetServer())
                    .addParameter("q", q)
                    .addParameter("aiid", aiid)
                    .addParameter("dev_id", devid)
                    .addParameter("uid", uid)
                    .addParameter("min_p", Float.toString(min_p))
                    .addParameter("multiprocess", "yes")
                    .addParameter("nproc", "8")
                    .addParameter("topic", topic)
                    .addParameter("nproc", config.getWnetNumberOfCPUS())
                    .addParameter("history", history);
            URL finalUrl = new URL(url.toString());
            InputStream stream = finalUrl.openStream();
            ChatResult result = (ChatResult) serializer.deserialize(stream, ChatResult.class);
            return result;
        } catch (JsonParseException jp) {
            logger.logError(LOGFROM, "failed to deserialize json result from SemanticAnalysis server: ");
            throw new SemanticAnalysisException(jp);
        } catch (IOException e) {
            logger.logError(LOGFROM, "failed to contact SemanticAnalysis server: " + e.toString());
            throw new SemanticAnalysisException(e);
        }
    }

}