package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import io.mikael.urlbuilder.UrlBuilder;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by David MG on 08/08/2016.
 */
public class SemanticAnalysis {

    Config config;
    Logger logger;

    private final String LOGFROM = "wnetconnector";

    @Inject
    public SemanticAnalysis(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public String getAnswer(String devid, String aiid, String q, float min_p, boolean fastSearch) {
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
            return IOUtils.toString(new InputStreamReader(finalUrl.openStream()));
        } catch (IOException e) {
            logger.logError(LOGFROM, "failed to contact SemanticAnalysis server: " + e.toString());
        }
        return "";
    }

    /*
    public String getAnswerOld(String devid, String aiid, String q, float min_p, boolean fastSearch) {
        try
        {
            String wnet_server = config.getWNetServer();
            wnet_server += "q="+q.replace(" ","%20");
            wnet_server += "&aiid="+aiid;
            wnet_server += "&uid="+devid;
            wnet_server += "&min_p="+min_p;
            wnet_server += "&multiprocess=yes&nproc=8&fs=yes";
            if (fastSearch) wnet_server +="&fs=yes";
            URL url = new URL(wnet_server);
            return IOUtils.toString(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {}
        return "";
    }
    */

}
