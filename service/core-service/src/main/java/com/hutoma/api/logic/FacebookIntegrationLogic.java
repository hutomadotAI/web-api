package com.hutoma.api.logic;


import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.containers.facebook.FacebookNotification;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class FacebookIntegrationLogic {

    private static final String LOGFROM = "fbintegrationlogic";
    private final ILogger logger;
    private final Config config;
    private final JsonSerializer serializer;
    private final ThreadSubPool threadSubPool;
    private Provider<FacebookChatHandler> chatHandlerProvider;

    @Inject
    public FacebookIntegrationLogic(final ILogger logger, final Config config,
                                    final JsonSerializer serializer, ThreadSubPool threadSubPool,
                                    Provider<FacebookChatHandler> chatHandlerProvider) {
        this.logger = logger;
        this.config = config;
        this.serializer = serializer;
        this.threadSubPool = threadSubPool;
        this.chatHandlerProvider = chatHandlerProvider;
    }

    /***
     * Verification call sent by Facebook to make sure that we really are a Facebook webhook handler
     * @param mode =subscription
     * @param challenge this is what we need to return
     * @param verifyToken this is what we expect from facebook
     * @return
     */
    public Response verify(final String mode, final String challenge, final String verifyToken) {

        // did Facebook send us the right token?
        if (verifyToken.equals(this.config.getFacebookVerifyToken())) {
            this.logger.logInfo(LOGFROM, String.format("webhook subscription verification passed"));
            // return the challenge in plaintext
            return Response.ok(challenge, MediaType.TEXT_HTML_TYPE).build();
        }
        // wrong token! discard
        this.logger.logError(LOGFROM, String.format(
                "webhook subscription verification failed. expected verify token \"%s\" received \"%s\"",
                this.config.getFacebookVerifyToken(), verifyToken));
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }

    /***
     * A Facebook notification to tell us that a user has sent a message to one of the pages
     * that we are responding on behalf of
     * @param facebookNotification
     * @return
     */
    public Response chatRequest(final FacebookNotification facebookNotification) {
        try {
            // for each entry (there should only be one)
            facebookNotification.getEntryList().forEach(entry -> {
                // if it is a message entry
                if (entry.isMessagingEntry()) {
                    // for each one of the messages it contains
                    entry.getMessaging().forEach(message ->
                            // open a separate thread to handle the message (and respond)
                            this.threadSubPool.submit(this.chatHandlerProvider.get().initialise(message)));
                } else {
                    // otherwise, log it
                    this.logger.logWarning(LOGFROM, String.format("received a non-messaging entry from Facebook"),
                            LogMap.map("json", this.serializer.serialize(entry)));
                }
            });
        } catch (Exception e) {
            // log if anything goes wrong at all
            this.logger.logError(LOGFROM, String.format("failed to process incoming facebook message"),
                    LogMap.map("json", this.serializer.serialize(facebookNotification)));
        }
        // return OK to acknowledge the webhook call no matter what happens
        // otherwise Facebook will think the service has gone down
        // and (eventually) stop sending us notifications altogether
        return Response.ok().build();
    }

}
