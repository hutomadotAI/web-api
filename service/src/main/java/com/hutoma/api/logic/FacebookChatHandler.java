package com.hutoma.api.logic;

import com.google.common.base.Strings;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.facebook.FacebookRichContentNode;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.IntegrationType;
import com.hutoma.api.containers.sub.WebHookResponse;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Provider;

public class FacebookChatHandler implements Callable {

    private static final String LOGFROM = "fbchathandler";
    private static final int FB_MESSAGE_SIZE_LIMIT = 640;

    private final Database database;
    private final ILogger logger;
    private Provider<ChatLogic> chatLogicProvider;
    private FacebookConnector facebookConnector;
    private JsonSerializer serializer;
    private Tools tools;

    private FacebookNotification.Messaging messaging;

    @Inject
    public FacebookChatHandler(final Database database, final ILogger logger,
                               final JsonSerializer serializer,
                               final FacebookConnector facebookConnector,
                               final Provider<ChatLogic> chatLogicProvider,
                               final Tools tools) {
        this.database = database;
        this.logger = logger;
        this.chatLogicProvider = chatLogicProvider;
        this.facebookConnector = facebookConnector;
        this.serializer = serializer;
        this.tools = tools;
    }

    /***
     * The all important incoming-message structure
     * @param messaging
     * @return
     */
    public FacebookChatHandler initialise(final FacebookNotification.Messaging messaging) {
        this.messaging = messaging;
        return this;
    }

    /***
     * Separate thread to handle the incoming message
     * @return
     * @throws Exception
     */
    @Override
    public Void call() throws Exception {

        LogMap logMap = LogMap.map("Op", "chat")
                .put("Origin", "facebook");
        try {

            String userQuery = null;

            // common fields
            logMap = logMap
                    .put("Facebook_Sender", this.messaging.getSender())
                    .put("Facebook_Recipient", this.messaging.getRecipient())
                    .put("Facebook_Timestamp", this.messaging.getTimestamp());

            if (this.messaging.isMessage()) {
                // incoming message type event
                userQuery = this.messaging.getMessageText();
                logMap.add("Facebook_Sequence", this.messaging.getMessageSeq());
                logMap.add("Facebook_Question", userQuery);
                logMap.add("Facebook_WebhookType", "message");
            } else if (this.messaging.isPostback()) {
                // postback event type
                userQuery = this.messaging.getPostbackPayload();
                logMap.add("Facebook_WebhookType", "postback");
                logMap.add("Facebook_Question", userQuery);
                logMap.add("Facebook_Referrer", this.messaging.getPostbackReferral());
                logMap.add("Facebook_ClickTitle", this.messaging.getPostbackTitle());
            } else {
                // some other type that we don't handle or care about
                logMap.add("Facebook_WebhookType", "unknown");
            }

            // the pageid that the message was sent to i.e. the page intgerated to the bot
            String recipientPageId = this.messaging.getRecipient();
            // the Facebook user who sent the message (we need to respond to this user)
            String messageOriginatorId = this.messaging.getSender();

            if (Strings.isNullOrEmpty(userQuery)) {
                this.logger.logDebug(LOGFROM, String.format("facebook webhook with no usable payload"), logMap);
                return null;
            }

            // look for and load the integration info from the database
            IntegrationRecord integrationRecord = this.database.getIntegrationResource(
                    IntegrationType.FACEBOOK, recipientPageId);

            // no record? bail
            if (integrationRecord == null) {
                this.logger.logWarning(LOGFROM, String.format("message to unknown facebook recipient pageID %s",
                        recipientPageId), logMap);
                return null;
            }

            // if we are still here then integration is configured, so we have a devid/aiid
            logMap = logMap
                    .put("aiid", integrationRecord.getAiid())
                    .put("devid", integrationRecord.getDevid());

            // but is it enabled? should we respond? bail if not
            if (!integrationRecord.isActive()) {
                this.logger.logWarning(LOGFROM, String.format("message to inactive facebook integration %s",
                        integrationRecord.getAiid().toString()), logMap);
                return null;
            }

            // keep track of whether this worked or not.
            boolean chatSuccess = false;
            String status;

            // load the metadata from the record
            FacebookIntegrationMetadata metadata = (FacebookIntegrationMetadata) this.serializer.deserialize(
                    integrationRecord.getData(), FacebookIntegrationMetadata.class);

            // have we got a valid page token? bail if not
            if ((metadata == null) || Strings.isNullOrEmpty(metadata.getPageToken())) {
                this.logger.logError(LOGFROM,
                        String.format("bad facebook integration config. cannot get page token"),
                        logMap);
                status = "Bad Facebook integration config";
            } else {

                // hash the sender ID and the aiid together to get a chatID
                // that is unique to each user-page pair
                UUID chatID = generateChatId(integrationRecord.getAiid(), messageOriginatorId);

                // TODO: rate limiting
                // TODO: load chat state and check sequence number

                // call the chat logic to get a response
                Pair<String, FacebookRichContentNode> response = getChatResponse(
                        integrationRecord, chatID, userQuery);

                try {
                    // note the start time
                    long startTime = this.tools.getTimestamp();
                    // send the response back to Facebook
                    sendChatResponseToFacebook(integrationRecord, metadata, messageOriginatorId,
                            response.getA(), response.getB());

                    // chat worked. save the status
                    status = "Chat active.";
                    chatSuccess = true;

                    // how long did we wait for sendapi to complete?
                    double duration = (double) (this.tools.getTimestamp() - startTime) / 1000.0;

                    // log the message that we just processed
                    this.logger.logDebug(LOGFROM, String.format("responded to facebook message for %s",
                            integrationRecord.getAiid()), logMap.put("duration", duration));

                } catch (FacebookException fe) {
                    // something went wrong. log it and save the status
                    status = fe.getMessage();
                    this.logger.logWarning(LOGFROM, status, logMap);
                }
            }
            // save the status in the integration record to feed it back to the user
            this.database.updateIntegrationStatus(integrationRecord.getAiid(),
                    IntegrationType.FACEBOOK, status, chatSuccess);
            return null;
        } catch (Database.DatabaseException dbe) {
            this.logger.logException(LOGFROM, dbe);
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
        }
        return null;
    }

