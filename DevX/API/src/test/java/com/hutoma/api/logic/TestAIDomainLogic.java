package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.ApiAiDomains;
import com.hutoma.api.containers.ApiResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 05/08/2016.
 */
public class TestAIDomainLogic {

    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    Config fakeConfig;
    Logger fakeLogger;

    ArrayList<AiDomain> listOfSingleResult;
    ArrayList<AiDomain> listOfEmpty;
    AIDomainLogic aiDomainLogic;

    private String DEVID = "devid";
    private String DOMID = "domid";

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeLogger = mock(Logger.class);

        AiDomain domain = new AiDomain(DOMID, "name", "desc", "icon", "colour", true);
        listOfSingleResult = new ArrayList<>();
        listOfSingleResult.add(domain);
        listOfEmpty = new ArrayList<>();

        aiDomainLogic = new AIDomainLogic(fakeConfig, fakeSerializer, fakeDatabase, fakeLogger);
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        when(fakeDatabase.getAiDomainList()).thenReturn(listOfSingleResult);
        ApiResult result = aiDomainLogic.getDomains(fakeContext);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Result() throws Database.DatabaseException {
        when(fakeDatabase.getAiDomainList()).thenReturn(listOfSingleResult);
        aiDomainLogic.getDomains(fakeContext);
        ApiAiDomains result = (ApiAiDomains)aiDomainLogic.getDomains(fakeContext);
        Assert.assertNotNull(result.getDomainList());
        Assert.assertFalse(result.getDomainList().isEmpty());
        Assert.assertEquals(DOMID, result.getDomainList().get(0).getDomID());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        when(fakeDatabase.getAiDomainList()).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = aiDomainLogic.getDomains(fakeContext);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_NotFound() throws Database.DatabaseException {
        when(fakeDatabase.getAiDomainList()).thenReturn(listOfEmpty);
        ApiResult result = aiDomainLogic.getDomains(fakeContext);
        Assert.assertEquals(404, result.getStatus().getCode());
    }
}