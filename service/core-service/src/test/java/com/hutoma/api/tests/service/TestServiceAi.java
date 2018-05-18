package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.endpoints.AIEndpoint;
import com.hutoma.api.logic.AIIntegrationLogic;
import com.hutoma.api.logic.AILogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public class TestServiceAi extends ServiceTestBase {

    private static final String AI_BASEPATH = "/ai";
    private static final String AI_PATH = AI_BASEPATH + "/" + AIID;
    private static final String BOTS_BASEPATH = AI_PATH + "/bots";
    private static final String BOT_BASEPATH = AI_PATH + "/bot";
    private static final String BOT_PATH = BOT_BASEPATH + "/" + BOTID;
    private static final String BOT_CLONE_PATH = AI_BASEPATH + "/" + AIID + "/clone";
    private static final String IMPORT_BASEPATH = AI_BASEPATH + "/" + "import";

    private static MultivaluedMap<String, String> getCreateAiRequestParams() {
        return new MultivaluedHashMap<String, String>() {{
            this.add("name", "ainame");
            this.add("description", null);
            this.add("confidence", "0.5");
            this.add("timezone", "UTC");
            this.add("locale", "en-US");
        }};
    }

    @Test
    public void testGetAIs() throws DatabaseException {
        ApiAi ai = TestDataHelper.getAI();
        when(this.fakeDatabaseAi.getAllAIs(any(), any())).thenReturn(Collections.singletonList(ai));
        final Response response = target(AI_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiList list = deserializeResponse(response, ApiAiList.class);
        Assert.assertEquals(1, list.getAiList().size());
        Assert.assertEquals(ai.getAiid(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAI() throws DatabaseException {
        ApiAi ai = TestDataHelper.getAI();
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(ai);
        final Response response = target(AI_PATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAi responseAi = deserializeResponse(response, ApiAi.class);
        Assert.assertEquals(ai.getAiid(), responseAi.getAiid());
    }

    @Test
    public void testGetSingle_QueuedMasked() throws DatabaseException {
        ApiAi result = checkMaskedTrainingStatus(TrainingStatus.AI_TRAINING_QUEUED, 0.1);
        Assert.assertEquals(TrainingStatus.AI_TRAINING, result.getSummaryStatusPublic());
    }

    @Test
    public void testDeleteAI() throws DatabaseException {
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        final Response response = target(AI_PATH).request().headers(defaultHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testDeleteAI_clientToken_forbidden() {
        UUID aiid = UUID.randomUUID();
        MultivaluedHashMap<String, Object> authHeaders = getClientAuthHeaders(UUID.randomUUID(), aiid);
        final Response response = target(AI_BASEPATH + "/" + aiid).request().headers(authHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.getStatus());
    }

    @Test
    public void testCreateAI() throws DatabaseException {
        final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(this.fakeTools.createNewRandomUUID()).thenReturn(uuid);
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, uuid);
        when(this.fakeDatabaseAi.getAI(any(), any(), any(), any())).thenReturn(TestDataHelper.getAI());
        final Response response = target(AI_BASEPATH).request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateAI() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabaseAi.updateAI(any(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), anyInt(), anyInt(), anyString(), any())).thenReturn(true);
        final Response response = target(AI_PATH).request().headers(defaultHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetAI_devId_invalid() {
        Response response = target(AI_PATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetAIs_devId_invalid() {
        Response response = target(AI_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testDeleteAI_devId_invalid() {
        final Response response = target(AI_PATH).request().headers(noDevIdHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testCreateAI_devId_invalid() {
        final Response response = target(AI_BASEPATH).request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUpdateAI_devId_invalid() {
        final Response response = target(AI_PATH).request().headers(noDevIdHeaders).post(
                Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetLinkedBots() throws DatabaseException {
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        final Response response = target(BOTS_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiAiBotList botList = deserializeResponse(response, ApiAiBotList.class);
        Assert.assertEquals(1, botList.getBotList().size());
        Assert.assertEquals(BOTID, botList.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetLinkedBots_devId_invalid() {
        final Response response = target(BOTS_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testLinkBotToAi() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        final Response response = target(BOT_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(new MultivaluedHashMap<>()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testLinkBotToAi_devId_invalid() {
        final Response response = target(BOT_PATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.form(new MultivaluedHashMap<>()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUnlinkBotFromAi() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        final Response response = target(BOT_PATH).request().headers(defaultHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUnlLinkBotFromAi_devId_invalid() {
        final Response response = target(BOT_PATH).request().headers(noDevIdHeaders).delete();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testGetPublishedBotForAI() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        final Response response = target(BOT_BASEPATH).request().headers(defaultHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testGetPublishedBotForAI_devId_invalid() {
        final Response response = target(BOT_BASEPATH).request().headers(noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testCloneBot() throws DatabaseException {
        final UUID aiid = UUID.randomUUID();
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAI(any(), any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeTools.createNewRandomUUID()).thenReturn(aiid);
        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), anyString(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(true);
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, aiid);
          final Response response = target(BOT_CLONE_PATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatus());
    }

    @Test
    public void testCloneBot_devId_invalid() {
        final Response response = target(BOT_CLONE_PATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.form(getCreateAiRequestParams()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testUpdateBotList() throws DatabaseException {
        AiBot bot1 = new AiBot(SAMPLEBOT);
        bot1.setBotId(1);
        bot1.setPublishingType(AiBot.PublishingType.SKILL);
        AiBot bot2 = new AiBot(SAMPLEBOT);
        bot2.setBotId(2);
        bot2.setPublishingType(AiBot.PublishingType.SKILL);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseMarketplace.getBotDetails(bot1.getBotId())).thenReturn(bot1);
        when(this.fakeDatabaseMarketplace.getBotDetails(bot2.getBotId())).thenReturn(bot2);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Arrays.asList(bot1, bot2));
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        final Response response = target(BOTS_BASEPATH)
                .queryParam("bot_list", bot1.getBotId(), bot2.getBotId())
                .request()
                .headers(defaultHeaders)
                .post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    @Test
    public void testUpdateBotList_devId_invalid() {
        final Response response = target(BOTS_BASEPATH)
                .queryParam("bot_list", 1)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.text(""));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testImportBot() throws DatabaseException {
        final UUID newAiid = UUID.randomUUID();
        when(this.fakeTools.createNewRandomUUID()).thenReturn(newAiid);
        when(this.fakeDatabaseAi.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), any(), anyInt(), anyInt(), any(), any(), any())).thenReturn(newAiid);
        when(this.fakeDatabaseAi.getAI(any(), any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        final Response response = target(IMPORT_BASEPATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(getExportedBotJson()));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatus());
    }

    @Test
    public void testImportBot_devId_invalid() {
        final Response response = target(IMPORT_BASEPATH)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.json(getExportedBotJson()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testImportBot_invalidJsonContent() {
        final Response response = target(IMPORT_BASEPATH)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json("{}"));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return AIEndpoint.class;
    }

    @Override
    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIServices.class).to(AIServices.class);
        binder.bind(AILogic.class).to(AILogic.class);
        binder.bind(AIIntegrationLogic.class).to(AIIntegrationLogic.class);
        return binder;
    }

    private String getExportedBotJson() {
        return "{\"version\":1,\"name\":\"exported_bot\",\"description\":\"desc\",\"isPrivate\":false, \"personality\":0,"
                + "\"confidence\":0.4000000059604645,\"voice\":1, \"language\":\"en-US\",\"timezone\":\"Europe\\/London\"}";
    }

    private ApiAi checkMaskedTrainingStatus(
            TrainingStatus trainingStatus, double trainingProgress) throws DatabaseException {
        BackendStatus status = new BackendStatus();
        status.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(
                trainingStatus, 0.0, trainingProgress));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(status));
        final Response response = target(AI_PATH).request().headers(defaultHeaders).get();
        return deserializeResponse(response, ApiAi.class);
    }
}
