package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David MG on 06/10/2016.
 */
public class TestEntityLogic {

    SecurityContext fakeContext;
    Database fakeDatabase;
    Config fakeConfig;
    Tools fakeTools;
    EntityLogic entityLogic;
    Logger fakeLogger;

    private final String DEVID = "devid";
    private final String AIID = "aiid";
    private final String UID = "uid";
    private final String ENTNAME = "entity";


    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.entityLogic = new EntityLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase);
    }

    private List<ApiEntity> getEntitiesList() {
        return Arrays.asList(new ApiEntity[] {getEntityEmpty()});
    }

    private ApiEntity getEntity() {
        return new ApiEntity(this.ENTNAME, Arrays.asList(new String[] {"oneval", "twoval"}));
    }

    private ApiEntity getEntityEmpty() {
        return new ApiEntity(this.ENTNAME, Arrays.asList(new String[] {}));
    }

    @Test
    public void testGetEntities_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(this.fakeContext, this.DEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(this.fakeContext, this.DEVID);
        Assert.assertEquals(1, ((ApiEntityList) result).getEntities().size());
        Assert.assertEquals(this.ENTNAME, ((ApiEntityList) result).getEntities().get(0).getEntityName());
    }

    @Test
    public void testGetEntities_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(new ArrayList<ApiEntity>());
        final ApiResult result = this.entityLogic.getEntities(this.fakeContext, this.DEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.entityLogic.getEntities(this.fakeContext, this.DEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }


    @Test
    public void testGetEntity_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(anyString(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(this.fakeContext, this.DEVID, this.ENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(anyString(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(this.fakeContext, this.DEVID, this.ENTNAME);
        Assert.assertEquals(this.ENTNAME, ((ApiEntity) result).getEntityName());
    }

    @Test
    /***
     * If the caller gets a single entity that doesn't exist, then the return is "success" with
     * an empty value list
     */
    public void testGetEntity_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(anyString(), anyString())).thenReturn(getEntityEmpty());
        final ApiResult result = this.entityLogic.getEntity(this.fakeContext, this.DEVID, this.ENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(this.ENTNAME, ((ApiEntity) result).getEntityName());
        Assert.assertEquals(0, ((ApiEntity) result).getEntityList().size());
    }

    @Test
    public void testGetEntity_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(anyString(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.entityLogic.getEntity(this.fakeContext, this.DEVID, this.ENTNAME);
        Assert.assertEquals(500, result.getStatus().getCode());
    }
}
