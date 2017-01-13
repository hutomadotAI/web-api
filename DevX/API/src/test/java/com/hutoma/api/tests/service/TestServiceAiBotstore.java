package com.hutoma.api.tests.service;

import com.hutoma.api.common.BotHelper;
import com.hutoma.api.common.DeveloperInfoHelper;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.endpoints.AIBotStoreEndpoint;
import com.hutoma.api.logic.AIBotStoreLogic;

import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.hutoma.api.common.BotHelper.BOTID;
import static com.hutoma.api.common.BotHelper.SAMPLEBOT;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * AI Bot Store service tests.
 */
public class TestServiceAiBotstore extends ServiceTestBase {

    private static final String BOTSTORE_BASEPATH = "/botstore";
    private static final String BOTSTORE_BOTPATH = BOTSTORE_BASEPATH + "/" + BOTID;
    private static final String BOTSTORE_PURCHASEDPATH = BOTSTORE_BASEPATH + "/purchased";
    private static final String BOTSTORE_BOTICONPATH = BOTSTORE_BOTPATH + "/icon";
    private static final String BOTSTORE_PURCHASEBOTPATH = BOTSTORE_BASEPATH + "/purchase/" + BOTID;

    private static final MultivaluedMap<String, String> BOT_PUBLISH_POST = new MultivaluedHashMap<String, String>() {{
        this.put("aiid", Collections.singletonList(UUID.randomUUID().toString()));
        this.put("name", Collections.singletonList("bot name"));
        this.put("description", Collections.singletonList("bot description"));
        this.put("price", Collections.singletonList("1.0"));
    }};

    @Test
    public void testGetPublishedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTSTORE_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList list = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, list.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), list.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPublishedBots_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetBotDetails() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(BOTID)).thenReturn(SAMPLEBOT);
        final Response response = target(BOTSTORE_BOTPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBot bot = deserializeResponse(response, ApiAiBot.class);
        Assert.assertNotNull(bot.getBot());
        Assert.assertEquals(BOTID, bot.getBot().getBotId());
    }

    @Test
    public void testGetBotDetails_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BOTPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetPurchasedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTSTORE_PURCHASEDPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList list = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, list.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), list.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPurchasedBots_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_PURCHASEDPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testPurchaseBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(true);
        final Response response = target(BOTSTORE_PURCHASEBOTPATH).request().headers(defaultHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testPurchaseBot_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_PURCHASEBOTPATH).request().headers(noDevIdHeaders).post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testPublishBot() throws Database.DatabaseException {
        final int newBotId = 76832;
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabase.publishBot(any())).thenReturn(newBotId);
        final Response response = target(BOTSTORE_BASEPATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(BOT_PUBLISH_POST));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBot bot = deserializeResponse(response, ApiAiBot.class);
        Assert.assertNotNull(bot.getBot());
        Assert.assertEquals(newBotId, bot.getBot().getBotId());
    }

    @Test
    public void testPublishBot_invalid_devId() throws Database.DatabaseException {
        final Response response = target(BOTSTORE_BASEPATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.form(BOT_PUBLISH_POST));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetBotIcon() throws Database.DatabaseException, IOException {
        final InputStream botIconStream = new ByteArrayInputStream(BotHelper.getBotIconContent());
        when(this.fakeDatabase.getBotIcon(anyInt())).thenReturn(botIconStream);
        final Response response = target(BOTSTORE_BOTICONPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        InputStream inputStream = (InputStream) response.getEntity();
        Assert.assertNotNull(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream);
        Assert.assertEquals(BotHelper.getBotIconContentSize(), outputStream.size());
    }

    @Test
    public void testGetBotIcon_invalid_devId() throws Database.DatabaseException, IOException {
        final Response response = target(BOTSTORE_BOTICONPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUploadBotIcon() throws Database.DatabaseException, IOException {
        when(this.fakeTools.isStreamSmallerThan(any(), anyLong())).thenReturn(true);
        when(this.fakeDatabase.saveBotIcon(anyString(), anyInt(), any())).thenReturn(true);
        FormDataMultiPart multipart = generateIconMultipartEntity();
        final Response response = target(BOTSTORE_BOTICONPATH)
                .register(MultiPartFeature.class)
                .request()
                .headers(defaultHeaders)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        multipart.close();

        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUploadBotIcon_invalid_devId() throws Database.DatabaseException, IOException {
        FormDataMultiPart multipart = generateIconMultipartEntity();
        final Response response = target(BOTSTORE_BOTICONPATH)
                .register(MultiPartFeature.class)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        multipart.close();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    private FormDataMultiPart generateIconMultipartEntity() {
        final FileDataBodyPart filePart = new FileDataBodyPart("file",
                new File(getTestsBaseLocation(), "test-images/hutoma_icon.png"));
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        return (FormDataMultiPart) formDataMultiPart.field("foo", "bar").bodyPart(filePart);
    }

    protected Class<?> getClassUnderTest() {
        return AIBotStoreEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIBotStoreLogic.class).to(AIBotStoreLogic.class);
        return binder;
    }
}
