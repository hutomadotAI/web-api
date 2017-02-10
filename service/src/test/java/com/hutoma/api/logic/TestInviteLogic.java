package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for InviteLogic methods.
 */
public class TestInviteLogic {

    private JsonSerializer fakeSerializer;
    private Config fakeConfig;
    private SecurityContext fakeContext;
    private Database fakeDatabase;
    private ILogger fakeLogger;
    private InviteLogic fakeInviteLogic;
    private final String inviteCode = "invitecode";
    private final String userName = "user@hutoma.com";

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeInviteLogic = new InviteLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeLogger);
    }

    /*
     * Validate that an invalid invite code will cause a 200.
     */
    @Test
    public void testValidInviteCode_Success() throws Database.DatabaseException, java.sql.SQLException {
        when(this.fakeDatabase.inviteCodeValid(anyString())).thenReturn(true);
        final ApiResult result = this.fakeInviteLogic.validCode(inviteCode);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    /*
     * Validate that an invalid invite code will cause a 404.
     */
    @Test
    public void testValidInviteCode_Failure() throws Database.DatabaseException, java.sql.SQLException {
        when(this.fakeDatabase.inviteCodeValid(anyString())).thenReturn(false);
        final ApiResult result = this.fakeInviteLogic.validCode(inviteCode);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    /*
     * Validate that a valid invite code redeemed successfully returns 201.
     */
    @Test
    public void testValidInviteCodeRedeemed_Success() throws Database.DatabaseException, java.sql.SQLException {
        when(this.fakeDatabase.inviteCodeValid(anyString())).thenReturn(true);
        when(this.fakeDatabase.redeemInviteCode(anyString(), anyString())).thenReturn(true);
        final ApiResult result = this.fakeInviteLogic.redeemCode(inviteCode, userName);
        Assert.assertEquals(201, result.getStatus().getCode());
    }

    /*
     * Validate that redeeming an invalid invite code returns 400.
     */
    @Test
    public void testValidInviteCodeRedeemed_InvalidCode() throws Database.DatabaseException, java.sql.SQLException {
        when(this.fakeDatabase.redeemInviteCode(anyString(), anyString())).thenReturn(false);
        final ApiResult result = this.fakeInviteLogic.redeemCode(inviteCode, userName);
        Assert.assertEquals(400, result.getStatus().getCode());
    }
}
