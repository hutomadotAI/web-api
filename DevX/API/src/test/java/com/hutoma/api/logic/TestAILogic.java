package com.hutoma.api.logic;

import com.amazonaws.services.route53domains.model.CountryCode;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import hutoma.api.server.ai.api_root;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;

import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 04/08/2016.
 */
public class TestAILogic {

    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    MessageQueue fakeMessageQueue;
    Config fakeConfig;
    Tools fakeTools;
    AILogic aiLogic;

    private String DEVID = "devid";
    private String AIID = "aiid";
    private String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private String VALIDDEVID = "DevidExists";

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        when(fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeMessageQueue = mock(MessageQueue.class);
        this.fakeTools = mock(Tools.class);
        when(fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        aiLogic = new AILogic(fakeConfig, fakeSerializer, fakeDatabase, fakeMessageQueue, fakeTools);
    }

    @Test
    public void testCreate_Valid() {
        when(fakeDatabase.createAI(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(true);
        aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        api_root._newai apiRoot = ((api_root._newai)fakeSerializer.getUnserialized());
        Assert.assertEquals(200, apiRoot.status.code);
    }

    @Test
    public void testCreate_Valid_Token() {
        when(fakeDatabase.createAI(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(true);
        aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        api_root._newai apiRoot = ((api_root._newai)fakeSerializer.getUnserialized());
        Assert.assertNotNull(apiRoot.client_token);
        Assert.assertTrue(apiRoot.client_token.length() > 0);
    }

    @Test
    public void testCreate_DBFail_Error() {
        when(fakeDatabase.createAI(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(false);
        aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        api_root._newai apiRoot = ((api_root._newai)fakeSerializer.getUnserialized());
        Assert.assertEquals(500, apiRoot.status.code);
    }

    @Test
    public void testCreate_DBFail_NoToken() {
        when(fakeDatabase.createAI(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(false);
        aiLogic.createAI(fakeContext, DEVID, "name", "description", true, 0.0d, 0, 0, 0);
        api_root._newai apiRoot = ((api_root._newai)fakeSerializer.getUnserialized());
        Assert.assertNull(apiRoot.client_token);
    }

    @Test
    public void testGetSingle_Valid() {
        api_root._ai ai = new api_root._ai();
        when(fakeDatabase.getAI(AIID)).thenReturn(ai);
        aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(200, apiRoot.status.code);
    }

    @Test
    public void testGetSingle_Valid_Return() {
        api_root._ai ai = new api_root._ai();
        ai.aiid = AIID;
        when(fakeDatabase.getAI(AIID)).thenReturn(ai);
        aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertNotNull(apiRoot.ai);
        Assert.assertEquals(AIID, apiRoot.ai.aiid);
    }

    @Test
    public void testGetSingle_DBFail_Error() {
        api_root._ai ai = new api_root._ai();
        when(fakeDatabase.getAI(anyString())).thenReturn(ai);
        aiLogic.getSingleAI(fakeContext, VALIDDEVID, AIID);
        api_root._myAIs apiRoot = ((api_root._myAIs)fakeSerializer.getUnserialized());
        Assert.assertEquals(404, apiRoot.status.code);
    }

}