    /***
     * Create a chatID by hashing the aiid and the originating user.
     * This guarantees that text typed into a Facebook messenger window by
     * the same user will maintain state.
     * @param aiid
     * @param facebookID
     * @return
     */
    public UUID generateChatId(UUID aiid, String facebookID) {

        // write all the data into a byte buffer
        ByteBuffer bb = ByteBuffer.wrap(new byte[128]);
        bb.putLong(aiid.getMostSignificantBits());
        bb.putLong(aiid.getLeastSignificantBits());
        for (char c : facebookID.toCharArray()) {
            bb.putChar(c);
        }
        byte[] onlyUsed = Arrays.copyOf(bb.array(), bb.position());
        // create a UUID by hashing the data
        return UUID.nameUUIDFromBytes(onlyUsed);
    }

    /***
     * Send a response back to Facebook
     * @param integrationRecord
     * @param metadata
     * @param messageOriginatorId
     * @param responseText
     * @param richContent
     * @throws FacebookException
     */
    private void sendChatResponseToFacebook(final IntegrationRecord integrationRecord,
                                            final FacebookIntegrationMetadata metadata,
                                            final String messageOriginatorId,
                                            final String responseText,
                                            final FacebookRichContentNode richContent) throws FacebookException {
        String acceptedText = null;
        FacebookRichContentNode acceptedRichContent = null;

        if ((richContent != null) && (richContent.getContentType() != null)) {
            // if we accepted the rich-content
            acceptedRichContent = richContent;
        } else {
            // otherwise, use the text version
            // and truncate the message to be smaller than the limit
            acceptedText = responseText.length() > FB_MESSAGE_SIZE_LIMIT ?
                    responseText.substring(0, FB_MESSAGE_SIZE_LIMIT) : responseText;
        }

        // send to facebook
        this.facebookConnector.sendFacebookMessage(messageOriginatorId, metadata.getPageToken(),
                acceptedText, acceptedRichContent);
    }

    private Pair<String, FacebookRichContentNode> getChatResponse(final IntegrationRecord integrationRecord,
                                                                  final UUID chatID, final String userQuery) {
        // call chat logic to create a response
        try {
            // if everything went well then get the answer
            ChatResult chatResult = this.chatLogicProvider.get().chatFacebook(
                    integrationRecord.getAiid(), integrationRecord.getDevid(),
                    userQuery, chatID.toString());

            // was there a webhook response with rich content for facebook?
            WebHookResponse webhookResponse = chatResult.getWebhookResponse();
            FacebookRichContentNode richContent = (webhookResponse == null) ? null : webhookResponse.getFacebookNode();

            return new Pair<>(chatResult.getAnswer(), richContent);

        } catch (ChatLogic.ChatFailedException e) {
            // otherwise get a status message
            return new Pair<>(e.getApiError().getStatus().getInfo(), null);
        }
    }

}