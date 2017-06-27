package com.hutoma.api.logic;


import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.facebook.FacebookNotification;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class FacebookIntegrationLogic {

    private static final String LOGFROM = "fbintegrationlogic";
    private final ILogger logger;
    private final Config config;
    private final JsonSerializer serializer;

    @Inject
    public FacebookIntegrationLogic(final ILogger logger, final Config config, final JsonSerializer serializer) {
        this.logger = logger;
        this.config = config;
        this.serializer = serializer;
    }

    public Response verify(final String mode, final String challenge, final String verifyToken) {
        if (verifyToken.equals(this.config.getFacebookVerifyToken())) {
            this.logger.logInfo(LOGFROM, String.format("webhook subscription verification passed"));
            return Response.ok(challenge, MediaType.TEXT_HTML_TYPE).build();
        }
        this.logger.logError(LOGFROM, String.format(
                "webhook subscription verification failed. expected verify token \"%s\" received \"%s\"",
                this.config.getFacebookVerifyToken(), verifyToken));
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }

    public Response chatRequest(final FacebookNotification facebookNotification) {
        this.logger.logInfo(LOGFROM, String.format("incoming facebook message \n%s",
                this.serializer.serialize(facebookNotification)));
        return Response.ok().build();
    }
}
