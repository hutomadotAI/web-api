package com.hutoma.api.logic;

import com.hutoma.api.common.DeveloperInfoHelper;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestBotHelper;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseUI;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.ui.ApiAiDetails;
import com.hutoma.api.containers.ui.ApiBotstoreItem;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;
import com.hutoma.api.logging.ILogger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 28/03/17.
 */
public class TestUILogic {

    private DatabaseUI fakeDatabaseUi;
    private DatabaseAI fakeDatabaseAi;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private DatabaseMarketplace fakeDatabaseMarketplace;
    private ILogger fakeLogger;
    private JsonSerializer fakeSerializer;
    private UILogic uiLogic;

    @Before
    public void setup() {
        this.fakeDatabaseUi = mock(DatabaseUI.class);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.uiLogic = new UILogic(this.fakeDatabaseUi, this.fakeDatabaseMarketplace, this.fakeDatabaseAi,
                this.fakeDatabaseEntitiesIntents, this.fakeLogger, this.fakeSerializer);
    }

    @Test
    public void testGetBotstoreList() throws DatabaseException {
        final int startItem = 0;
        final int pageSize = 100;
        ApiBotstoreItemList itemList = getItemList(startItem, pageSize, 1);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        ApiBotstoreItemList result = (ApiBotstoreItemList) this.uiLogic.getBotstoreList(null, startItem, pageSize, null,
                null, null);
        validateGetBotstoreListResponse(result, itemList.getItems());
    }

