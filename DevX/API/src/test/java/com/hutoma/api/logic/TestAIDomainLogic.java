package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.connectors.Database;
import hutoma.api.server.ai.api_root;
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

    ArrayList<api_root._domain> listOfSingleResult;
    AIDomainLogic aiDomainLogic;

    private String DEVID = "devid";
    private String DOMID = "domid";

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);

        api_root._domain result = new api_root._domain();
        result.dom_id = DOMID;
        listOfSingleResult = new ArrayList<>();
        listOfSingleResult.add(result);

        aiDomainLogic = new AIDomainLogic(fakeConfig, fakeSerializer, fakeDatabase);
    }

    @Test
    public void testGetAll_Valid() {
        when(fakeDatabase.getAllDomains()).thenReturn(listOfSingleResult);
        aiDomainLogic.getDomains(fakeContext, DEVID);
        api_root._domainList domain = ((api_root._domainList) fakeSerializer.getUnserialized());
        Assert.assertEquals(200, domain.status.code);
    }

    @Test
    public void testGetAll_Valid_Result() {
        when(fakeDatabase.getAllDomains()).thenReturn(listOfSingleResult);
        aiDomainLogic.getDomains(fakeContext, DEVID);
        api_root._domainList domain = ((api_root._domainList) fakeSerializer.getUnserialized());
        Assert.assertNotNull(domain.domain_list);
        Assert.assertFalse(domain.domain_list.isEmpty());
        Assert.assertEquals(DOMID, domain.domain_list.get(0).dom_id);
    }

    @Test
    public void testGetAll_DBFail() {
        when(fakeDatabase.getAllDomains()).thenReturn(new ArrayList<api_root._domain>());
        aiDomainLogic.getDomains(fakeContext, DEVID);
        api_root._domainList domain = ((api_root._domainList) fakeSerializer.getUnserialized());
        Assert.assertEquals(500, domain.status.code);
    }

    @Test
    public void testGetAll_NotFound() {
        when(fakeDatabase.getAllDomains()).thenReturn(new ArrayList<api_root._domain>());
        aiDomainLogic.getDomains(fakeContext, DEVID);
        api_root._domainList domain = ((api_root._domainList) fakeSerializer.getUnserialized());
        Assert.assertEquals(404, domain.status.code);
    }
}