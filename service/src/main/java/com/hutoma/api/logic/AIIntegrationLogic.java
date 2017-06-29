package com.hutoma.api.logic;

import com.google.common.base.Strings;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiFacebookIntegration;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNodeList;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.Integration;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.IntegrationType;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class AIIntegrationLogic {

    private static final String LOGFROM = "integrations";
    private final Config config;
    private final Database database;
    private final ILogger logger;
    private final FacebookConnector facebookConnector;
    private final JsonSerializer serializer;

    @Inject
    public AIIntegrationLogic(Config config, Database database, JsonSerializer serializer,
                              FacebookConnector facebookConnector, ILogger logger) {
        this.config = config;
        this.database = database;
        this.logger = logger;
        this.facebookConnector = facebookConnector;
        this.serializer = serializer;
    }

    public ApiResult getIntegrations(final UUID devId) {

        try {
            List<Integration> integrationList = this.database.getAiIntegrationList();
            if (integrationList.size() == 0) {
                this.logger.logDebug(LOGFROM, "no integrations found");
                return ApiError.getNotFound();
            }
            return new ApiIntegrationList(integrationList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    /***
     * Returns the configuration state of Facebook integration for this bot
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult facebookState(final UUID devid, final UUID aiid) {

        // an empty structure to begin with
        ApiFacebookIntegration facebookIntegration = new ApiFacebookIntegration(this.config.getFacebookAppId(),
                false,
                new DateTime(), "", false, "");

        LogMap logMap = LogMap.map("Op", "facebook getState")
                .put("aiid", aiid)
                .put("devid", devid);

        try {
            // load from the db
            IntegrationRecord record = this.database.getIntegration(aiid, devid, IntegrationType.FACEBOOK);

            if (record != null) {
                // interpret the JSON block of data
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);
                if (metadata != null) {

                    // do we have a token?
                    boolean tokenPresent = !Strings.isNullOrEmpty(metadata.getAccessToken());
                    // is it expired?
                    boolean tokenValid = metadata.getAccessTokenExpiry().isAfter(DateTime.now());

                    // ok, we have enough data to send back a non-empty state
                    facebookIntegration = new ApiFacebookIntegration(this.config.getFacebookAppId(),
                            tokenPresent && tokenValid,
                            metadata.getAccessTokenExpiry(), metadata.getUserName(),
                            record.isActive(), record.getStatus());

                    if (tokenPresent && tokenValid) {
                        // if we have no page token then get a list of available pages
                        // that the user can select
                        if (Strings.isNullOrEmpty(metadata.getPageToken())) {
                            // get nodes and convert to an id->name map
                            Map<String, String> pages = getListOfUserPages(metadata).stream().collect(
                                    Collectors.toMap(FacebookNode::getId, FacebookNode::getName));
                            // set the structure in the response object
                            facebookIntegration.setPageList(pages);
                            // log success
                            logMap.add("state", "listing available pages");
                        } else {
                            // otherwise just list the page that we are integrated with
                            facebookIntegration.setPageIntegrated(
                                    record.getIntegrationResource(), metadata.getPageName());
                            logMap.add("state", "integration active");
                        }
                    } else {
                        logMap.add("state", "token missing or expired");
                    }
                } else {
                    logMap.add("state", "no metadata");
                }
            } else {
                logMap.add("state", "none");
            }
            this.logger.logUserTraceEvent(LOGFROM, "facebook integration getState", devid.toString(), logMap);
        } catch (FacebookException.FacebookAuthException authException) {
            facebookIntegration.setIntegrationStatus(String.format("Error: %s",
                    authException.getFacebookErrorMessage()));
            // and flag an error
            facebookIntegration.setSuccess(false);
            // and log it
            logMap.add("error", authException.getMessage());
        } catch (FacebookException e) {
            this.logger.logError(LOGFROM, e.getMessage(), logMap);
            return ApiError.getInternalServerError(e.getMessage());
        } catch (Database.DatabaseException dbe) {
            this.logger.logError(LOGFROM, dbe.getMessage(), logMap);
            return ApiError.getInternalServerError(dbe.getMessage());
        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage(), logMap);
            return ApiError.getInternalServerError(e.getMessage());
        }
        return facebookIntegration.setSuccessStatus();
    }

    /***
     * Connect a bot to a Facebook account using an auth code that was generated by Facebook UI
     * @param devid
     * @param aiid
     * @param facebookConnect auth code
     * @return
     */
    public ApiResult facebookConnect(final UUID devid, final UUID aiid, final FacebookConnect facebookConnect) {

        LogMap logMap = LogMap.map("Op", "facebook connect")
                .put("aiid", aiid)
                .put("devid", devid);

        try {
            // get an access token
            FacebookToken token = this.facebookConnector.getFacebookUserToken(facebookConnect);

            // if it is a two-hour token, get a 60 day
            if (this.facebookConnector.isShortLivedToken(token)) {
                token = this.facebookConnector.getLongFromShortLivedToken(token);
            }

            // get the user data from the token
            FacebookNode userNode = this.facebookConnector.getFacebookUserFromToken(token);
            logMap.add("facebook_user", userNode.getName());
            logMap.add("facebook_id", userNode.getId());

            // create the metadata
            FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(token.getAccessToken(),
                    userNode.getName(), token.getExpires());

            // save it
            this.database.updateIntegration(aiid, devid, IntegrationType.FACEBOOK,
                    null, userNode.getId(), this.serializer.serialize(metadata),
                    String.format("Connected to \"%s\" Facebook account", userNode.getName()), false);

            this.logger.logUserTraceEvent(LOGFROM, "facebook account connect", devid.toString(), logMap);

        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "error connecting an account to facebook",
                    devid.toString(), e, logMap);
            // TODO: differentiate between different kinds of errors to message back to the user
            return ApiError.getInternalServerError(e.getMessage());
        }

        return new ApiResult().setSuccessStatus();
    }

    /***
     * Perform an action for Facebook integration
     * @param devid
     * @param aiid
     * @param action
     * @param pageId
     * @return
     */
    public ApiResult facebookAction(final UUID devid, final UUID aiid, final String action, final String pageId) {

        LogMap logMap = LogMap.map("Op", "facebook connect")
                .put("aiid", aiid)
                .put("devid", devid)
                .put("action", action);

        String failReason;
        try {
            IntegrationRecord record = this.database.getIntegration(aiid, devid, IntegrationType.FACEBOOK);
            if (record != null) {
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);

                if (!Strings.isNullOrEmpty(action)) {
                    switch (action) {
                        case "page":
                            return pageSelect(logMap, devid, aiid, record, metadata, pageId);
                        case "disconnect":
                            return disconnect(logMap, devid, aiid, record, metadata);
                        case "renew":
                        case "activate":
                        case "deactivate":
                        default:
                            break;
                    }
                }
                failReason = "Action not recognised";
            } else {
                failReason = "Your account is not connected to Facebook.";
            }
            // TODO distinguish between different classes of error messages
        } catch (Database.DatabaseException dbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "error performing facebook action",
                    devid.toString(), dbe, logMap);
            return ApiError.getInternalServerError();
        } catch (FacebookException fbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "error performing facebook action",
                    devid.toString(), fbe, logMap);
            return ApiError.getBadRequest(fbe.getMessage());
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "error performing facebook action",
                    devid.toString(), ex, logMap);
            return ApiError.getInternalServerError();
        }
        this.logger.logUserTraceEvent(LOGFROM, "facebook integration action request failed",
                devid.toString(), logMap.put("failreason", failReason));
        return ApiError.getBadRequest(failReason);
    }

    /***
     * Unsubscribe or cancel any integrations once the bot is being deleted
     * If an error occurs, log it but carry on regardless
     * @param aiid
     * @param devid
     */
    public void deleteIntegrations(UUID aiid, UUID devid) {

        LogMap logMap = LogMap.map("Op", "facebook integration")
                .put("aiid", aiid)
                .put("devid", devid)
                .put("action", "delete");

        try {
            IntegrationRecord record = this.database.getIntegration(aiid, devid, IntegrationType.FACEBOOK);
            if (record == null) {
                // there was no integration; nothing to delete
                return;
            }

            try {
                // load the metadata
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);

                // try to unsubscribe
                unsubscribeToFacebookPage(logMap, record, metadata);
            } finally {
                // delete the integration
                this.database.deleteIntegration(aiid, devid, IntegrationType.FACEBOOK);
            }
            // log the result
            this.logger.logUserTraceEvent(LOGFROM, String.format("deleted facebook integration for bot %s",
                    aiid.toString()), devid.toString(), logMap);

        } catch (Database.DatabaseException dbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "error deleting facebook integration",
                    devid.toString(), dbe, logMap);
        }
    }

    /***
     * Disconnect a bot from facebook
     * @param logMap
     * @param devid
     * @param aiid
     * @param record
     * @param metadata
     * @return
     * @throws Database.DatabaseException
     * @throws FacebookException
     */
    private ApiResult disconnect(final LogMap logMap, final UUID devid, final UUID aiid,
                                 final IntegrationRecord record, final FacebookIntegrationMetadata metadata)
            throws Database.DatabaseException, FacebookException {

        unsubscribeToFacebookPage(logMap, record, metadata);

        // clear the database record
        this.database.updateIntegration(aiid, devid, IntegrationType.FACEBOOK,
                null, null,
                this.serializer.serialize(new FacebookIntegrationMetadata()), "", false);
        // log
        this.logger.logUserTraceEvent(LOGFROM, "facebook integration disconnected",
                devid.toString(), logMap);
        return new ApiResult().setSuccessStatus("Bot disconnected");
    }

    private void unsubscribeToFacebookPage(final LogMap logMap, final IntegrationRecord record,
                                           final FacebookIntegrationMetadata metadata) {
        try {
            // if we have a token, use it to unsubscribe
            if ((record != null) && (metadata != null) && !Strings.isNullOrEmpty(metadata.getPageToken())) {
                this.facebookConnector.pageUnsubscribe(record.getIntegrationResource(), metadata.getPageToken());
                logMap.add("unsubscribe", "succeeded");
            } else {
                // otherwise log the fact that we don't
                logMap.add("unsubscribe", "not necessary");
            }
        } catch (FacebookException fbe) {
            // if we fail, log the failure
            logMap.add("unsubscribe", "failed");
            logMap.add("unsubscribe_error", fbe.toString());
        }
    }

    /***
     * Get a list of the user's pages that we can integrate with this bot
     * @param metadata
     * @return
     * @throws FacebookException
     */
    private List<FacebookNode> getListOfUserPages(final FacebookIntegrationMetadata metadata) throws FacebookException {
        FacebookNodeList nodeList = this.facebookConnector.getUserPages(metadata.getAccessToken());
        if (nodeList != null && nodeList.getData() != null) {
            return nodeList.getData();
        }
        return Collections.EMPTY_LIST;
    }

    /***
     * Select a page to integrate the bot with
     * This is the final stage of Facebook integration
     * @param logMap
     * @param devid
     * @param aiid
     * @param record
     * @param metadata
     * @param page
     * @return
     * @throws Database.DatabaseException
     * @throws FacebookException
     */
    private ApiResult pageSelect(final LogMap logMap, final UUID devid, final UUID aiid,
                                 final IntegrationRecord record, final FacebookIntegrationMetadata metadata,
                                 final String page)
            throws Database.DatabaseException, FacebookException {

        logMap.add("facebook_page_id", page);
        // get a list of pages and filter out the one the user has selected
        FacebookNode pageNode = getListOfUserPages(metadata).stream()
                .filter(node -> node.getId().equalsIgnoreCase(page))
                .findFirst()
                .orElse(null);

        // if there is none then bail
        if (pageNode == null) {
            this.logger.logUserTraceEvent(LOGFROM, "facebook integration page select failed",
                    devid.toString(), logMap.put("failreason", "page not found"));
            return ApiError.getBadRequest("Facebook page not found");
        }

        // subscribe to notifications for that page
        this.facebookConnector.pageSubscribe(pageNode.getId(), pageNode.getAccessToken());

        // store information for this page
        record.setIntegrationResource(pageNode.getId());
        metadata.setPageToken(pageNode.getAccessToken());
        metadata.setPageName(pageNode.getName());
        record.setStatus(String.format("Waiting for chat."));
        logMap.add("facebook_page_name", metadata.getPageName());
        logMap.add("facebook_user_name", metadata.getUserName());

        // store the new record
        this.database.updateIntegration(aiid, devid, IntegrationType.FACEBOOK,
                pageNode.getId(), record.getIntegrationUserid(),
                this.serializer.serialize(metadata), record.getStatus(), true);

        // log
        this.logger.logUserInfoEvent(LOGFROM, String.format("integrated bot %s with Facebook page %s for user %s",
                aiid.toString(), metadata.getPageName(), metadata.getUserName()), devid.toString(), logMap);

        return new ApiResult().setSuccessStatus("Link successful.");
    }

}