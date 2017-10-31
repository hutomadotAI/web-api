package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 02/08/2016.
 */
public class TestAdminLogic {

    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final UUID VALIDDEVID = UUID.fromString("b97b80cb-6d6d-4dc6-88a7-061c3b6282a0");
    private static final String DEVTOKEN = "wieqejqwkjeqwejqlkejqwejwldslkfhslkdhflkshflskfh-sdfjdf";

    private JsonSerializer fakeSerializer;
    private DatabaseUser fakeDatabaseUser;
    private DatabaseMarketplace fakeDatabaseMarketplace;
    private AIServices fakeAiServices;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private AdminLogic adminLogic;
    private AIBotStoreLogic fakeBotstoreLogic;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseUser = mock(DatabaseUser.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeBotstoreLogic = mock(AIBotStoreLogic.class);
        this.adminLogic = new AdminLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabaseUser, this.fakeLogger,
                this.fakeAiServices, this.fakeBotstoreLogic);
    }

    @Test
    public void testCreate_Valid() throws DatabaseException {
        validKeyDBSuccess();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws DatabaseException {
        validKeyDBSuccess();
        Assert.assertNotNull(((ApiAdmin) createDev()).getDev_token());
    }

    @Test
    public void testCreate_KeyNull() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(null);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_InvalidKey() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn("[]");
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyUpdateFail() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(false);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyDBFail() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any()))
                .thenThrow(DatabaseException.class);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testDelete_Success() throws DatabaseException {
        when(this.fakeDatabaseUser.deleteDev(any())).thenReturn(true);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, deleteDev(VALIDDEVID).getStatus().getCode());
    }

    @Test
    public void testDelete_NullDevid() throws DatabaseException {
        when(this.fakeDatabaseUser.deleteDev(any())).thenThrow(DatabaseException.class);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, deleteDev(null).getStatus().getCode());
    }

    @Test
    public void testDelete_NonExistentDevid() throws DatabaseException {
        when(this.fakeDatabaseUser.deleteDev(any())).thenReturn(false);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, deleteDev(UUID.randomUUID()).getStatus().getCode());
    }

    @Test
    public void testDelete_dbException() throws DatabaseException {
        when(this.fakeDatabaseUser.deleteDev(any())).thenThrow(DatabaseException.class);
        ApiResult result = this.adminLogic.deleteDev(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetDevToken() throws DatabaseException {
        when(this.fakeDatabaseUser.getDevToken(any())).thenReturn(DEVTOKEN);
        ApiAdmin admin = (ApiAdmin) this.adminLogic.getDevToken(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, admin.getStatus().getCode());
    }

    @Test
    public void testGetDevToken_dbException() throws DatabaseException {
        when(this.fakeDatabaseUser.getDevToken(any())).thenThrow(DatabaseException.class);
        ApiResult result = this.adminLogic.getDevToken(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetDevToken_devTokenEmpty() throws DatabaseException {
        when(this.fakeDatabaseUser.getDevToken(any())).thenReturn(null);
        ApiResult result = this.adminLogic.getDevToken(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testAutoPurchaseBots_botsList_purchasesBots() throws DatabaseException {
        validKeyDBSuccess();
        when(this.fakeConfig.getAutoPurchaseBotIds()).thenReturn(Arrays.asList("1", "4"));
        when(this.fakeBotstoreLogic.purchaseBot(any(), anyInt())).thenReturn(new ApiResult().setSuccessStatus());
        ApiAdmin result = (ApiAdmin) createDev();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeBotstoreLogic, times(2)).purchaseBot(any(), anyInt());
        verify(this.fakeBotstoreLogic, times(1)).purchaseBot(result.getDevid(), 1);
        verify(this.fakeBotstoreLogic, times(1)).purchaseBot(result.getDevid(), 4);
    }

    @Test
    public void testAutoPurchaseBots_emptyBotsList_doesnotPurchaseBots() throws DatabaseException {
        validKeyDBSuccess();
        when(this.fakeConfig.getAutoPurchaseBotIds()).thenReturn(Collections.emptyList());
        ApiAdmin result = (ApiAdmin) createDev();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeBotstoreLogic, never()).purchaseBot(any(), anyInt());
    }

    @Test
    public void testAutoPurchaseBots_botsList_failPurchase_silentError() throws DatabaseException {
        validKeyDBSuccess();
        when(this.fakeBotstoreLogic.purchaseBot(any(), anyInt())).thenReturn(ApiError.getNotFound());
        when(this.fakeConfig.getAutoPurchaseBotIds()).thenReturn(Collections.singletonList("1"));
        ApiAdmin result = (ApiAdmin) createDev();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeDatabaseMarketplace, never()).purchaseBot(any(), anyInt());
    }

    private void validKeyDBSuccess() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
    }

    private ApiResult createDev() {
        return this.adminLogic.createDev("ROLE", "username", "email", "password", "passSalt", "first_name", "last_time", 0);
    }

    private ApiResult deleteDev(UUID devid) {
        return this.adminLogic.deleteDev(devid);
    }
}
