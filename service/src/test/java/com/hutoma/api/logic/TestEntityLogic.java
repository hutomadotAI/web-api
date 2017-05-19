package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.DEVID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 06/10/2016.
 */
public class TestEntityLogic {

    private final String ENTNAME = "entity";
    DatabaseEntitiesIntents fakeDatabase;
    Config fakeConfig;
    EntityLogic entityLogic;
    ILogger fakeLogger;
    TrainingLogic trainingLogic;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.entityLogic = new EntityLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase, this.trainingLogic);
    }

    @Test
    public void testGetEntities_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID);
        Assert.assertEquals(1, ((ApiEntityList) result).getEntities().size());
        Assert.assertEquals(this.ENTNAME, ((ApiEntityList) result).getEntities().get(0));
    }

    @Test
    public void testGetEntities_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenReturn(new ArrayList<>());
        final ApiResult result = this.entityLogic.getEntities(DEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntities(anyString())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntities(DEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(this.ENTNAME, ((ApiEntity) result).getEntityName());
    }

    @Test
    /***
     * If the caller gets a single entity that doesn't exist, then the return is "success" with
     * an empty value list
     */
    public void testGetEntity_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntityEmpty());
        final ApiResult result = this.entityLogic.getEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
        Assert.assertEquals(this.ENTNAME, ((ApiEntity) result).getEntityName());
        Assert.assertEquals(0, ((ApiEntity) result).getEntityValueList().size());
    }

    @Test
    public void testGetEntity_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Success() throws Database.DatabaseException {
        final ApiResult result = this.entityLogic.writeEntity(DEVID, this.ENTNAME, new ApiEntity(this.ENTNAME, DEVID_UUID));
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Error() throws Database.DatabaseException {
        doThrow(Database.DatabaseException.class).when(this.fakeDatabase).writeEntity(anyString(), anyString(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID, this.ENTNAME, new ApiEntity(this.ENTNAME, DEVID_UUID));
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_RenameClash() throws Database.DatabaseException {
        doThrow(new Database.DatabaseIntegrityViolationException(new Exception("test"))).when(this.fakeDatabase).writeEntity(anyString(), anyString(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID, this.ENTNAME, new ApiEntity("nameclash", DEVID_UUID));
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenReturn(true);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenThrow(Database.DatabaseException.class);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenReturn(false);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_entityInUse_triggersStopTraining() throws Database.DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAisForEntity(DEVID, this.ENTNAME)).thenReturn(Collections.singletonList(aiid));
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenReturn(true);
        this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        verify(this.trainingLogic).stopTraining(DEVID, aiid);
    }

    @Test
    public void testDeleteEntity_entityNotInUse_doesNotStopTraining() throws Database.DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID, this.ENTNAME)).thenReturn(new ArrayList<>());
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenReturn(true);
        this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testDeleteEntity_dbError_doesNotStopTraining() throws Database.DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID, this.ENTNAME)).thenReturn(Collections.singletonList(UUID.randomUUID()));
        when(this.fakeDatabase.deleteEntity(anyString(), anyString())).thenThrow(Database.DatabaseException.class);
        this.entityLogic.deleteEntity(DEVID, this.ENTNAME);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_entityInUse_triggersStopTraining() throws Database.DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAisForEntity(DEVID, this.ENTNAME)).thenReturn(Collections.singletonList(aiid));
        this.entityLogic.writeEntity(DEVID, this.ENTNAME, getEntity());
        verify(this.trainingLogic).stopTraining(DEVID, aiid);
    }

    @Test
    public void testWriteEntity_dbError_doesNotStopTraining() throws Database.DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAisForEntity(any(), any())).thenThrow(Database.DatabaseException.class);
        this.entityLogic.writeEntity(DEVID, this.ENTNAME, getEntity());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_entityNotInUse_doesNotStopTraining() throws Database.DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID, this.ENTNAME)).thenReturn(new ArrayList<>());
        this.entityLogic.writeEntity(DEVID, this.ENTNAME, getEntity());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }


    private List<String> getEntitiesList() {
        return Collections.singletonList(this.ENTNAME);
    }

    private ApiEntity getEntity() {
        return new ApiEntity(this.ENTNAME, DEVID_UUID, Arrays.asList("oneval", "twoval"), false);
    }

    private ApiEntity getEntityEmpty() {
        return new ApiEntity(this.ENTNAME, DEVID_UUID, Arrays.asList(new String[]{}), false);
    }


}
