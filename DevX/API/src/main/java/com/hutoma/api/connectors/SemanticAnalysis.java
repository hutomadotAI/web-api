package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by David MG on 08/08/2016.
 */
public class SemanticAnalysis {

    Config config;
    Logger logger;
    JsonSerializer serializer;

    private final String LOGFROM = "wnetconnector";

    public static class SemanticAnalysisException extends Exception {
        public SemanticAnalysisException(Throwable cause) {
            super(cause);
        }
    }

    @Inject
    public SemanticAnalysis(Config config, Logger logger, JsonSerializer serializer) {
        this.config = config;
        this.logger = logger;
        this.serializer = serializer;
    }

    public ChatResult getAnswer(String devid, String aiid, String q, float min_p, boolean fastSearch) throws SemanticAnalysisException {
        try {
            UrlBuilder url = UrlBuilder.fromString(config.getWNetServer())
                    .addParameter("q", q)
                    .addParameter("aiid", aiid)
                    .addParameter("uid", devid)
                    .addParameter("min_p", Float.toString(min_p))
                    .addParameter("multiprocess", "yes")
                    .addParameter("nproc", "8");
            if (fastSearch) {
                url = url.addParameter("fs", "yes");
            }
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
