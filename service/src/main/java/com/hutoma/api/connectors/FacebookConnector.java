package com.hutoma.api.connectors;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookMachineID;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNodeList;
import com.hutoma.api.containers.facebook.FacebookResponseSegment;
import com.hutoma.api.containers.facebook.FacebookRichContentNode;
import com.hutoma.api.containers.facebook.FacebookToken;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public class FacebookConnector {

    private static final String LOGFROM = "facebookconnector";
    private Config config;
    private ILogger logger;
    private JerseyClient jerseyClient;
    private JsonSerializer jsonSerializer;
    private FacebookMachineID machineIdState;

    @Inject
    public FacebookConnector(final Config config, final ILogger logger, final FacebookMachineID machineIdState,
                             final JerseyClient jerseyClient, final JsonSerializer jsonSerializer) {
        this.config = config;
        this.logger = logger;
        this.jerseyClient = jerseyClient;
        this.jsonSerializer = jsonSerializer;
        this.machineIdState = machineIdState;
    }

    /***
     * Tokens are typically of two hour or two month validity.
     * If it is longer than a month then we consider it short-lived.
     * @param token
     * @return
     */
    public boolean isShortLivedToken(FacebookToken token) {
        return (token.getExpiresInSeconds() > 0) // token does expire
                && (token.getExpiresInSeconds() < (60 * 60 * 24 * 30)); // in less than a month
    }

    /***
     * If we are sent a short-lived token then we call facebook api to swap it for a long-lived one
     * @see https://developers.facebook.com/docs/facebook-login/access-tokens/expiration-and-extension
     * @param shortToken
     * @return
     * @throws FacebookException
     */
    public FacebookToken getLongFromShortLivedToken(FacebookToken shortToken) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path("oauth")
                .path("access_token")
                .queryParam("client_id", this.config.getFacebookAppId())
                .queryParam("client_secret", this.config.getFacebookAppSecret())
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("fb_exchange_token", shortToken.getAccessToken());
        return getFacebookToken(target);
    }

    /***
     * On the client side we are given a code as a result of user-login and auth
     * We send the code to Facebook to get an access token.
     * @see https://developers.facebook.com/docs/facebook-login/access-tokens/expiration-and-extension
     * @param facebookConnect
     * @return
     * @throws FacebookException
     */
    public FacebookToken getFacebookUserToken(FacebookConnect facebookConnect) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path("oauth")
                .path("access_token")
                .queryParam("client_id", this.config.getFacebookAppId())
                .queryParam("client_secret", this.config.getFacebookAppSecret())
                .queryParam("redirect_uri", facebookConnect.getRedirectUri())
                .queryParam("code", facebookConnect.getConnectToken());

        // if we have previously been issued a machine_id then we include it here
        String machineId = this.machineIdState.getMachineId();
        if (machineId != null) {
            target = target.queryParam("machine_id", machineId);
        }
        return getFacebookToken(target);
    }

    /***
     * Reply to a message on behalf of a Facebook page
     * @param toFacebookID
     * @param pageToken
     * @param responseSegment
     * @throws FacebookException
     */
    public void sendFacebookMessage(String toFacebookID, String pageToken,
                                    FacebookResponseSegment responseSegment)
            throws FacebookException {
        SendMessage sendMessage = new SendMessage(toFacebookID);
        if (responseSegment.isRichContentSegment()) {
            sendMessage.setRichContent(responseSegment.getRichContentNode());
        } else {
            sendMessage.setText(responseSegment.getText());
        }
        sendFacebookMeMessages(toFacebookID, pageToken, sendMessage);
    }
    
    /***
     * Send a Facebook sender-action on behalf of the responding bot
     * This can be typing-on, typing-off or mark-seen
     * @param toFacebookID
     * @param pageToken
     * @param sendAction
     * @throws FacebookException
     */
    public void sendFacebookSenderAction(String toFacebookID, String pageToken,
                                         SendMessage.SenderAction sendAction)
            throws FacebookException {
        SendMessage sendMessage = new SendMessage(toFacebookID, sendAction);
        sendFacebookMeMessages(toFacebookID, pageToken, sendMessage);
    }

    /***
     * Get the Facebook userid and name from an access token
     * @param token
     * @return
     * @throws FacebookException
     */
    public FacebookNode getFacebookUserFromToken(FacebookToken token) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path("me")
                .queryParam("fields", "id,name")
                .queryParam("access_token", token.getAccessToken());
        String body = webCall(target, RequestMethod.GET, this.config.getFacebookGraphAPITimeout());
        return (FacebookNode) this.jsonSerializer.deserialize(body, FacebookNode.class);
    }

    /***
     * Get a list of Facebook pages that this user owns or administers
     * These are candidates for facebook chat integration
     * @param accessToken
     * @return
     * @throws FacebookException
     */
    public FacebookNodeList getUserPages(String accessToken) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path("me")
                .path("accounts")
                .queryParam("fields", "id,name,perms,access_token")
                .queryParam("access_token", accessToken);
        String body = webCall(target, RequestMethod.GET, this.config.getFacebookGraphAPITimeout());
        return (FacebookNodeList) this.jsonSerializer.deserialize(body, FacebookNodeList.class);
    }

    public FacebookNodeList getUserGrantedPermissions(String userid, String accessToken) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path(userid)
                .path("permissions")
                .queryParam("access_token", accessToken);
        String body = webCall(target, RequestMethod.GET, this.config.getFacebookGraphAPITimeout());
        return (FacebookNodeList) this.jsonSerializer.deserialize(body, FacebookNodeList.class);
    }

    /***
     * Disconnect a Facebook account altogether
     * This is not currently in use because we cannot guarantee that it will not
     * cut off another Hutoma user that shares the same Facebook login
     * @param accessToken
     * @throws FacebookException
     */
    public void disconnectAccount(final String accessToken)
            throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path("me")
                .path("permissions")
                .queryParam("access_token", accessToken);
        webCall(target, RequestMethod.DELETE, this.config.getFacebookGraphAPITimeout());
    }

    /***
     * Tell facebook to stop sending us messages that are directed to this Facebook page
     * i.e. deactivate facebook integration
     * @param pageId
     * @param pageAccessToken
     * @throws FacebookException
     */
    public void pageUnsubscribe(final String pageId, final String pageAccessToken) throws FacebookException {
        subscribedApps(pageId, pageAccessToken, RequestMethod.DELETE);
    }

    /***
     * Tell facebook to start sending us messages that are directed to this Facebook page
     * i.e. activate facebook integration
     * @param pageId
     * @param pageAccessToken
     * @throws FacebookException
     */
    public void pageSubscribe(final String pageId, final String pageAccessToken) throws FacebookException {
        subscribedApps(pageId, pageAccessToken, RequestMethod.POST);
    }

    private void sendFacebookMeMessages(String toFacebookID, String pageToken,
                                        SendMessage sendMessage)
            throws FacebookException {
        // set up the parameters
        JerseyWebTarget target = getGraphApiTarget()
                .path("me")
                .path("messages")
                .queryParam("client_id", this.config.getFacebookAppId())
                .queryParam("client_secret", this.config.getFacebookAppSecret())
                .queryParam("access_token", pageToken);

        // send the serialized payload to facebook
        webCall(target, RequestMethod.POST, Entity.json(this.jsonSerializer.serialize(sendMessage)),
                this.config.getFacebookSendAPITimeout());
    }

    /***
     * Subscription on/off functionality
     * @param pageId
     * @param pageAccessToken
     * @param requestMethod
     * @throws FacebookException
     */
    private void subscribedApps(final String pageId, final String pageAccessToken,
                                final RequestMethod requestMethod) throws FacebookException {
        JerseyWebTarget target = getGraphApiTarget()
                .path(pageId)
                .path("subscribed_apps")
                .queryParam("access_token", pageAccessToken);
        webCall(target, requestMethod, this.config.getFacebookGraphAPITimeout());
    }

    /***
     * Base Graph API endpoint including version number
     * @return
     */
    private JerseyWebTarget getGraphApiTarget() {
        return this.jerseyClient
                .target("https://graph.facebook.com")
                .path("v2.9");
    }

    /***
     * Shared code that handles calls resulting in an access token
     * @param target
     * @return
     * @throws FacebookException
     */
    private FacebookToken getFacebookToken(final JerseyWebTarget target) throws FacebookException {
        String body = webCall(target, RequestMethod.GET, this.config.getFacebookGraphAPITimeout());
        try {
            // try to get a token
            FacebookToken token = (FacebookToken)
                    this.jsonSerializer.deserialize(body, FacebookToken.class);
            if (token != null) {
                // get expiry and machine id
                token.calculateExpiry();
                this.machineIdState.setMachineId(token.getMachineId());
                return token;
            }
        } catch (JsonParseException jpe) {
            // failed to get token
        }
        throw new FacebookException("Could not get token from Facebook");
    }

    /***
     * By default, send an empty body on POST and PUT
     * @param target
     * @param requestmethod
     * @return
     * @throws FacebookException
     */
    private String webCall(JerseyWebTarget target, RequestMethod requestmethod, int readTimeout) throws FacebookException {
        return webCall(target, requestmethod, Entity.text(""), readTimeout);
    }

    /***
     * Make a call to Facebook Graph API and try to read an entity body
     * @param target
     * @param requestmethod
     * @param entity
     * @return
     * @throws FacebookException
     */
    private String webCall(JerseyWebTarget target, RequestMethod requestmethod,
                           Entity entity, int readTimeout) throws FacebookException {

        try {
            JerseyInvocation.Builder builder = target
                    .property(CONNECT_TIMEOUT, this.config.getFacebookGraphAPITimeout())
                    .property(READ_TIMEOUT, readTimeout)
                    .request();

            Response response;
            switch (requestmethod) {
                case POST:
                    response = builder.post(entity);
                    break;
                case PUT:
                    response = builder.put(entity);
                    break;
                case DELETE:
                    response = builder.delete();
                    break;
                case GET:
                default:
                    response = builder.get();
                    break;
            }

            // what did we get back?
            Response.StatusType statusInfo = response.getStatusInfo();
            switch (statusInfo.getStatusCode()) {
                // if it went well then just read the entity
                case HttpURLConnection.HTTP_OK:
                    return response.readEntity(String.class);
                // otherwise try to interpret the result
                default:
                    throw response.hasEntity()
                            ? FacebookException.exceptionMapper(statusInfo.getStatusCode(),
                            statusInfo.getReasonPhrase(),
                            response.readEntity(String.class), this.jsonSerializer)
                            : FacebookException.exceptionMapper(statusInfo.getStatusCode(),
                            statusInfo.getReasonPhrase(),
                            null, this.jsonSerializer);
            }
        } catch (FacebookException fe) {
            throw fe;
        } catch (Exception e) {
            throw new FacebookException(e.toString());
        }
    }

    private enum RequestMethod {
        GET,
        PUT,
        POST,
        DELETE,
    }

    public static class SendMessage {

        @SerializedName("recipient")
        private Recipient recipient;
        @SerializedName("message")
        private MessagePayload message;
        @SerializedName("sender_action")
        private SenderAction senderAction;

        public SendMessage(final String recipient) {
            this.recipient = new Recipient();
            this.recipient.id = recipient;
            this.message = new MessagePayload();
        }

        public SendMessage(final String recipient, final SenderAction senderAction) {
            this.recipient = new Recipient();
            this.recipient.id = recipient;
            this.senderAction = senderAction;
        }

        public void setText(final String text) {
            this.message.text = text;
        }

        public void setRichContent(FacebookRichContentNode content) {
            this.message.richContent = content;
        }

        public enum SenderAction {
            typing_on,
            typing_off,
            mark_seen
        }

        private static class Recipient {
            @SerializedName("id")
            private String id;
        }

        private static class MessagePayload {
            @SerializedName("text")
            private String text;

            @SerializedName("attachment")
            private FacebookRichContentNode richContent;
        }
    }

}
