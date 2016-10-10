package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.IntentVariable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 10/10/2016.
 */
public class TestIntentLogic {

    private final String DEVID = "devid";
    private final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private final String INTENTNAME = "intent";
    private final String TOPICIN = "topicin";
    private final String TOPICOUT = "topicout";
    SecurityContext fakeContext;
    Database fakeDatabase;
    Config fakeConfig;
    Tools fakeTools;
    IntentLogic intentLogic;
    Logger fakeLogger;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.intentLogic = new IntentLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase);
    }

    private List<String> getIntentsList() {
        return Arrays.asList(new String[] {this.INTENTNAME, "intent2"});
    }

    private ApiIntent getIntent() {
        return new ApiIntent(this.INTENTNAME, this.TOPICIN, this.TOPICOUT)
            .addResponse("response").addUserSays("usersays")
            .addVariable(new IntentVariable("entity", true, 3, "somevalue").addPrompt("prompt"));
    }

    private ApiIntent getIntentEmpty() {
        return new ApiIntent(this.INTENTNAME, this.TOPICIN, this.TOPICOUT);
    }

    @Test
    public void testGetIntents_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(getIntentsList());
        final ApiResult result = this.intentLogic.getIntents(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(2, ((ApiIntentList) result).getIntentNames().size());
        Assert.assertEquals(this.INTENTNAME, ((ApiIntentList) result).getIntentNames().get(0));
    }

    @Test
    public void testGetIntents_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenReturn(new ArrayList<String>());
        final ApiResult result = this.intentLogic.getIntents(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetIntents_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntents(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.intentLogic.getIntents(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }


    @Test
    public void testGetIntent_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(getIntent());
        final ApiResult result = this.intentLogic.getIntent(this.fakeContext, this.DEVID, this.AIID, this.INTENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(getIntent());
        final ApiResult result = this.intentLogic.getIntent(this.fakeContext, this.DEVID, this.AIID, this.INTENTNAME);
        Assert.assertEquals(this.INTENTNAME, ((ApiIntent) result).getIntentName());
    }

    @Test
    public void testGetIntent_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenReturn(null);
        final ApiResult result = this.intentLogic.getIntent(this.fakeContext, this.DEVID, this.AIID, this.INTENTNAME);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetIntent_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getIntent(anyString(), any(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.intentLogic.getIntent(this.fakeContext, this.DEVID, this.AIID, this.INTENTNAME);
        Assert.assertEquals(500, result.getStatus().getCode());
    }
}
