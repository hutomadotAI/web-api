package com.hutoma.api.logic;


import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class FacebookIntegrationLogic {

    private static final String LOGFROM = "integrationlogic";
    private final ILogger logger;
    private final Config config;

    @Inject
    public FacebookIntegrationLogic(final ILogger logger, final Config config) {
        this.logger = logger;
        this.config = config;
    }

    public Response verify(final String mode, final String challenge, final String verifyToken) {
        if (verifyToken.equals(this.config.getFacebookVerifyToken())) {
            this.logger.logInfo(LOGFROM, String.format("webhook verification mode=%s passed", mode));
            return Response.ok(challenge, MediaType.TEXT_HTML_TYPE).build();
        }
        this.logger.logError(LOGFROM, String.format("webhook verification failed. expected verify token %s received %s",
                this.config.getFacebookVerifyToken(), verifyToken));
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }


}
