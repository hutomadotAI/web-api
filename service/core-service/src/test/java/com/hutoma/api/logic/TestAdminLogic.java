package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.AuthHelper;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTokenRegenResult;
import com.hutoma.api.containers.sub.UserInfo;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 02/08/2016.
 */
public class TestAdminLogic {

    private static final UUID VALIDDEVID = UUID.fromString("b97b80cb-6d6d-4dc6-88a7-061c3b6282a0");
    private static final String DEVTOKEN = "wieqejqwkjeqwejqlkejqwejwldslkfhslkdhflkshflskfh-sdfjdf";
    private static final String ANOTHER_ENCODING_KEY = "werwerer";

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

        when(this.fakeConfig.getEncodingKey()).thenReturn(VALID_ENCODING_KEY);
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
    public void testCreate_notAllowedSecurityRole() throws DatabaseException {
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, createDev(Role.ROLE_ADMIN, Role.ROLE_ADMIN.getPlan()).getStatus().getCode());
    }

    @Test
    public void testCreate_mismatchedRoleAndPlan() throws DatabaseException {
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, createDev(Role.ROLE_FREE, Role.ROLE_ADMIN.getPlan()).getStatus().getCode());
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
        when(this.fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(false);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, createDev().getStatus().getCode());
    }

    @Test
    public void testCreate_ValidKeyDBFail() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
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

    @Test
    public void testRegenerateTokends_singleUser_skip() throws DatabaseException {
        // The user has the correct claims which can be decoded with the current encoding key, so skip
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, this.adminLogic.regenerateTokens(DEVID, false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_singleUser_differentEncodingKey() throws DatabaseException {
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenReturn(true);
        // Use a user created with a different encoding key, thus forcing it to regenerate
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan(), ANOTHER_ENCODING_KEY));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, this.adminLogic.regenerateTokens(DEVID, false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_singleUser_simulateOnly() throws DatabaseException {
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan(), ANOTHER_ENCODING_KEY));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, this.adminLogic.regenerateTokens(DEVID, true).getStatus().getCode());
        verify(this.fakeDatabaseUser, never()).updateUserDevToken(any(), anyString());
    }

    @Test
    public void testRegenerateTokens_singleUser_differentDevIdEncoded() throws DatabaseException {
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenReturn(true);
        UserInfo userInfo = getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan());
        // Set the token to encode a different DevId
        userInfo.setDevToken(AuthHelper.generateDevToken(UUID.randomUUID(), Role.ROLE_FREE.name(), TestDataHelper.VALID_ENCODING_KEY));
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(userInfo);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, this.adminLogic.regenerateTokens(DEVID, false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_failureToUpdateDb() throws DatabaseException {
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenReturn(false);
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan(), ANOTHER_ENCODING_KEY));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, this.adminLogic.regenerateTokens(DEVID, false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_failureToUpdateDbException() throws DatabaseException {
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenThrow(DatabaseException.class);
        when(this.fakeDatabaseUser.getUserFromDevId(any())).thenReturn(getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan(), ANOTHER_ENCODING_KEY));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, this.adminLogic.regenerateTokens(DEVID, false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_invalidDevId() {
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, this.adminLogic.regenerateTokens("NOT_A_UUID", false).getStatus().getCode());
    }

    @Test
    public void testRegenerateTokens_multipleUsers() throws DatabaseException  {
        List<UserInfo> users = getTestUsers();
        when(this.fakeDatabaseUser.getAllUsers()).thenReturn(users);
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenReturn(true);
        ApiTokenRegenResult result = (ApiTokenRegenResult) this.adminLogic.regenerateTokens(null, false);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getSkipped().size());
        Assert.assertEquals(1, result.getUpdated().size());
        Assert.assertEquals(users.get(0).getDevId(), result.getSkipped().get(0).toString());
        Assert.assertEquals(users.get(1).getDevId(), result.getUpdated().get(0).toString());
    }

    @Test
    public void testRegenerateTokens_multipleUsers_simulateOnly() throws DatabaseException  {
        List<UserInfo> users = getTestUsers();
        when(this.fakeDatabaseUser.getAllUsers()).thenReturn(users);
        ApiTokenRegenResult result = (ApiTokenRegenResult) this.adminLogic.regenerateTokens(null, true);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(1, result.getSkipped().size());
        Assert.assertEquals(1, result.getUpdated().size());
        Assert.assertEquals(users.get(0).getDevId(), result.getSkipped().get(0).toString());
        Assert.assertEquals(users.get(1).getDevId(), result.getUpdated().get(0).toString());
        verify(this.fakeDatabaseUser, never()).updateUserDevToken(any(), anyString());
    }

    @Test
    public void testRegenerateTokens_multipleUsers_errorsUpdating() throws DatabaseException  {
        List<UserInfo> users = getTestUsers();
        when(this.fakeDatabaseUser.getAllUsers()).thenReturn(users);
        when(this.fakeDatabaseUser.updateUserDevToken(any(), anyString())).thenThrow(DatabaseException.class);
        ApiTokenRegenResult result = (ApiTokenRegenResult) this.adminLogic.regenerateTokens(null, false);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getSkipped().size());
        Assert.assertEquals(users.get(0).getDevId(), result.getSkipped().get(0).toString());
        Assert.assertEquals(1, result.getErrors().size());
        UUID errorDevId = result.getErrors().keySet().iterator().next();
        Assert.assertEquals(users.get(1).getDevId(), errorDevId.toString());
    }

    private void validKeyDBSuccess() throws DatabaseException {
        when(this.fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
        when(this.fakeDatabaseUser.createDev(any(), any(), any(), any(), any(), any(), any(), anyInt(), any())).thenReturn(true);
    }

    private ApiResult createDev() {
        return this.createDev(Role.ROLE_FREE, Role.ROLE_FREE.getPlan());
    }

    private ApiResult createDev(final Role role, final int planId) {
        return this.adminLogic.createDev(role.name(), "username", "email", "password",
                "passSalt", "first_name", "last_time", planId);
    }

    private ApiResult deleteDev(UUID devid) {
        return this.adminLogic.deleteDev(devid);
    }

    private static UserInfo getUserInfo(final String devId, final Role role, final int planId) {
        return getUserInfo(devId, role, planId, TestDataHelper.VALID_ENCODING_KEY);
    }

    private static UserInfo getUserInfo(final String devId, final Role role, final int planId, final String encodingKey) {
        String token = AuthHelper.generateDevToken(UUID.fromString(devId), role.name(), encodingKey);
        return new UserInfo("test user", "testuser", "test@email.com", DateTime.now(),
                true, false, "password", "salt",
                devId, "0", 1, token, planId);
    }

    private static List<UserInfo> getTestUsers() {
        UserInfo userInfo = getUserInfo(DEVID, Role.ROLE_FREE, Role.ROLE_FREE.getPlan());
        userInfo.setDevToken(AuthHelper.generateDevToken(UUID.randomUUID(), Role.ROLE_FREE.name(), TestDataHelper.VALID_ENCODING_KEY));
        List<UserInfo> users = Arrays.asList(
                getUserInfo(UUID.randomUUID().toString(), Role.ROLE_FREE, Role.ROLE_FREE.getPlan(), VALID_ENCODING_KEY),
                userInfo);
        return users;
    }
}
