package com.hutoma.api.tests.service;

import com.hutoma.api.endpoints.IntegrationEndpoint;
import com.hutoma.api.logic.FacebookIntegrationLogic;
import junitparams.JUnitParamsRunner;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class TestServiceFacebook extends ServiceTestBase {

    private static final String BASEPATH = "/integration/facebook";
    private static final String CHALLENGE = "challenge";
    private static final String FBVERIFY = "fbverify";

    String fbMessage = "{\"object\":\"page\",\"entry\":[{\"id\":\"page_id\",\"time\":1497884978923,"
            + "\"messaging\":[{\"sender\":{\"id\":\"from_fb_user\"},\"recipient\":{\"id\":\"page_id\"},"
            + "\"timestamp\":1497884978787,\"message\":{\"mid\":\"mid.$cAAZ8W3FgXCZi8iw2Y1cwObp3IKnj\","
            + "\"seq\":128,\"text\":\"random user chat\"}}]}]}";

    String fbPostback = "{\"object\":\"page\",\"entry\":[{\"id\":\"1731355550428969\","
            + "\"time\":1498224298036,\"messaging\":[{\"sender\":{\"id\":\"1707037399308287\"},"
            + "\"recipient\":{\"id\":\"632106133664550\"},\"timestamp\":1498224297854,\"postback\":"
            + "{\"title\":\"TITLE_FOR_THE_CTA\",\"payload\":\"hook\",\"referral\":{\"ref\":"
            + "\"USER_DEFINED_REFERRAL_PARAM\",\"source\":\"SHORTLINK\",\"type\":\"OPEN_THREAD\"}}}]}]}";

    String fbOptin = "{\n" +
            "   \"object\":\"page\",\n" +
            "   \"entry\":[\n" +
            "      {\n" +
            "         \"id\":\"1731355550428969\",\n" +
            "         \"time\":1458692752478,\n" +
            "         \"messaging\":[\n" +
            "            {\n" +
            "               \"sender\":{\n" +
            "                  \"id\":\"from_fb_user\"\n" +
            "               },\n" +
            "               \"recipient\":{\n" +
            "                  \"id\":\"page_id\"\n" +
            "               },\n" +
            "               \"timestamp\":1234567890,\n" +
            "               \"optin\":{\n" +
            "                  \"ref\":\"PASS_THROUGH_PARAM\"\n" +
            "               }\n" +
            "            }\n" +
            "         ]\n" +
            "      }\n" +
            "   ]\n" +
            "}";

    @Test
    public void testFBWebhookVerifyOk() {
        when(this.fakeConfig.getFacebookVerifyToken()).thenReturn(FBVERIFY);
        final Response response = target(BASEPATH)
                .queryParam("hub.challenge", CHALLENGE)
                .queryParam("hub.verify_token", FBVERIFY)
                .request()
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        String returnedChallenge = response.readEntity(String.class);
        Assert.assertEquals(CHALLENGE, returnedChallenge);
    }

    @Test
    public void testFBWebhookVerify_BadToken() {
        when(this.fakeConfig.getFacebookVerifyToken()).thenReturn(FBVERIFY);
        final Response response = target(BASEPATH)
                .queryParam("hub.challenge", CHALLENGE)
                .queryParam("hub.verify_token", FBVERIFY + "!")
                .request()
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testFBChat() {
        final Response response = target(BASEPATH)
                .request()
                .post(Entity.json(this.fbMessage));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testFBPostback() {
        final Response response = target(BASEPATH)
                .request()
                .post(Entity.json(this.fbPostback));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testFBChat_empty() {
        final Response response = target(BASEPATH)
                .request()
                .post(Entity.json(""));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testFBOptIn() {
        final Response response = target(BASEPATH)
                .request()
                .post(Entity.json(this.fbOptin));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return IntegrationEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(FacebookIntegrationLogic.class).to(FacebookIntegrationLogic.class);
        return binder;
    }
}