    @Test
    public void testGetBotstoreList_authenticated_botOwned() throws DatabaseException {
        final int startItem = 0;
        final int pageSize = 100;
        ApiBotstoreItemList itemList = getItemList(startItem, pageSize, 1);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any()))
                .thenReturn(Collections.singletonList(itemList.getItems().get(0).getMetadata()));
        ApiBotstoreItemList result = (ApiBotstoreItemList) this.uiLogic.getBotstoreList(DEVID_UUID, startItem, pageSize,
                null, null, null);
        validateGetBotstoreListResponse(result, itemList.getItems());
        Assert.assertTrue(result.getItems().get(0).isOwned());
    }

    @Test
    public void testGetBotstoreList_authenticated_botNowOwned() throws DatabaseException {
        final int startItem = 0;
        final int pageSize = 100;
        ApiBotstoreItemList itemList = getItemList(startItem, pageSize, 1);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        ApiBotstoreItemList result = (ApiBotstoreItemList) this.uiLogic.getBotstoreList(DEVID_UUID, startItem, pageSize,
                null, null, null);
        validateGetBotstoreListResponse(result, itemList.getItems());
        Assert.assertFalse(result.getItems().get(0).isOwned());
    }

    @Test
    public void testGetBotstoreList_pagination() throws DatabaseException {
        final int startItem = 0;
        final int pageSize = 5;
        final int totalItems = 9;
        ApiBotstoreItemList itemList = getItemList(startItem, pageSize, totalItems);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        // Go to page 1
        ApiBotstoreItemList result = (ApiBotstoreItemList) this.uiLogic.getBotstoreList(DEVID_UUID, startItem, pageSize,
                null, null, null);
        Assert.assertEquals(startItem, result.getStartItem());
        Assert.assertEquals(pageSize, result.getTotalPage());
        Assert.assertEquals(totalItems, result.getTotalResults());

        // Now go to page 2
        itemList = getItemList(startItem + pageSize, pageSize, totalItems - pageSize);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        result = (ApiBotstoreItemList) this.uiLogic.getBotstoreList(DEVID_UUID, startItem + pageSize, pageSize, null,
                null, null);
        Assert.assertEquals(startItem + pageSize, result.getStartItem());
        Assert.assertEquals(totalItems - pageSize, result.getTotalPage());
        Assert.assertEquals(itemList.getItems().size(), result.getTotalResults());
    }

    @Test
    public void testGetBotstoreList_dbException() throws DatabaseException {
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any()))
                .thenThrow(DatabaseException.class);
        ApiResult result = this.uiLogic.getBotstoreList(DEVID_UUID, 0, 100, null, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotstoreItem() throws DatabaseException {
        BotstoreItem item = new BotstoreItem(0, TestBotHelper.SAMPLEBOT, DeveloperInfoHelper.DEVINFO, false);
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenReturn(item);
        ApiBotstoreItem result = (ApiBotstoreItem) this.uiLogic.getBotstoreBot(null,
                TestBotHelper.SAMPLEBOT.getBotId());
        validateGetBostoreItemResponse(result, item);
    }

    @Test
    public void testGetBotstoreItem_authenticated_notOwned() throws DatabaseException {
        BotstoreItem item = new BotstoreItem(0, TestBotHelper.SAMPLEBOT, DeveloperInfoHelper.DEVINFO, false);
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenReturn(item);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        ApiBotstoreItem result = (ApiBotstoreItem) this.uiLogic.getBotstoreBot(DEVID_UUID,
                TestBotHelper.SAMPLEBOT.getBotId());
        validateGetBostoreItemResponse(result, item);
        Assert.assertFalse(result.getItem().isOwned());
    }

    @Test
    public void testGetBotstoreItem_nonExistent() throws DatabaseException {
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenReturn(null);
        ApiResult result = this.uiLogic.getBotstoreBot(DEVID_UUID, TestBotHelper.SAMPLEBOT.getBotId());
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotstoreItem_dbException() throws DatabaseException {
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.uiLogic.getBotstoreBot(DEVID_UUID, TestBotHelper.SAMPLEBOT.getBotId());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotstoreItem_authenticated_owned() throws DatabaseException {
        BotstoreItem item = new BotstoreItem(0, TestBotHelper.SAMPLEBOT, DeveloperInfoHelper.DEVINFO, false);
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenReturn(item);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any()))
                .thenReturn(Collections.singletonList(TestBotHelper.SAMPLEBOT));
        ApiBotstoreItem result = (ApiBotstoreItem) this.uiLogic.getBotstoreBot(DEVID_UUID,
                TestBotHelper.SAMPLEBOT.getBotId());
        validateGetBostoreItemResponse(result, item);
        Assert.assertTrue(result.getItem().isOwned());
    }

    @Test
    public void testGetAiDetails() throws DatabaseException {
        final List<AiBot> linkedBots = Collections.singletonList(TestDataHelper.getAiBot(1, "bot1"));
        final String trainingFile = "QQQ\nAAA";
        final List<String> intentNameList = Arrays.asList("intent1", "intent2");
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(trainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(intentNameList);
        UUID aiid = UUID.fromString(TestDataHelper.getAI().getAiid());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(linkedBots);
        ApiAiDetails result = (ApiAiDetails) this.uiLogic.getAiDetails(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(trainingFile, result.getTrainingFile());
        Assert.assertEquals(intentNameList, result.getIntents());
        Assert.assertEquals(linkedBots.size(), result.getSkills().size());
        Assert.assertEquals(linkedBots.get(0).getName(), result.getSkills().get(0).getName());
        Assert.assertEquals(linkedBots.get(0).getCategory(), result.getSkills().get(0).getCategory());
        Assert.assertEquals(linkedBots.get(0).getBotId(), result.getSkills().get(0).getBotId());
    }

    @Test
    public void testGetAiDetails_invalidAI() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.uiLogic.getAiDetails(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetAiDetails_databaseException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.uiLogic.getAiDetails(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void validateGetBostoreItemResponse(final ApiBotstoreItem result, final BotstoreItem expectedItem) {
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(expectedItem.getMetadata(), result.getItem().getMetadata());
        Assert.assertEquals(expectedItem.getDeveloperInfo(), result.getItem().getDeveloperInfo());
    }

    private ApiBotstoreItemList getItemList(final int startItem, final int pageSize, final int numBots) {
        List<BotstoreItem> listItems = new ArrayList<>();
        for (int i = 0; i < numBots; i++) {
            listItems.add(new BotstoreItem(i, TestBotHelper.getBot(DEVID_UUID, UUID.randomUUID(), i + 1),
                    DeveloperInfoHelper.DEVINFO, false));
        }
        return new ApiBotstoreItemList(listItems, startItem, listItems.size() < pageSize ? listItems.size() : pageSize,
                listItems.size());
    }

    private void validateGetBotstoreListResponse(final ApiBotstoreItemList result,
            final List<BotstoreItem> expectedListItems) {
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(expectedListItems.size(), result.getItems().size());
        Assert.assertEquals(0, result.getStartItem());
        Assert.assertEquals(expectedListItems.size(), result.getTotalPage());
        Assert.assertEquals(expectedListItems.size(), result.getTotalResults());
        Assert.assertNotNull(result.getItems().get(0).getMetadata());
        Assert.assertNotNull(result.getItems().get(0).getDeveloperInfo());
    }
}
