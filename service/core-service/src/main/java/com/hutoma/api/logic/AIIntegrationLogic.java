package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseIntegrations;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiFacebookCustomisation;
import com.hutoma.api.containers.ApiFacebookIntegration;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookMessengerProfile;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNodeList;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.Integration;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.IntegrationType;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class AIIntegrationLogic {

    private static final String LOGFROM = "integrations";
    private final Config config;
    private final DatabaseIntegrations databaseIntegrations;
    private final ILogger logger;
    private final FacebookConnector facebookConnector;
    private final JsonSerializer serializer;

    @Inject
    public AIIntegrationLogic(Config config, DatabaseIntegrations databaseIntegrations, JsonSerializer serializer,
                              FacebookConnector facebookConnector, ILogger logger) {
        this.config = config;
        this.databaseIntegrations = databaseIntegrations;
        this.logger = logger;
        this.facebookConnector = facebookConnector;
        this.serializer = serializer;
    }

    public ApiResult getIntegrations(final UUID devId) {

        try {
            List<Integration> integrationList = this.databaseIntegrations.getAiIntegrationList();
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

    public ApiResult setFacebookCustomisation(final UUID devid, final UUID aiid,
                                              final ApiFacebookCustomisation facebookCustomisation) {

        LogMap logMap = LogMap.map("Op", "writeCustomisations")
                .put("aiid", aiid)
                .put("devid", devid);
        try {

            IntegrationRecord updatedRecord = this.databaseIntegrations.updateIntegrationRecord(aiid, devid,
                    IntegrationType.FACEBOOK,
                    (record) -> {
                        // load a record if we have one
                        FacebookIntegrationMetadata metadata = null;
                        if (record != null) {
                            metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                                    record.getData(), FacebookIntegrationMetadata.class);
                        } else {
                            record = new IntegrationRecord(aiid, devid, null);
                        }
                        // create new metadata or load the old one
                        metadata = (metadata == null) ? new FacebookIntegrationMetadata() : metadata;

                        // set the fields we care about
                        metadata.setPageGreeting(StringUtils.defaultIfEmpty(
                                facebookCustomisation.getPageGreeting(), null));
                        metadata.setGetStartedPayload(StringUtils.defaultIfEmpty(
                                facebookCustomisation.getGetStartedPayload(), null));

                        // set the new data
                        record.setData(this.serializer.serialize(metadata));
                        return record;
                    });

            sendCustomisationsToFacebook(updatedRecord, logMap);
            this.logger.logUserTraceEvent(LOGFROM, "facebook set customisations", devid.toString(), logMap);

        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage(), logMap);
            return ApiError.getInternalServerError(e.getMessage());
        }
        return new ApiResult().setSuccessStatus();
    }

    public ApiResult getFacebookCustomisation(final UUID devid, final UUID aiid) {

        LogMap logMap = LogMap.map("Op", "readCustomisations")
                .put("aiid", aiid)
                .put("devid", devid);
        try {
            IntegrationRecord record = this.databaseIntegrations.getIntegration(aiid, devid, IntegrationType.FACEBOOK);
            if (record != null) {
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);
                if (metadata != null) {
                    return new ApiFacebookCustomisation(
                            StringUtils.defaultString(metadata.getPageGreeting()),
                            StringUtils.defaultString(metadata.getGetStartedPayload())).setSuccessStatus();
                }
            }
        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage(), logMap);
            return ApiError.getInternalServerError(e.getMessage());
        }
        return new ApiFacebookCustomisation("", "");
    }

    /***
     * Returns the configuration state of Facebook integration for this bot
     * @param devid
     * @param aiid
     * @return
     */
    public ApiResult getFacebookState(final UUID devid, final UUID aiid) {

        // an empty structure to begin with
        ApiFacebookIntegration facebookIntegration = new ApiFacebookIntegration(
                String.join(",", this.getRequiredPermissions()),
                this.config.getFacebookAppId(),
                false,
                new DateTime(), "", false, "");

        LogMap logMap = LogMap.map("Op", "facebook getState")
                .put("aiid", aiid)
                .put("devid", devid);

        try {
            // load from the db
            IntegrationRecord record = this.databaseIntegrations.getIntegration(aiid, devid, IntegrationType.FACEBOOK);

            if (record != null) {
                // interpret the JSON block of data
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);

                // do we have a token?
                boolean tokenPresent = !Tools.isEmpty(metadata.getAccessToken());
                // is it expired?
                boolean tokenValid = metadata.getAccessTokenExpiry().isAfter(DateTime.now());

                // ok, we have enough data to send back a non-empty state
                facebookIntegration = new ApiFacebookIntegration(
                        String.join(",", this.getRequiredPermissions()),
                        this.config.getFacebookAppId(),
                        tokenPresent && tokenValid,
                        metadata.getAccessTokenExpiry(), metadata.getUserName(),
                        record.isActive(), record.getStatus());

                if (tokenPresent && tokenValid) {

                    // do we still have access rights?
                    checkGrantedPermissions(record.getIntegrationUserid(), metadata.getAccessToken());

                    // if we have no page token then get a list of available pages
                    // that the user can select
                    if (Tools.isEmpty(record.getIntegrationResource())
                            || Tools.isEmpty(metadata.getPageToken())) {
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

            this.logger.logUserTraceEvent(LOGFROM, "facebook integration getState", devid.toString(), logMap);
        } catch (FacebookException.FacebookMissingPermissionsException missing) {
            facebookIntegration.setIntegrationStatus(String.format("Error: %s",
                    missing.getMessage()));
            // and flag an error
            facebookIntegration.setSuccess(false);
            // and log it
            logMap.add("error", missing.getMessage());
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
        } catch (DatabaseException dbe) {
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
     * @return a 409 if there was an error or a 200 otherwise
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

            // check if anyone else has already connected this Facebook account
            boolean userAlreadyIntegrated = this.databaseIntegrations.isIntegratedUserAlreadyRegistered(
                    IntegrationType.FACEBOOK, userNode.getId(), devid);

            final FacebookToken finalToken = token;
            this.databaseIntegrations.updateIntegrationRecord(aiid, devid, IntegrationType.FACEBOOK,
                    (record) -> {

                        // load a record if we have one
                        FacebookIntegrationMetadata metadata = null;
                        if (record != null) {
                            metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                                    record.getData(), FacebookIntegrationMetadata.class);
                        }
                        // create new metadata or load the old one
                        metadata = (metadata == null) ? new FacebookIntegrationMetadata() : metadata;

                        // check if anyone else has already connected this Facebook account
                        if (userAlreadyIntegrated) {
                            return new IntegrationRecord(aiid, devid,
                                    this.serializer.serialize(metadata));
                        }

                        metadata.connect(finalToken.getAccessToken(),
                                userNode.getName(), finalToken.getExpires());

                        return new IntegrationRecord("", userNode.getId(),
                                this.serializer.serialize(metadata),
                                String.format("Connected to \"%s\" Facebook account", userNode.getName()),
                                false);
                    });

            if (userAlreadyIntegrated) {
                // and log
                this.logger.logUserWarnEvent(LOGFROM, "facebook account already in use", devid.toString(),
                        logMap);
                return ApiError.getConflict(
                        "Cannot connect as account already in use. "
                        + "Please disconnect from other account or contact support");
            }

            // make sure we got the permissions we require
            checkGrantedPermissions(userNode.getId(), token.getAccessToken());

            this.logger.logUserInfoEvent(LOGFROM, "facebook account connect", devid.toString(), logMap);


        } catch (FacebookException.FacebookMissingPermissionsException missing) {
            this.logger.logUserWarnEvent(LOGFROM, "facebook account connect with missing permissions",
                    devid.toString(), logMap.put("missing", missing.getMessage()));
            return ApiError.getConflict(missing.getMessage());
        } catch (FacebookException.FacebookAuthException auth) {
            this.logger.logUserExceptionEvent(LOGFROM, "auth error while connecting facebook account",
                    devid.toString(), auth, logMap);
            return ApiError.getConflict(auth.getFacebookErrorMessage());
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "error connecting an account to facebook",
                    devid.toString(), e, logMap);
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
            IntegrationRecord record = this.databaseIntegrations.getIntegration(aiid, devid, IntegrationType.FACEBOOK);
            if (record != null) {
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);

                if (!Tools.isEmpty(action)) {
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
            IntegrationRecord record = this.databaseIntegrations.getIntegration(aiid, devid, IntegrationType.FACEBOOK);
            if (record == null) {
                // there was no integration; nothing to delete
                return;
            }

            try {
                // load the metadata
                FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                        record.getData(), FacebookIntegrationMetadata.class);

                // delete customisations
                removeCustomisationsFromFacebook(record, devid, logMap);

                // try to unsubscribe
                unsubscribeToFacebookPage(logMap, record, metadata);
            } finally {
                // delete the integration
                this.databaseIntegrations.deleteIntegration(aiid, devid, IntegrationType.FACEBOOK);
            }
            // log the result
            this.logger.logUserTraceEvent(LOGFROM, String.format("deleted facebook integration for bot %s",
                    aiid.toString()), devid.toString(), logMap);

        } catch (DatabaseException dbe) {
            this.logger.logUserExceptionEvent(LOGFROM, "error deleting facebook integration",
                    devid.toString(), dbe, logMap);
        }
    }

    /***
     * Send the customisations that we stored in the database record
     * to Facebook i.e. customise the page integration
     * @param updatedRecord
     * @throws FacebookException
     */
    private void sendCustomisationsToFacebook(final IntegrationRecord updatedRecord, final LogMap logMap)
            throws FacebookException {
        if ((updatedRecord != null) && (updatedRecord.isActive())) {
            FacebookIntegrationMetadata metadata =
                    (FacebookIntegrationMetadata) this.serializer.deserialize(
                            updatedRecord.getData(), FacebookIntegrationMetadata.class);
            customiseFacebookIntegration(updatedRecord.getIntegrationResource(), metadata.getPageToken(),
                    metadata.getPageGreeting(), metadata.getGetStartedPayload(),
                    logMap);
        }
    }

    /***
     * Switch off any Facebook customisations before we disconnect or delete
     * @param updatedRecord
     * @param devid
     */
    private void removeCustomisationsFromFacebook(final IntegrationRecord updatedRecord,
                                                  final UUID devid, final LogMap logMap) {
        if ((updatedRecord != null) && (updatedRecord.isActive())) {
            try {
                FacebookIntegrationMetadata metadata =
                        (FacebookIntegrationMetadata) this.serializer.deserialize(
                                updatedRecord.getData(), FacebookIntegrationMetadata.class);
                customiseFacebookIntegration(updatedRecord.getIntegrationResource(), metadata.getPageToken(),
                        null, null,
                        logMap);
            } catch (FacebookException facebookException) {
                // for unknown reasons we sometimes get an error from facebook
                // OAuthException 100: (#100) You must set a Get Started button if you also wish to use persistent menu.
                // when we are clearing the get started button
                // even though we don't support a persistent menu
                this.logger.logUserWarnEvent(LOGFROM,
                        String.format("clear facebook customisations failed with %s", facebookException.getMessage()),
                        devid.toString(), logMap);
            }
        }
    }

    private void customiseFacebookIntegration(final String integrationResource, final String pageToken,
                                              final String greeting, final String getStarted,
                                              final LogMap logMap) throws FacebookException {

        FacebookMessengerProfile profile = new FacebookMessengerProfile(greeting, getStarted);
        logMap.add("Facebook_Customise_Greeting", profile.isSetGreeting() ? "set" : "deleted");
        logMap.add("Facebook_Customise_GetStarted", profile.isSetGetStarted() ? "set" : "deleted");
        this.facebookConnector.setFacebookMessengerProfile(pageToken, profile);
    }


    /***
     * Disconnect a bot from facebook
     * @param logMap
     * @param devid
     * @param aiid
     * @param integrationRecord
     * @param integrationMetadata
     * @return the API result
     * @throws DatabaseException
     * @throws FacebookException
     */
    private ApiResult disconnect(final LogMap logMap, final UUID devid, final UUID aiid,
                                 final IntegrationRecord integrationRecord,
                                 final FacebookIntegrationMetadata integrationMetadata)
            throws DatabaseException {

        try {
            // switch off customisations
            removeCustomisationsFromFacebook(integrationRecord, devid, logMap);

            unsubscribeToFacebookPage(logMap, integrationRecord, integrationMetadata);
            
        } finally {
            // clear the database record
            this.databaseIntegrations.updateIntegrationRecord(aiid, devid, IntegrationType.FACEBOOK,
                    (record) -> {

                        // load a record if we have one
                        FacebookIntegrationMetadata metadata = null;
                        if (record != null) {
                            metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                                    record.getData(), FacebookIntegrationMetadata.class);
                        }
                        // create new metadata or load the old one
                        metadata = (metadata == null) ? new FacebookIntegrationMetadata() : metadata;

                        // clear the accesstoken, username, token expiry, retaining other data
                        metadata.connect("", "", null);

                        // clear the integration resource, integrationUserid and status
                        // and set active=false
                        return new IntegrationRecord("", "",
                                this.serializer.serialize(metadata), "", false);
                    });
        }

        // log
        this.logger.logUserTraceEvent(LOGFROM, "facebook integration disconnected",
                devid.toString(), logMap);
        return new ApiResult().setSuccessStatus("Bot disconnected");
    }

    private void unsubscribeToFacebookPage(final LogMap logMap, final IntegrationRecord record,
                                           final FacebookIntegrationMetadata metadata) {
        try {
            // if we have a token, use it to unsubscribe
            if (record != null && metadata != null && !Tools.isEmpty(metadata.getPageToken())) {
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
     * @return the list of Facebook nodes
     * @throws FacebookException
     */
    private List<FacebookNode> getListOfUserPages(final FacebookIntegrationMetadata metadata) throws FacebookException {
        FacebookNodeList nodeList = this.facebookConnector.getUserPages(metadata.getAccessToken());
        if (nodeList != null && nodeList.getData() != null) {
            return nodeList.getData();
        }
        return Collections.emptyList();
    }

    /***
     * Select a page to integrate the bot with
     * This is the final stage of Facebook integration
     * @param logMap
     * @param devid
     * @param aiid
     * @param integrationRecord
     * @param integrationMetadata
     * @param page
     * @return the API result
     * @throws DatabaseException
     * @throws FacebookException
     */
    private ApiResult pageSelect(final LogMap logMap, final UUID devid, final UUID aiid,
                                 final IntegrationRecord integrationRecord,
                                 final FacebookIntegrationMetadata integrationMetadata,
                                 final String page)
            throws DatabaseException, FacebookException {

        logMap.add("facebook_page_id", page);
        // get a list of pages and filter out the one the user has selected
        FacebookNode pageNode = getListOfUserPages(integrationMetadata).stream()
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

        logMap.add("facebook_page_name", pageNode.getName());
        logMap.add("facebook_user_name", integrationMetadata.getUserName());

        IntegrationRecord updatedRecord = this.databaseIntegrations.updateIntegrationRecord(aiid, devid,
                IntegrationType.FACEBOOK,
                (record) -> {
                    // load a record if we have one
                    FacebookIntegrationMetadata metadata = null;
                    if (record != null) {
                        metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                                record.getData(), FacebookIntegrationMetadata.class);
                    }
                    // create new metadata or load the old one
                    metadata = (metadata == null) ? new FacebookIntegrationMetadata() : metadata;

                    metadata.setPageToken(pageNode.getAccessToken());
                    metadata.setPageName(pageNode.getName());
                    record.setIntegrationResource(pageNode.getId());
                    record.setStatus(String.format("Waiting for chat."));
                    record.setActive(true);

                    record.setData(this.serializer.serialize(metadata));
                    return record;
                });

        sendCustomisationsToFacebook(updatedRecord, logMap);

        // log
        this.logger.logUserInfoEvent(LOGFROM, String.format("integrated bot %s with Facebook page %s for user %s",
                aiid.toString(), integrationMetadata.getPageName(), integrationMetadata.getUserName()),
                devid.toString(), logMap);

        return new ApiResult().setSuccessStatus("Link successful.");
    }

    /***
     * Checks the permissions that we have been granted against the ones that we requested
     * and throws an exception if there are missing permissions
     * @param id
     * @param accessToken
     * @throws FacebookException.FacebookMissingPermissionsException
     */
    protected void checkGrantedPermissions(final String id, final String accessToken) throws FacebookException {

        // get permissions from facebook
        FacebookNodeList nodeList = this.facebookConnector.getUserGrantedPermissions(id, accessToken);
        // extract the list
        List<FacebookNode> nodes = ((nodeList == null) || (nodeList.getData() == null))
                ? Collections.EMPTY_LIST : nodeList.getData();
        // filter only the granted ones and convert to a word list
        List<String> granted = nodes.stream()
                .filter(permission -> permission.getStatus().equals("granted"))
                .map(FacebookNode::getPermission)
                .collect(Collectors.toList());
        // get a set of permissions that we require to function
        Set<String> required = new HashSet<String>(Arrays.asList(getRequiredPermissions()));
        // subtract the granted from the required
        required.removeAll(granted);
        // if any required are left over then throw an exception
        if (!required.isEmpty()) {
            throw new FacebookException.FacebookMissingPermissionsException(
                    String.format("Required permissions not granted: %s",
                            String.join(",", required)));
        }
    }

    protected String[] getRequiredPermissions() {
        return new String[]{
                "manage_pages", "pages_show_list", "pages_messaging"
        };
    }

}
