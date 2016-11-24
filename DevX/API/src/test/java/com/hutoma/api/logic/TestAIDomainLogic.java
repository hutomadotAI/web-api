package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiStore;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 05/08/2016.
 */
public class TestAIDomainLogic {

    private final String DEVID = "devid";
    private final String DOMID = "domid";
    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    Config fakeConfig;
    ILogger fakeLogger;
    ArrayList<AiStore> listOfSingleResult;
    ArrayList<AiStore> listOfEmpty;
    AIBotStoreLogic aiDomainLogic;

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeLogger = mock(ILogger.class);

        AiStore domain = new AiStore(this.DOMID, "name", "desc", "icon", "colour", true);
        this.listOfSingleResult = new ArrayList<>();
        this.listOfSingleResult.add(domain);
        this.listOfEmpty = new ArrayList<>();

        this.aiDomainLogic = new AIBotStoreLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeLogger);
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotStoreList()).thenReturn(this.listOfSingleResult);
        ApiResult result = this.aiDomainLogic.getBots(this.fakeContext);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Result() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotStoreList()).thenReturn(this.listOfSingleResult);
        this.aiDomainLogic.getBots(this.fakeContext);
        ApiAiStore result = (ApiAiStore) this.aiDomainLogic.getBots(this.fakeContext);
        Assert.assertNotNull(result.getDomainList());
        Assert.assertFalse(result.getDomainList().isEmpty());
        Assert.assertEquals(this.DOMID, result.getDomainList().get(0).getDomID());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotStoreList()).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = this.aiDomainLogic.getBots(this.fakeContext);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotStoreList()).thenReturn(this.listOfEmpty);
        ApiResult result = this.aiDomainLogic.getBots(this.fakeContext);
        Assert.assertEquals(404, result.getStatus().getCode());
    }
}